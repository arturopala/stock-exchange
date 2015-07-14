package me.arturopala.stockexchange.simpleimpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import akka.actor.UntypedActor;
import me.arturopala.stockexchange.api.*;
import me.arturopala.stockexchange.stock.*;
import me.arturopala.stockexchange.util.*;
 
public class TickerActor extends UntypedActor {

  private final Stock stock;
  private final Queue<Trade> queue = new PriorityQueue<Trade>(Trade.BY_TIME_COMPARATOR);
  private final AtomicReference<StockInfo> stockInfoRef;

  private int quantity = 0;
  private Money accumulated = Money.ZERO;
  private Money volume = Money.ZERO;

  public TickerActor(Stock stock, AtomicReference<StockInfo> stockInfoRef){
  	this.stock = stock;
  	this.stockInfoRef = stockInfoRef;
  }
 
  public void onReceive(Object message) throws Exception {
    if (message instanceof Trade) {
      Trade trade = (Trade) message;
  	  if(persist(trade)){
  	      quantity = quantity + trade.quantity;
          Money value = trade.price.multiply(trade.quantity);
  	      accumulated = accumulated.add(value);
          volume = volume.add(value);
  	      queue.add(trade);
  	      updatePrice();
          //System.out.println(trade);
  	  }
    } else if (message instanceof Tick) {
      Tick tick = (Tick) message;
      cleanAndUpdate(tick.timestamp);
    } else {
      unhandled(message);
    }
  }

  public void cleanAndUpdate(Instant last){
  	Trade trade = queue.peek();
  	while (trade != null && hasExpired(trade.timestamp, last)){
  		quantity = quantity - trade.quantity;
  		accumulated = accumulated.subtract(trade.price.multiply(trade.quantity));
  		queue.poll();
  		trade = queue.peek();
  	}
  	updatePrice();
  }

  public void updatePrice(){
   if(quantity > 0){
     Money price = accumulated.divide(quantity);
	   stockInfoRef.set(new StockInfo(price,quantity,volume));
   } else {
     stockInfoRef.set(new StockInfo());
   }
  }

  public boolean persist(Trade trade){
  	//not required to implement persistent storage
  	return true;
  }

  public boolean hasExpired(Instant timestamp, Instant last){
  	return timestamp.isBefore(last);
  }
}