package me.arturopala.stockexchange.simpleimpl;

import java.util.*;
import java.util.concurrent.*;
import java.time.Instant;
import java.time.Clock;
import me.arturopala.stockexchange.api.*;
import me.arturopala.stockexchange.stock.*;
import me.arturopala.stockexchange.util.*;

public class Trade {

	public final Instant timestamp;
	public final TradeType type;
	public final Stock stock;
	public final int quantity;
	public final Money price;

	public Trade (Instant timestamp, TradeType type, Stock stock, int quantity, Money price){
		this.timestamp = timestamp;
		this.type = type;
		this.stock = stock;
		this.quantity = quantity;
		this.price = price;
	}

	@Override
	public String toString(){
		return type.toString()+"(stock="+stock+", quantity="+quantity+", price="+price+", time="+timestamp+")";
	}

	public static final Comparator<Trade> BY_TIME_COMPARATOR = new Comparator<Trade>(){
		public int compare(Trade trade1, Trade trade2){
			return trade1.timestamp.compareTo(trade2.timestamp);
		}
	};

}