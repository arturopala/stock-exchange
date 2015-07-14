package me.arturopala.stockexchange.stock;

import java.math.BigDecimal;
import me.arturopala.stockexchange.util.Money;
import me.arturopala.stockexchange.api.Stock;
import me.arturopala.stockexchange.api.StockType;

public class CommonStock extends AbstractStock {

	public CommonStock(String symbol, Money parValue, Money lastDividend){
		super(symbol,parValue,lastDividend);
	}

	@Override
	public StockType type(){
		return StockType.COMMON;
	}
	
	@Override
	public double calculateDividendYield(Money tickerPrice){
		return lastDividend().divide(tickerPrice);
	}

}