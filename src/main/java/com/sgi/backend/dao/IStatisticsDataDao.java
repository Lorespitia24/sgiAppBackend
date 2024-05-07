package com.sgi.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sgi.backend.entity.StatisticsData;


public interface IStatisticsDataDao extends JpaRepository<StatisticsData, Long> {
	
	
	@Query(value = "SELECT * FROM information.statistics_data ORDER BY quantity DESC", nativeQuery = true)
    List<StatisticsData> findByQuantityDesc();
}
