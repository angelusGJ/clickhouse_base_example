package com.autentia.training.springboot.statistics.airlines.model;

public class AirlineFlightsNumber {

	private final String airline;
	private final long flights;

	public AirlineFlightsNumber(String airline, long flights) {
		this.airline = airline;
		this.flights = flights;
	}

	public String getAirline() {
		return airline;
	}

	public long getFlights() {
		return flights;
	}

}
