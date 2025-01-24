package com.example.samuraitabelog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.samuraitabelog.entity.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Integer>{
	public List<Holiday> findAllByRestaurantId(Integer restaurantId);
	
	// 編集前の曜日IDリストを取得
	@Query("SELECT h.dayOfWeekId FROM Holiday h WHERE h.restaurant.id = :restaurantId")
    List<Integer> findDayOfWeekIdsByRestaurantId(@Param("restaurantId") Integer restaurantId);
	
	// 特定の曜日を削除
	@Modifying
	@Query("DELETE FROM Holiday h WHERE h.restaurant.id = :restaurantId AND h.dayOfWeekId = :dayOfWeekId")
	void deleteByRestaurantIdAndDayOfWeekId(@Param("restaurantId") Integer restaurantId, @Param("dayOfWeekId") Integer dayOfWeekId);    
}
