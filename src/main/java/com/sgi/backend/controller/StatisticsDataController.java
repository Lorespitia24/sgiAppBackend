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

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class StatisticsDataController {

	@Autowired
	private IStatisticsDataService statisticsDataService;
	
	@Scheduled(fixedRate = 60000)
	public void getAllStatisticsData() {
		 statisticsDataService.getFileExcel();
	}
	
}
