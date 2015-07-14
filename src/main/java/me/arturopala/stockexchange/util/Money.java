package me.arturopala.stockexchange.util;

import java.math.*;
import java.text.*;
import java.util.Locale;

public final class Money {

	public static final Money ZERO = new Money(BigDecimal.ZERO);
	public static final Money UNDEFINED = new Money();

	private final BigDecimal value;

	public Money(int value){
		this(new BigDecimal(value));
	}

	public Money(BigDecimal value){
		if(value==null) throw new IllegalArgumentException("Money value cannot be null!");
		if(value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Money value cannot be lower than zero!");
		this.value = value.setScale(DIGITS, RoundingMode.HALF_UP);
	}

	private Money(){
		this.value = null;
	}

	public boolean isDefined(){
		return this!=UNDEFINED && this.value.compareTo(BigDecimal.ZERO)>0;
	}

	public Money multiply(int value){
		return multiply(new BigDecimal(value));
	}

	public Money multiply(BigDecimal value){
		if(this==UNDEFINED || value==null) return UNDEFINED;
		if(value.equals(BigDecimal.ZERO)) return ZERO;
		return new Money(this.value.multiply(value));
	}

	public Money divide(BigDecimal value){
		if(this==UNDEFINED || value == null || value.equals(BigDecimal.ZERO)) return UNDEFINED;
		return new Money(this.value.divide(value, MathContext.DECIMAL32));
	}

	public Money divide(int value){
		return divide(new BigDecimal(value));
	}

	public Double divide(Money that){
		if(this==UNDEFINED || that==UNDEFINED) return Double.NaN;
		if(that.equals(ZERO)) return Double.NaN;
		else {
			BigDecimal result = this.value.divide(that.value,MathContext.DECIMAL32);
			if(result.scale()>8) return result.setScale(8,RoundingMode.HALF_UP).doubleValue();
			else return result.doubleValue();
		}
	}

	public Money add(Money that){
		if(this==UNDEFINED || that==UNDEFINED) return UNDEFINED;
		else return new Money(this.value.add(that.value));
	}

	public Money subtract(Money that){
		if(this==UNDEFINED || that==UNDEFINED) return UNDEFINED;
		else return new Money(this.value.subtract(that.value));
	}

	public double doubleValue(){
		if(this==UNDEFINED) return Double.NaN;
		else return value.doubleValue();
	}

	public static Money parse(String maybeMoney){
		try {
			return new Money((BigDecimal) FORMAT.parse(maybeMoney.trim()));
		} catch (Exception e){
			return ZERO;
		}
	}

	@Override
	public String toString(){
		if(this==UNDEFINED) return "X";
		else if(this.equals(ZERO)) return "-";
		else return FORMAT.format(value);
	}

	@Override
	public int hashCode(){
		return value.hashCode();
	}

	@Override 
	public boolean equals(Object that){
		if(that != null && that instanceof Money){
			return ((Money)that).value.equals(this.value);
		} else {
			return false;
		}
	}

	private static final int DIGITS = 4; 
	public static final DecimalFormat FORMAT = new DecimalFormat("0.####", 
		new DecimalFormatSymbols(Locale.forLanguageTag("US_us")));

	static {
		FORMAT.setParseBigDecimal(true);
	}

	public static Money random(int max){
		return new Money((int) Math.round(Math.random() * max + 0.0001));
	}

	public static Money random(Money max){
		return new Money((int) Math.round(Math.random() * max.doubleValue() + 0.0001));
	}

}