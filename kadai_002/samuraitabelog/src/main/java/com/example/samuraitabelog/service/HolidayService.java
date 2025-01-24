package com.example.samuraitabelog.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.Holiday;
import com.example.samuraitabelog.entity.HolidayType;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.enums.DayOfWeek;
import com.example.samuraitabelog.form.RestaurantEditForm;
import com.example.samuraitabelog.form.RestaurantRegisterForm;
import com.example.samuraitabelog.repository.HolidayRepository;
import com.example.samuraitabelog.repository.HolidayTypeRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;

@Service
public class HolidayService {
	private final RestaurantRepository restaurantRepository;
	private final HolidayRepository holidayRepository;
	private final HolidayTypeRepository holidayTypeRepository;
	
	public HolidayService(RestaurantRepository restaurantRepository, HolidayRepository holidayRepository, HolidayTypeRepository holidayTypeRepository) {
		this.restaurantRepository = restaurantRepository;
		this.holidayRepository = holidayRepository;
		this.holidayTypeRepository = holidayTypeRepository;
	}
	
	@Transactional
	public void create(RestaurantRegisterForm restaurantRegisterForm, Restaurant restaurant) {
		List<Integer> weeklyHolidayIds = restaurantRegisterForm.getWeeklyHolidayIds();
		
		// 曜日固定の定休日がある場合
		if(weeklyHolidayIds !=  null) {
			for (Integer dayId : restaurantRegisterForm.getWeeklyHolidayIds()) {
	            DayOfWeek dayOfWeek = DayOfWeek.fromId(dayId);
	            HolidayType holidayType = holidayTypeRepository.findByName("定休日");
	            Holiday holiday = new Holiday();
	            holiday.setRestaurant(restaurant);  
	            holiday.setDayOfWeekId(dayId); 
	            holiday.setHolidayType(holidayType);
	            holidayRepository.save(holiday);
	        }
		}
		
	}
	
	@Transactional
	public void update(RestaurantEditForm restaurantEditForm, Restaurant restaurant) {
		List<Integer> weeklyHolidayIds = restaurantEditForm.getWeeklyHolidayIds();
		List<Holiday> savedHolidays = holidayRepository.findAllByRestaurantId(restaurant.getId());
		
		
	    // 編集前の曜日IDリストを取得
	    List<Integer> currentHolidays = holidayRepository.findDayOfWeekIdsByRestaurantId(restaurant.getId());

	    // 編集後の曜日IDリストを取得
	    List<Integer> updatedHolidays = restaurantEditForm.getWeeklyHolidayIds();
	    
	 // 差分計算
	    List<Integer> holidaysToAdd = updatedHolidays.stream()
	        .filter(id -> !currentHolidays.contains(id))
	        .collect(Collectors.toList());

	    List<Integer> holidaysToRemove = currentHolidays.stream()
	        .filter(id -> !updatedHolidays.contains(id))
	        .collect(Collectors.toList());
	    
	    // DB更新
	    updateHolidays(restaurant, holidaysToAdd, holidaysToRemove);

	    
		// 曜日固定の定休日がある場合
//		if(weeklyHolidayIds !=  null) {
//			for (Integer dayId : restaurantEditForm.getWeeklyHolidayIds()) {
//	            DayOfWeek dayOfWeek = DayOfWeek.fromId(dayId);
//	            HolidayType holidayType = holidayTypeRepository.findByName("定休日");
//	            Holiday holiday = new Holiday();
//	            holiday.setRestaurant(restaurant);  
//	            holiday.setDayOfWeekId(dayId); 
//	            holiday.setHolidayType(holidayType);
//	            holidayRepository.save(holiday);
//	        }
//		}
		
	}
	
	public List<String> getWeeklyHolidayNames(List<Holiday> holidays) {
				
		List<Integer> weeklyHolidayIds = holidays.stream()
	        	.map(Holiday::getDayOfWeekId) 
	        	.collect(Collectors.toList());
		
        return weeklyHolidayIds.stream()
                .map(DayOfWeek::getNameById)
                .collect(Collectors.toList());
    }
	
	// 定休日の更新処理
    public void updateHolidays(Restaurant restaurant, List<Integer> holidaysToAdd, List<Integer> holidaysToRemove) {
        // 追加処理
		HolidayType holidayType = holidayTypeRepository.findByName("定休日"); 
        for (Integer dayId : holidaysToAdd) {
            Holiday holiday = new Holiday();
//            HolidayType holidayType = holidayTypeRepository.findByName("定休日"); 
            holiday.setRestaurant(restaurant);
            holiday.setDayOfWeekId(dayId);                       
            holiday.setHolidayType(holidayType);
            holidayRepository.save(holiday);
        }

        // 削除処理
        for (Integer dayId : holidaysToRemove) {
            holidayRepository.deleteByRestaurantIdAndDayOfWeekId(restaurant.getId(), dayId);
        }
    }
}
