package me.arturopala.stockexchange.stock;

import java.math.BigDecimal;
import me.arturopala.stockexchange.util.Money;
import me.arturopala.stockexchange.api.Stock;
import me.arturopala.stockexchange.api.StockType;

public class PreferredStock extends AbstractStock {

	private final BigDecimal fixedDividend;

	public PreferredStock(String symbol, Money parValue, Money lastDividend, BigDecimal fixedDividend) {
		super(symbol, parValue,lastDividend);
		this.fixedDividend = fixedDividend;
	}

	@Override
	public StockType type(){
		return StockType.PREFERRED;
	}
	
	@Override
	public double calculateDividendYield(Money tickerPrice){
		return (parValue().multiply(fixedDividend)).divide(tickerPrice);
	}

}