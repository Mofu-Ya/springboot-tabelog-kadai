package com.example.samuraitabelog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.HolidayType;

public interface HolidayTypeRepository extends JpaRepository<HolidayType, Integer>{
	public HolidayType findByName(String name);
}
