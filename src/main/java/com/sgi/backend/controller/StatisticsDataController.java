package com.sgi.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sgi.backend.entity.StatisticsData;
import com.sgi.backend.services.IStatisticsDataService;

@RestController
@RequestMapping("/api")
public class StatisticsDataController {

	@Autowired
	private IStatisticsDataService statisticsDataService;
	
	@Scheduled(fixedRate = 10000)
	@GetMapping("/statisticsData")
	public void getAllStatisticsData() {
		 statisticsDataService.getFileExcel();
	}
	
}
