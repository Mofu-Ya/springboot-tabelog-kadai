package com.example.samuraitabelog.controller;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.Category;
import com.example.samuraitabelog.entity.Holiday;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.enums.DayOfWeek;
import com.example.samuraitabelog.form.RestaurantEditForm;
import com.example.samuraitabelog.form.RestaurantRegisterForm;
import com.example.samuraitabelog.repository.CategoryRepository;
import com.example.samuraitabelog.repository.HolidayRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.service.HolidayService;
import com.example.samuraitabelog.service.RestaurantService;


@Controller
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {
	private final RestaurantRepository restaurantRepository;  
	private final RestaurantService restaurantService; 
	private final CategoryRepository categoryRepository; 
	private final HolidayRepository holidayRepository; 
	private final HolidayService holidayService; 
    
	public AdminRestaurantController(RestaurantRepository restaurantRepository, RestaurantService restaurantService, CategoryRepository categoryRepository, HolidayService holidayService, HolidayRepository holidayRepository) {
		this.restaurantRepository = restaurantRepository; 
		this.restaurantService = restaurantService; 
		this.categoryRepository = categoryRepository;
		this.holidayService = holidayService;
		this.holidayRepository = holidayRepository; 
	}	
  
	@GetMapping
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestParam(name = "keyword", required = false) String keyword) {
		Page<Restaurant> restaurantPage;      
      
		if (keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantRepository.findByNameLike("%" + keyword + "%", pageable);                
		} else {
			restaurantPage = restaurantRepository.findAll(pageable);
		} 
		
		model.addAttribute("restaurantPage", restaurantPage);   
		model.addAttribute("keyword", keyword);             
     
		return "admin/restaurants/index";
	} 
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(id);
		List<String> weeklyHolidayNames = new ArrayList<String>();
		
		if(holidays == null) {
			holidays = new ArrayList<Holiday>();			
		} else {
			weeklyHolidayNames = holidayService.getWeeklyHolidayNames(holidays);
		}
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
		
		return "admin/restaurants/show";
		
	}
	
	@GetMapping("/register")
	public String register(Model model) {
		List<Category> categories = categoryRepository.findAll(); 
		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		
		model.addAttribute("categories", categories);
		model.addAttribute("days", DayOfWeek.values());
		return "admin/restaurants/register";
		
	}
	
	@PostMapping("/create")
    public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {        
		LocalTime openingTime = restaurantRegisterForm.getOpeningTime();
		LocalTime closingTime = restaurantRegisterForm.getClosingTime();
		
		if (!restaurantRegisterForm.getCrossDay() && openingTime.isAfter(closingTime)) {
            bindingResult.rejectValue("closingTime", "error.closingTime",
                    "営業終了時刻は営業開始時刻より後の時間を指定してください");
        }

        if (restaurantRegisterForm.getCrossDay() && !openingTime.isAfter(closingTime)) {
            bindingResult.rejectValue("closingTime", "error.closingTime",
                    "日をまたぐ場合、営業終了時刻は営業開始時刻より前の時間を指定してください");
        }
		
		if (bindingResult.hasErrors()) {
        	List<Category> categories = categoryRepository.findAll();
        	model.addAttribute("restaurantRegisterForm", restaurantRegisterForm);
        	model.addAttribute("categories", categories);
        	model.addAttribute("days", DayOfWeek.values());
        	
            return "admin/restaurants/register";
        }
        
        restaurantService.create(restaurantRegisterForm);
        if(restaurantRegisterForm.getWeeklyHolidayIds() != null) {
        	
        }
        redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");    
        
        return "redirect:/admin/restaurants";
    }
	
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		String imageName = restaurant.getImageName();
		List<Category> categories = categoryRepository.findAll(); 
		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(id);
		List<Integer> weeklyHolidayIds = holidays.stream()
        	.map(Holiday::getDayOfWeekId) 
        	.collect(Collectors.toList());
		
		Boolean crossDay = false;
		if(restaurant.getOpeningTime().isAfter(restaurant.getClosingTime())) {
			crossDay = true;
		}
		
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getId(), restaurant.getName(), null, restaurant.getCategory().getId(), restaurant.getDescription(), restaurant.getLowestPrice(), restaurant.getHighestPrice(), restaurant.getOpeningTime(), restaurant.getClosingTime(), weeklyHolidayIds, crossDay, restaurant.getNumberOfSeats(), restaurant.getPostalCode(), restaurant.getAddress(), restaurant.getPhoneNumber());

		model.addAttribute("imageName",imageName);
		model.addAttribute("categories", categories);
		model.addAttribute("days", DayOfWeek.values());
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		
		return "admin/restaurants/edit";
		
	}
	
	@PostMapping("/{id}/update")
	public String update(@PathVariable(name = "id") Integer id, @ModelAttribute @Validated RestaurantEditForm restaurantEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		LocalTime openingTime = restaurantEditForm.getOpeningTime();
		LocalTime closingTime = restaurantEditForm.getClosingTime();
		String imageName = restaurant.getImageName();
		
		if (!restaurantEditForm.getCrossDay() && openingTime.isAfter(closingTime)) {
            bindingResult.rejectValue("closingTime", "error.closingTime",
                    "営業終了時刻は営業開始時刻より後の時間を指定してください");
        }

        if (restaurantEditForm.getCrossDay() && !openingTime.isAfter(closingTime)) {
            bindingResult.rejectValue("closingTime", "error.closingTime",
                    "日をまたぐ場合、営業終了時刻は営業開始時刻より前の時間を指定してください");
        }
        
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryRepository.findAll();
			model.addAttribute("imageName",imageName);
        	model.addAttribute("restaurantEditForm", restaurantEditForm);
        	model.addAttribute("categories", categories);
        	model.addAttribute("days", DayOfWeek.values());
            
            return "admin/restaurants/edit";
        }
		
		restaurantService.update(restaurantEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗情報を編集しました。");
		
		return "redirect:/admin/restaurants";
	}
	
	
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		restaurantRepository.deleteById(id);
		
		redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");
		
		return "redirect:/admin/restaurants";
	}
}
