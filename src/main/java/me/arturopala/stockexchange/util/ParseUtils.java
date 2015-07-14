package me.arturopala.stockexchange.util;

import me.arturopala.stockexchange.api.*;
import me.arturopala.stockexchange.stock.*;
import java.util.*;
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

public class ParseUtils {

	public static final Pattern VALUE_REGEX = Pattern.compile("[A-Za-z0-9%.-]+");
	public static final Pattern DECIMAL_REGEX = Pattern.compile("[0-9%.-]+");

	public static final Function<String,Stock> STOCK_PARSER = new Function<String,Stock>() {
		public Stock apply(String line){
			String[] attrs = split(line, VALUE_REGEX);
			if(attrs.length<5){
				throw new IllegalArgumentException("Stock definition must consist of at least 5 arguments"
					+ " separated by any character except point [.] or dash [-]: Symbol,Type,LastDividend,FixedDividend,ParValue");
			}
			try {
				String symbol = attrs[0];
				String type = attrs[1];
				Money lastDividend = Money.parse(attrs[2]);
				BigDecimal fixedDividend = parseDecimal(attrs[3], BigDecimal.ZERO);
				Money parValue = Money.parse(attrs[4]);
				if(type.equals("Common")) return new CommonStock(symbol,parValue,lastDividend);
				else if(type.equals("Preferred")) return new PreferredStock(symbol,parValue,lastDividend,fixedDividend);
				else throw new IllegalArgumentException("Unknown Stock type: "+type);
			}
			catch (Exception e){
				throw new RuntimeException("Error parsing Stock from: "+line, e);
			}
		}
	};

	public static Set<Stock> parseStockListing(InputStream inputStream){
		if(inputStream == null) return Collections.<Stock>emptySet();
		try {
			BufferedReader source = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
			return source
				.lines()
				.skip(1)
				.map(STOCK_PARSER)
				.collect(Collectors.toSet());
		}
		catch(Exception e){
			throw new RuntimeException("Could not parse listing from input stream", e);
		}
	}

	public static String[] split(String line, Pattern pattern){
		Matcher m = pattern.matcher(line);
		List<String> list = new LinkedList<>();
		while(m.find()){
			list.add(m.group());
		}
		String[] attrs = new String[list.size()];
		list.toArray(attrs);
		return attrs;
	}

	public static BigDecimal parseDecimal(String maybeNumber, BigDecimal defaultValue){
		try{
			Matcher m = DECIMAL_REGEX.matcher(maybeNumber);
			if(m.find()){
				String maybeDecimal = m.group().trim();
				if("-".equals(maybeDecimal)) return BigDecimal.ZERO;
				else if(maybeDecimal.endsWith("%")) 
					return new BigDecimal(maybeDecimal.substring(0,maybeDecimal.length()-1)).divide(new BigDecimal(100));
				else return new BigDecimal(maybeDecimal);
			} else {
				return defaultValue;
			}
		}
		catch(NumberFormatException e){
			return defaultValue;
		}
	}
	
	private ParseUtils(){}

}