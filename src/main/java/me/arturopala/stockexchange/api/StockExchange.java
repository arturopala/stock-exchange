package me.arturopala.stockexchange.api;

import java.util.Optional;
import java.util.Set;
import me.arturopala.stockexchange.util.Money;

public interface StockExchange {

	Set<Stock> listing();

	Optional<Stock> find(String symbol);

	void sell(Stock stock, int quantity, Money price) throws StockExchangeClosedException;

	void buy(Stock stock, int quantity, Money price) throws StockExchangeClosedException;

	Ticker watch(Stock stock);

	double allShareIndex();

	StockExchange open();

	StockExchange close();

	boolean isOpen();

}