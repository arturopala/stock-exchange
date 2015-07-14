package me.arturopala.stockexchange.simpleimpl;

import me.arturopala.stockexchange.util.Money;

public class StockInfo {

	public final Money price;
	public final int quantity;
	public final Money volume;

	public StockInfo(Money price, int quantity, Money volume){
		this.price = price;
		this.quantity = quantity;
		this.volume = volume;
	} 

	public StockInfo(){
		this(Money.UNDEFINED,0,Money.UNDEFINED);
	}

}