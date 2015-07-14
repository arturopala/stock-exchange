package me.arturopala.stockexchange.simpleimpl;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.time.Instant;
import java.time.Clock;
import java.time.Duration;
import me.arturopala.stockexchange.api.*;
import me.arturopala.stockexchange.stock.*;
import me.arturopala.stockexchange.util.*;
import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.PoisonPill;
import scala.concurrent.duration.FiniteDuration;

public class SimpleStockExchange implements StockExchange {

	private final Set<Stock> listing;
	private final Clock clock;
	private final Duration priceCalculationPeriod;
	private final ActorSystem actorSystem;
	private final Map<Stock, TickerImpl> tickers = new HashMap<>();

	private volatile boolean isOpen = false;
	private Map<Stock, ActorRef> workers = new HashMap<>();
	private AtomicReference<Double> allShareIndex = new AtomicReference<Double>(Double.NaN);

	private static final String DEFAULT_ACTOR_SYSTEM_NAME = "stockexchange";

	public SimpleStockExchange(Set<Stock> listing){
		this(listing, Clock.systemUTC(), Duration.ofMinutes(15));
	}

	public SimpleStockExchange(Set<Stock> listing, Clock clock){
		this(listing, clock, Duration.ofMinutes(15));
	}
	
	public SimpleStockExchange(Set<Stock> listing, Clock clock, Duration priceCalculationPeriod){
		this(listing, clock, priceCalculationPeriod, ActorSystem.create(DEFAULT_ACTOR_SYSTEM_NAME));
	}

	public SimpleStockExchange(Set<Stock> listing, Clock clock, Duration priceCalculationPeriod, ActorSystem actorSystem){
		this.listing = listing;
		this.clock = clock;
		this.priceCalculationPeriod = priceCalculationPeriod;
		this.actorSystem = actorSystem;
		listing.stream().forEach( stock -> {
			final AtomicReference<StockInfo> stockInfoRef = new AtomicReference<StockInfo>(new StockInfo());
			this.tickers.put(stock, new TickerImpl(stock, stockInfoRef));
		});
	}

	@Override
	public synchronized StockExchange open(){
		if(!isOpen){
			Map<Stock, ActorRef> workers = new HashMap<>();
			listing.stream().forEach( stock -> {
				TickerImpl ticker = tickers.get(stock);
				final ActorRef tickerActorRef = actorSystem.actorOf(
					Props.create(TickerActor.class, stock, ticker.stockInfoRef),
					"stock-"+stock.symbol()+"-"+stock.type()
				);
				workers.put(stock, tickerActorRef);
			});
			this.workers = workers;
			scheduleTicks();
			isOpen = true;
		}
		return this;
	}

	private void scheduleTicks(){
		FiniteDuration delay = FiniteDuration.create(1,"seconds");
		Runnable tickTask = () -> {
			allShareIndex.set(calculateAllShareIndex());
			Tick tick = new Tick(Instant.now(clock).minus(priceCalculationPeriod));
			workers.values().stream().forEach(actor -> actor.tell(tick, ActorRef.noSender()));
		};
		actorSystem.scheduler().schedule(delay,delay,tickTask,actorSystem.dispatcher());
	}

	public Double calculateAllShareIndex(){
		List<Double> prices = tickers.values().stream().map(t -> t.price().doubleValue()).filter(d -> !Double.isNaN(d) && d > 0).collect(Collectors.toList());
		if(prices.size() > 0){
			Double accumulated = prices.stream().reduce(1d, (acc,price) -> acc*price);
			Double order = 1d / prices.size();
			return Math.pow(accumulated, order);
		} else {
			return Double.NaN;
		}
	}

    @Override
	public boolean isOpen(){
		return isOpen && !(actorSystem.isTerminated());
	}

	@Override
	public Set<Stock> listing(){
		return Collections.unmodifiableSet(listing);
	}

	@Override
	public Optional<Stock> find(String symbol){
		return listing.stream().filter(stock -> stock.symbol().equals(symbol)).findFirst();
	}

	@Override
	public void sell(Stock stock, int quantity, Money price){
		if(isOpen){
			if(quantity > 0 && price.isDefined()){
				Trade trade = new Trade(Instant.now(clock), TradeType.SELL,stock,quantity,price);
				ActorRef tickerActor = workers.get(stock);
				if(tickerActor!=null){
					tickerActor.tell(trade, ActorRef.noSender());
				}
			}
		} else {
			throw new StockExchangeClosedException();
		}
	}

	@Override
	public void buy(Stock stock, int quantity, Money price){
		if(isOpen){
			if(quantity > 0 && price.isDefined()){
				Trade trade = new Trade(Instant.now(clock), TradeType.BUY,stock,quantity,price);
				ActorRef tickerActor = workers.get(stock);
				if(tickerActor!=null){
					tickerActor.tell(trade, ActorRef.noSender());
				}
			}
		} else {
			throw new StockExchangeClosedException();
		}
	}

	@Override
	public Ticker watch(Stock stock){
		return tickers.get(stock);
	}

	@Override
	public double allShareIndex(){
		return allShareIndex.get();
	}

	@Override
	public synchronized StockExchange close(){
		if(isOpen){
			isOpen = false;
			workers.values().stream().forEach(w -> w.tell(PoisonPill.getInstance(), ActorRef.noSender()));
			if(actorSystem.name().startsWith(DEFAULT_ACTOR_SYSTEM_NAME)){
				actorSystem.shutdown();
			}
		}
		return this;
	}

	private static class TickerImpl implements Ticker {

		private final Stock stock;
		private final AtomicReference<StockInfo> stockInfoRef;

		TickerImpl(Stock stock, AtomicReference<StockInfo> stockInfoRef){
			this.stock = stock;
			this.stockInfoRef = stockInfoRef;
		}

		@Override
		public Stock stock(){
			return stock;
		}

		@Override
		public Money price(){
			return stockInfoRef.get().price;
		}

		@Override
		public Money volume(){
			return stockInfoRef.get().volume;
		}

		@Override
		public int quantity(){
			return stockInfoRef.get().quantity;
		}


	}
	
}