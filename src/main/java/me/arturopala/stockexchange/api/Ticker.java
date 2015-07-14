package me.arturopala.stockexchange.api;

import java.math.BigDecimal;
import me.arturopala.stockexchange.util.Money;

public interface Ticker {

	Stock stock();
	
	Money price();

	Money volume();

	int quantity();

}