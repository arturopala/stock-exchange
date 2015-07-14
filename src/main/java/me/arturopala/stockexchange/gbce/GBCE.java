package me.arturopala.stockexchange.gbce;

import me.arturopala.stockexchange.api.*;
import me.arturopala.stockexchange.util.ParseUtils;
import me.arturopala.stockexchange.simpleimpl.SimpleStockExchange;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.stream.Collectors;
import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Function;
import java.time.Clock;
import java.time.Duration;
import akka.actor.ActorSystem;

public class GBCE {

	public static final Set<Stock> LISTING;

	public static StockExchange simpleStockExchange(){
		return new SimpleStockExchange(LISTING);
	}

	public static StockExchange simpleStockExchange(Clock clock, Duration priceCalculationPeriod, ActorSystem actorSystem){
		return new SimpleStockExchange(LISTING, clock, priceCalculationPeriod, actorSystem);
	}

	static {
		String csvFilePath = "/gbce-listing.txt";
		InputStream inputStream = GBCE.class.getResourceAsStream(csvFilePath);
		try {
			LISTING = Collections.unmodifiableSet(ParseUtils.parseStockListing(inputStream));
		}
		finally {
			try{ if(inputStream!=null) inputStream.close(); }
			catch(IOException ioe){}
		}
	}
	
	private GBCE(){}

}