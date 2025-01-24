package com.example.samuraitabelog.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.samuraitabelog.entity.Restaurant;

public class ReservationDTO {
	private Restaurant restaurant;
    private Integer id;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private Integer numberOfPeople;
    private String status;

    // コンストラクタ
    public ReservationDTO(Restaurant restaurant, Integer id, LocalDate bookingDate, LocalTime startTime, Integer numberOfPeople, String status) {
        this.restaurant = restaurant;
    	this.id = id;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.numberOfPeople = numberOfPeople;
        this.status = status;
    }

    // ゲッターとセッター
    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setBookingTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(Integer numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
