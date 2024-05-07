package com.sgi.backend.services;

import java.util.List;

import com.sgi.backend.entity.StatisticsData;

public interface IStatisticsDataService {
	
	public List<StatisticsData> findAll();
	
	public StatisticsData findById(Long id);
	
	public StatisticsData save(StatisticsData statisticsData);
	
	public boolean delete(String nameFile);
	
	public void getFileExcel();

}
