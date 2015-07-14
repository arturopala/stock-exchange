package me.arturopala.stockexchange.simpleimpl;

import java.time.Instant;

public class Tick {

	public final Instant timestamp;

	public Tick(Instant timestamp){
		this.timestamp = timestamp;
	}

}