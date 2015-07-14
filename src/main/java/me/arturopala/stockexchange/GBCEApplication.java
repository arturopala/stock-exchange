package me.arturopala.stockexchange;

import me.arturopala.stockexchange.gbce.GBCE;
import me.arturopala.stockexchange.api.StockExchange;
import me.arturopala.stockexchange.api.Ticker;
import me.arturopala.stockexchange.util.Money;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import akka.actor.*;
import akka.routing.*;
import scala.concurrent.duration.FiniteDuration;

public class GBCEApplication {
	
	public static void main(String[] args){
		int numberOfTraders = 100;
		if(args.length>0) try { numberOfTraders = Integer.parseInt(args[0]);} catch (Exception e ){}

		ActorSystem actorSystem = ActorSystem.create("gbce");
		StockExchange exchange = GBCE.simpleStockExchange(Clock.systemUTC(), Duration.ofMinutes(15), actorSystem);
		exchange.open();

		ActorRef traderPool = actorSystem.actorOf(Trader.props(exchange).withRouter(new BroadcastPool(numberOfTraders)));
		FiniteDuration tradeTick = FiniteDuration.create(100,"millis");
		Runnable tradeTask = () -> {
			traderPool.tell(Trader.TRADE, ActorRef.noSender());
		};
		actorSystem.scheduler().schedule(tradeTick, tradeTick, tradeTask, actorSystem.dispatcher());

		ActorRef reporter = actorSystem.actorOf(Reporter.props(exchange));
		FiniteDuration reportTick = FiniteDuration.create(5,"seconds");
		Runnable reportTask = () -> {
			reporter.tell(Reporter.REPORT, ActorRef.noSender());
		};
		actorSystem.scheduler().schedule(reportTick, reportTick, reportTask, actorSystem.dispatcher());

		actorSystem.awaitTermination();
	}

	private static class Trader extends UntypedActor {

		private final StockExchange stockExchange;

		public Trader(StockExchange stockExchange){
			this.stockExchange = stockExchange;
		}

		public void onReceive(Object message) throws Exception {
			if(message == TRADE){
				doTrade();
			} else unhandled(message);
	  	}

	  	private void doTrade(){
	  		BigDecimal margin = new BigDecimal(1 + Math.random());
	  		stockExchange.listing().stream().forEach(stock -> {
	  			Ticker ticker = stockExchange.watch(stock);
	  			Money sellPrice = ticker.price().multiply(margin);
	  			if(!sellPrice.isDefined()){
	  				sellPrice = Money.random(stock.parValue());
	  			}
	  			stockExchange.sell(stock, randomQuantity(1000), sellPrice);
	  			Money buyPrice = ticker.price().divide(margin);
	  			if(!buyPrice.isDefined()){
	  				buyPrice = Money.random(stock.parValue());
	  			}
	  			stockExchange.buy(stock, randomQuantity(1000), buyPrice);
	  		});
	  	}

		public static final String TRADE = "TRADE";
	  	public static Props props(StockExchange stockExchange){
	  		return Props.create(Trader.class,stockExchange);
	  	}
	  	public static int randomQuantity(int max){
	  		return (int) Math.round(Math.random() * max + 1);
	  	}
	}

	private static class Reporter extends UntypedActor {

		private final StockExchange stockExchange;

		public Reporter(StockExchange stockExchange){
			this.stockExchange = stockExchange;
			printSeparator();
			System.out.println("Welcome to the Global Beverage Corporation Exchange!");
			printSeparator();
		}

		public void onReceive(Object message) throws Exception {
			if(message == REPORT){
				doReport();
			} else unhandled(message);
	  	}

	  	private void doReport(){
	  		System.out.println("INDEX\t: "+Money.FORMAT.format(stockExchange.allShareIndex()));
	  		System.out.println("SYMBOL        PRICE       QUANTITY           VOLUME[MLN] ");
	  		stockExchange.listing().stream().forEach(stock -> {
	  			Ticker ticker = stockExchange.watch(stock);
	  			System.out.println(stock.symbol()+"  "+pad(" "+ticker.price())+pad(" "+ticker.quantity())+pad(" "+ticker.volume().divide(new BigDecimal(100000))));
	  		});
	  		printSeparator();
	  	}

	  	private String pad(String text){
	  		StringBuilder sb = new StringBuilder();
	  		int point = text.indexOf(".");
	  		if(point==-1) point=text.length();
	  		for(int i = 12-point;i>0;i--){
	  			sb.append(" ");
	  		}
	  		sb.append(text);
	  		for(int i = 0;i < 5-(text.length()-point);i++){
	  			sb.append(" ");
	  		}
	  		return sb.toString();
	  	}

	  	private void printSeparator(){
	  		System.out.println("----------------------------------------------------");
	  	}

		public static final String REPORT = "REPORT";
	  	public static Props props(StockExchange stockExchange){
	  		return Props.create(Reporter.class,stockExchange);
	  	}
	}

}