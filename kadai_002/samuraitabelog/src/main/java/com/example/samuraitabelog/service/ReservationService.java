package com.example.samuraitabelog.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.common.ValidateReserveError;
import com.example.samuraitabelog.entity.Holiday;
import com.example.samuraitabelog.entity.Reservation;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.ReservationEditForm;
import com.example.samuraitabelog.form.ReservationRegisterForm;
import com.example.samuraitabelog.repository.HolidayRepository;
import com.example.samuraitabelog.repository.ReservationRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.repository.dto.ReservationDTO;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final RestaurantRepository RestaurantRepository;
	private final UserRepository userRepository; 
	private final HolidayRepository holidayRepository; 
	
	public ReservationService(ReservationRepository reservationRepository, RestaurantRepository RestaurantRepository, 
			UserRepository userRepository, HolidayRepository holidayRepository ) {
		this.reservationRepository = reservationRepository;
		this.RestaurantRepository = RestaurantRepository;
		this.userRepository = userRepository;
		this.holidayRepository = holidayRepository;
	}
	
	@Transactional
	public void create(User user, Restaurant restaurant, ReservationRegisterForm reservationRegisterForm) {
		Reservation reservation = new Reservation();
		  
		reservation.setRestaurant(restaurant);
		reservation.setUser(user);
		reservation.setBookingDate(reservationRegisterForm.getBookingDate());
		reservation.setStartTime(reservationRegisterForm.getBookingTime());
		reservation.setEndTime(reservationRegisterForm.getBookingTime().plusHours(reservationRegisterForm.getStayTime()));
		reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
		
		reservationRepository.save(reservation);				
	}
	
	@Transactional
	public void update(ReservationEditForm reservationEditForm) {
		Reservation reservation = reservationRepository.getReferenceById(reservationEditForm.getId());
		  
		reservation.setBookingDate(reservationEditForm.getBookingDate());
		reservation.setStartTime(reservationEditForm.getBookingTime());
		reservation.setEndTime(reservationEditForm.getBookingTime().plusHours(reservationEditForm.getStayTime()));
		reservation.setNumberOfPeople(reservationEditForm.getNumberOfPeople());
		
		reservationRepository.save(reservation);				
	}
	
	public List<ValidateReserveError> validateReservation(Restaurant restaurant, LocalDate bookingDate, LocalTime bookingTime, boolean isCrossDay, Integer stayTime) {
		List<ValidateReserveError> errors = new ArrayList<>();
		
		boolean isBusinessTimeCrossDay = restaurant.getClosingTime().isBefore(restaurant.getOpeningTime());
		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(restaurant.getId());
		List<Integer> disabledDays = holidays				
					.stream()
					.map(Holiday::getDayOfWeekId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
				
		LocalTime endTime = bookingTime.plusHours(stayTime);; 
		
		// 定休日にかかっているかチェック
		boolean isClosed = disabledDays.contains(bookingDate.plusDays(1).getDayOfWeek().getValue() % 7);
       
		// 日またぎ不正チェック
		boolean isJustMidnight = false;
		
		if (endTime.equals(LocalTime.MIDNIGHT)) {
			isJustMidnight =  true; 
	    }
       
		if((!isCrossDay && endTime.isBefore(bookingTime))  				
				|| (isCrossDay && endTime.isAfter(bookingTime))
				|| (!isCrossDay && isJustMidnight)) {
			errors.add(new ValidateReserveError("isCrossDay", "日またぎを正しく設定してください。"));			
			isCrossDay = !isCrossDay;
		} 	
		
		// 入店時間が営業時間内かチェック
//		if(!isWithinRange(bookingTime, restaurant.getOpeningTime(), restaurant.getClosingTime())) {
//			
//		}
//		if(bookingTime.isBefore(restaurant.getOpeningTime())) {
//			errors.add(new ValidateReserveError("bookingTime", "予約時間が営業時間外です。"));				
//		}
    		
		// 予約時間が営業時間内かチェック
		// 営業時間が日をまたぐかどうかで分岐
		if(isBusinessTimeCrossDay) {
			boolean valid = 
	            (isWithinRange(bookingTime, restaurant.getOpeningTime(), LocalTime.MAX) || 
	            		(!isCrossDay && isWithinRange(bookingTime, LocalTime.MIN, restaurant.getClosingTime())) &&
	            ((!isCrossDay && isWithinRange(endTime, restaurant.getOpeningTime(), LocalTime.MAX)) || 
	            		(isCrossDay && isWithinRange(endTime, LocalTime.MIN, restaurant.getClosingTime())) ||
	            		(!isCrossDay && isWithinRange(endTime, LocalTime.MIN, restaurant.getClosingTime()))));
	        if (!valid) {
	        	errors.add(new ValidateReserveError("bookingTime", "予約時間が営業時間外です。"));
	        }
	        
		} else {
			if (!isWithinRange(bookingTime, restaurant.getOpeningTime(), restaurant.getClosingTime()) ||
		            !isWithinRange(endTime, restaurant.getOpeningTime(), restaurant.getClosingTime())) {
				errors.add(new ValidateReserveError("bookingTime", "予約時間が営業時間外です。"));
			}			
		}
		
       // 翌日が定休日になるチェック(日をまたぐ場合のみ)
       if (isCrossDay && isClosed) {
    	   errors.add(new ValidateReserveError("bookingTime", "予約時間が翌日の休業日にかかっています。"));	       	
       }
       
       return errors;
	}	

	// 指定時間が開始時間と終了時間の範囲にあるかのチェック
	private boolean isWithinRange(LocalTime time, LocalTime start, LocalTime end) {
	    return !time.isBefore(start) && !time.isAfter(end);
	}
	
	public Page<ReservationDTO> getReservationsWithStatus(User user, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return reservations.map(reservation -> {
            String status = reservation.getBookingDate().isBefore(LocalDate.now()) 
                            ? "予約終了" 
                            : "予約中";

            return new ReservationDTO(
            	reservation.getRestaurant(),
                reservation.getId(),
                reservation.getBookingDate(),
                reservation.getStartTime(),
                reservation.getNumberOfPeople(),
                status
            );
        });
    }

}

