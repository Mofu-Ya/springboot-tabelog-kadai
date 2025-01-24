package com.example.samuraitabelog.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "holidays")
@Data
public class Holiday {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
	
	@Column(name = "day_of_week_id")
    private Integer dayOfWeekId;
	
	@ManyToOne
    @JoinColumn(name = "holiday_type_id")
    private HolidayType holidayType;
	
	@Column(name = "specific_date")
	private LocalDate specificDate;
	
}
