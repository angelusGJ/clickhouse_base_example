package com.autentia.training.springboot.statistics.airlines.rest;

import java.util.List;

import javax.validation.constraints.Size;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autentia.training.springboot.statistics.airlines.model.AirlineFlightsNumber;
import com.autentia.training.springboot.statistics.airlines.service.StatisticsAirlinesService;

@RestController
@RequestMapping("/statistics/airlines")
public class StatisticsAirlinesController {

	private StatisticsAirlinesService statisticsService;

	public StatisticsAirlinesController(StatisticsAirlinesService statisticsService) {
		this.statisticsService = statisticsService;
		
	}
	
	@GetMapping("/flights")
	public List<AirlineFlightsNumber> getFlightsByYear(@Size(min=4, max= 4) @RequestParam("year") int year) {
		return statisticsService.getFlightsByYear(year);
	}
	
}
