package com.example.samuraitabelog.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
import com.example.samuraitabelog.repository.dto.ReservationDTO;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.HolidayService;
import com.example.samuraitabelog.service.ReservationService;


@Controller
public class ReservationController {
	private final ReservationRepository reservationRepository;
	private final RestaurantRepository restaurantRepository;
	private final ReservationService reservationService;
	private final HolidayRepository holidayRepository;
	private final HolidayService holidayService;	
	
	public ReservationController(ReservationRepository reservationRepository, 
			RestaurantRepository restaurantRepository, ReservationService reservationService, 
			HolidayRepository holidayRepository, HolidayService holidayService) {
		this.reservationRepository = reservationRepository;
		this.restaurantRepository = restaurantRepository;
		this.holidayRepository = holidayRepository;
		this.reservationService = reservationService;
		this.holidayService = holidayService;
	}
	
	@GetMapping("/reservations")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
		User user = userDetailsImpl.getUser();
//		Page<Reservation> reservationPage = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		
		Page<ReservationDTO> reservationPage = reservationService.getReservationsWithStatus(user, pageable);
		
		model.addAttribute("reservationPage", reservationPage);
		
		return "reservations/index";
	}
	
	@GetMapping("/restaurants/{id}/reservations/register")
	public String resgister(@PathVariable(name = "id") Integer id, Model model) {
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm();
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(restaurant.getId());
		List<Integer> disabledDays = holidays				
					.stream()
					.map(Holiday::getDayOfWeekId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		List<String> weeklyHolidayNames = new ArrayList<String>();
		
		if(holidays != null) {			
			weeklyHolidayNames = holidayService.getWeeklyHolidayNames(holidays);
		}
		 
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("disabledDays", disabledDays);
		model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
			
		return "reservations/register";
	}
	
	@PostMapping("/restaurants/{id}/reservations/register")
	public String resgister(@PathVariable(name = "id") Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			@ModelAttribute @Validated ReservationRegisterForm reservationRegisterForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {		
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(restaurant.getId());
		List<Integer> disabledDays = holidays				
					.stream()
					.map(Holiday::getDayOfWeekId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		List<String> weeklyHolidayNames = new ArrayList<String>();
		
		if(holidays != null) {			
			weeklyHolidayNames = holidayService.getWeeklyHolidayNames(holidays);
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("reservationRegisterForm", reservationRegisterForm);
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("disabledDays", disabledDays);
			model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			return "reservations/register";
		}	
		
		// 予約時間のエラーチェック
		List<ValidateReserveError> errors = reservationService.validateReservation(
        		restaurant, reservationRegisterForm.getBookingDate(), reservationRegisterForm.getBookingTime(),
        		reservationRegisterForm.getIsCrossDay(), reservationRegisterForm.getStayTime());
        
        // エラーがあればBindingResultに追加
        for (ValidateReserveError error : errors) {
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), error.getField(), error.getMessage()));
        }        
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("reservationRegisterForm", reservationRegisterForm);
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("disabledDays", disabledDays);
			model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			return "reservations/register";
		}		
			
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		model.addAttribute("restaurant", restaurant);
		
		return "reservations/confirm";
	}
	
	@PostMapping("/restaurants/{id}/reservations/create")
	public String create(@PathVariable(name = "id") Integer id, 			
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@ModelAttribute ReservationRegisterForm reservationRegisterForm,
						RedirectAttributes redirectAttributes,
						Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		User user = userDetailsImpl.getUser();
		
		reservationService.create(user, restaurant, reservationRegisterForm);
		
		redirectAttributes.addFlashAttribute("successMessage", "予約を完了しました。");    
        
        return "redirect:/restaurants/{id}";		
    } 
	
	@GetMapping("/reservations/{reservation_id}/edit")
	public String edit(@PathVariable(name = "reservation_id") Integer reservationId, Model model) {
		Reservation reservation = reservationRepository.getReferenceById(reservationId);
		Restaurant restaurant = reservation.getRestaurant();
		
		// 滞在時間を計算 (1時間単位)
		// 日をまたぐ場合、途中計算で24時間を超えることもあるので秒単位で計算する
        int startInSeconds = reservation.getStartTime().toSecondOfDay();
        int endInSeconds = reservation.getEndTime().toSecondOfDay();
        int durationInSeconds;
        		
		boolean isCrossDay = reservation.getEndTime().isBefore(reservation.getStartTime());
        if (isCrossDay) {
            // 日をまたぐ場合、終了時間に24時間を加算
        	durationInSeconds = (endInSeconds + 24 * 3600) - startInSeconds;        	
        } else {
            // 日をまたがない場合、そのまま計算
        	durationInSeconds = endInSeconds - startInSeconds;        	
        }
	    	
        // 秒を時間に変換
        int durationInHours = durationInSeconds / 3600;
        
	    ReservationEditForm reservationEditForm = new ReservationEditForm(reservation.getId(), 
				reservation.getBookingDate(), reservation.getStartTime(), durationInHours, isCrossDay, reservation.getNumberOfPeople());
		model.addAttribute("reservationEditForm", reservationEditForm);

		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(restaurant.getId());
		List<Integer> disabledDays = holidays				
					.stream()
					.map(Holiday::getDayOfWeekId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		List<String> weeklyHolidayNames = new ArrayList<String>();
		
		if(holidays != null) {			
			weeklyHolidayNames = holidayService.getWeeklyHolidayNames(holidays);
		}
		 
		model.addAttribute("reservation", reservation);
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("disabledDays", disabledDays);
		model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
			
		return "reservations/edit";
	}
	
	@PostMapping("/reservations/{reservation_id}/update")
	public String update(@PathVariable(name = "reservation_id") Integer reservationId, 			
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@ModelAttribute @Validated ReservationEditForm reservationEditForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						Model model) {
		
		Reservation reservation = reservationRepository.getReferenceById(reservationId);
		Restaurant restaurant = reservation.getRestaurant();
		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(restaurant.getId());
		List<Integer> disabledDays = holidays				
					.stream()
					.map(Holiday::getDayOfWeekId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		List<String> weeklyHolidayNames = new ArrayList<String>();
		
		if(holidays != null) {			
			weeklyHolidayNames = holidayService.getWeeklyHolidayNames(holidays);
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("reservationEditForm", reservationEditForm);
			model.addAttribute("reservation", reservation);
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("disabledDays", disabledDays);
			model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			return "reservations/edit";
		}
		
		// 予約時間のエラーチェック
        List<ValidateReserveError> errors = reservationService.validateReservation(
        		restaurant, reservationEditForm.getBookingDate(), reservationEditForm.getBookingTime(),
        		reservationEditForm.getIsCrossDay(), reservationEditForm.getStayTime());
        
        // エラーがあればBindingResultに追加
        for (ValidateReserveError error : errors) {
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), error.getField(), error.getMessage()));
        }
        
        if (bindingResult.hasErrors()) {
			model.addAttribute("reservationEditForm", reservationEditForm);
			model.addAttribute("reservation", reservation);
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("disabledDays", disabledDays);
			model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			return "reservations/edit";
		}	
		
		reservationService.update(reservationEditForm);
		
		redirectAttributes.addFlashAttribute("successMessage", "予約の変更を受付けました。");    
        
        return "redirect:/reservations";
	}
	
	@PostMapping("/reservations/{reservation_id}/delete")
	public String delete(@PathVariable(name = "reservation_id") Integer reservationId, RedirectAttributes redirectAttributes) {
		reservationRepository.deleteById(reservationId);
		
		redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");
		
		return "redirect:/reservations";
	}
	
}

