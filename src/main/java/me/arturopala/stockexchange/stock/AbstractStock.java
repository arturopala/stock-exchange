package me.arturopala.stockexchange.stock;

import java.math.BigDecimal;
import me.arturopala.stockexchange.util.Money;
import me.arturopala.stockexchange.api.Stock;
import me.arturopala.stockexchange.api.StockType;

public abstract class AbstractStock implements Stock {

	private final String symbol;
	private final Money parValue;
	private final Money lastDividend;

	public AbstractStock(String symbol, Money parValue, Money lastDividend){
		this.symbol = symbol;
		this.parValue = parValue;
		this.lastDividend = lastDividend;
	}

	@Override
	public String symbol(){
		return symbol;
	}

	@Override
	public Money parValue(){
		return parValue;
	}

	protected Money lastDividend(){
		return lastDividend;
	}

	@Override
	public double calculatePERatio(Money tickerPrice){
		return tickerPrice.divide(lastDividend);
	}

	@Override
	public final int hashCode(){
		return symbol.hashCode();
	}

	@Override 
	public final boolean equals(Object that){
		if(that != null && that instanceof Stock){
			return ((Stock)that).symbol().equals(this.symbol) 
				&& ((Stock)that).type().equals(this.type())
				&& ((Stock)that).parValue().equals(this.parValue());
		} else {
			return false;
		}
	}

	@Override
	public String toString(){
		return type().toString()+"("+symbol()+","+parValue()+","+lastDividend()+")";
	}

}