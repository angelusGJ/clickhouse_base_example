package com.autentia.training.springboot.statistics.airlines.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autentia.training.springboot.statistics.airlines.model.AirlineFlightsNumber;
import com.autentia.training.springboot.statistics.airlines.repository.StatisticsAirlinesRepository;

@Service
public class StatisticsAirlinesService {

	private StatisticsAirlinesRepository statisticsRepository;

	public StatisticsAirlinesService (StatisticsAirlinesRepository statisticsRepository) {
		this.statisticsRepository = statisticsRepository;
		
	}
	
	public List<AirlineFlightsNumber> getFlightsByYear(int year) {
		return statisticsRepository.getFlightsByYear(year);
	}

}
