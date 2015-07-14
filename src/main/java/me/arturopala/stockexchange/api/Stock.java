package me.arturopala.stockexchange.api;

import me.arturopala.stockexchange.util.*;
import java.math.BigDecimal;

public interface Stock {

	String symbol();

	StockType type();

	Money parValue();
	
	double calculateDividendYield(Money tickerMoney);
	
	double calculatePERatio(Money tickerMoney);

}