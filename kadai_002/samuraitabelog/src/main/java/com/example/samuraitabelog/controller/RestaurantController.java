package com.example.samuraitabelog.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitabelog.entity.Category;
import com.example.samuraitabelog.entity.Holiday;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.Review;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.RestaurantSearchForm;
import com.example.samuraitabelog.repository.CategoryRepository;
import com.example.samuraitabelog.repository.HolidayRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.repository.ReviewRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.FavoriteService;
import com.example.samuraitabelog.service.HolidayService;
import com.example.samuraitabelog.service.ReviewService;

@Controller
@RequestMapping("/restaurants")
public class RestaurantController {
	private final RestaurantRepository restaurantRepository;
	private final CategoryRepository categoryRepository;
	private final HolidayRepository holidayRepository;
	private final ReviewRepository reviewRepository;
	private final HolidayService holidayService;
	private final UserRepository userRepository;
	private final ReviewService reviewService;
	private final FavoriteService favoriteService;
	
	public RestaurantController(RestaurantRepository restaurantRepository, 
			CategoryRepository categoryRepository, HolidayRepository holidayRepository, 
			HolidayService holidayService, ReviewRepository reviewRepository, 
			UserRepository userRepository, ReviewService reviewService,
			FavoriteService favoriteService) {
		this.restaurantRepository = restaurantRepository;
		this.categoryRepository = categoryRepository;
		this.holidayRepository = holidayRepository;
		this.reviewRepository = reviewRepository;
		this.userRepository = userRepository;
		this.holidayService = holidayService;
		this.reviewService = reviewService;
		this.favoriteService = favoriteService;
	}
	
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
						@RequestParam(name = "categoryId", required = false) Integer categoryId,
						@RequestParam(name = "price", required = false) Integer price,
						@RequestParam(name = "order", required = false) String order,
						@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
						Model model)
	{
		Page<Restaurant> restaurantPage;
		Category category = null;
		model.addAttribute("restaurantSearchForm", new RestaurantSearchForm(keyword, categoryId, price, order));
		
		if(categoryId != null) {
			category = categoryRepository.getReferenceById(categoryId);
		}
		
		List<Category> categories = categoryRepository.findAll(); 
		
		if (keyword != null && !keyword.isEmpty()) {					
			if (order != null && order.equals("priceAsc")) {
				restaurantPage = restaurantRepository.findByNameLikeOrderByLowestPriceAsc("%" + keyword + "%", pageable);
			} else if (order != null && order.equals("priceDesc")) {
				restaurantPage = restaurantRepository.findByNameLikeOrderByHighestPriceDesc("%" + keyword + "%", pageable);
			} else {
				restaurantPage = restaurantRepository.findByNameLikeOrderByCreatedAtDesc("%" + keyword + "%", pageable);
			}
		} else if (categoryId != null) {						
			if (order != null && order.equals("priceAsc")) {
				restaurantPage = restaurantRepository.findByCategoryOrderByLowestPriceAsc(category, pageable);
			} else if (order != null && order.equals("priceDesc")) {
				restaurantPage = restaurantRepository.findByCategoryOrderByHighestPriceDesc(category, pageable);
			} else {
				restaurantPage = restaurantRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
			}
		} else {			
			if (order != null && order.equals("priceAsc")) {
				restaurantPage = restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
			} else if (order != null && order.equals("priceDesc")) {
				restaurantPage = restaurantRepository.findAllByOrderByHighestPriceDesc(pageable);
			} else {
				restaurantPage = restaurantRepository.findAllByOrderByCreatedAtDesc(pageable); 
			}
		}
		
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("price", price);
		model.addAttribute("order", order);
		model.addAttribute("categories", categories);
		
		return "restaurants/index";
	}
	
	@PostMapping
	public String index(@ModelAttribute RestaurantSearchForm restaurantSearchForm,
						@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
						Model model)
	{
		Page<Restaurant> restaurantPage;
		Category category = null;
		Integer categoryId = restaurantSearchForm.getCategoryId();
		String keyword = restaurantSearchForm.getKeyword();
		String order = restaurantSearchForm.getOrder();
		Integer price = restaurantSearchForm.getPrice();
		
		if (keyword != null && keyword.trim().isEmpty()) {
			keyword = null; // 空文字列を null に変換
	    }
		
		if(order == null) {
			order = "createdAtDesc";
		}
		
		if(categoryId == null && keyword == null && price == null) {
			if (order != null && order.equals("priceAsc")) {
				restaurantPage = restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
			} else if(order != null && order.equals("priceDesc")) {
				restaurantPage = restaurantRepository.findAllByOrderByHighestPriceDesc(pageable);
			}
			else {
				restaurantPage = restaurantRepository.findAllByOrderByCreatedAtDesc(pageable); 
			}
		} else {
			restaurantPage = restaurantRepository.searchRestaurantWithCondition(
					keyword, categoryId, price, order, pageable);
		} 
				        
		if(categoryId != null) {
			category = categoryRepository.getReferenceById(categoryId);
		}
		
		List<Category> categories = categoryRepository.findAll(); 
		
//		if((keyword != null && !keyword.isEmpty()) && (categoryId != null) && (price != null)) {
//			
//		} else if ((keyword != null && !keyword.isEmpty()) && (categoryId != null)) {					
//			if (order != null && order.equals("priceAsc")) {
//				restaurantPage = restaurantRepository.findByNameLikeOrderByLowestPriceAsc("%" + keyword + "%", pageable);
//			} else if (order != null && order.equals("priceDesc")){
//				restaurantPage = restaurantRepository.findByNameLikeOrderByHighestPriceDesc("%" + keyword + "%", pageable);
//			} else {
//				restaurantPage = restaurantRepository.findByNameLikeOrderByCreatedAtDesc("%" + keyword + "%", pageable);
//			}
//		} else if ((keyword != null && !keyword.isEmpty()) && (price != null)) {
//		
//		} else if((categoryId != null) && (price != null)) {
//			
//		} else if (keyword != null && !keyword.isEmpty()) {
//			
//		} else if (categoryId != null) {						
//			if (order != null && order.equals("priceAsc")) {
//				restaurantPage = restaurantRepository.findByCategoryOrderByLowestPriceAsc(category, pageable);
//			} else if (order != null && order.equals("priceDesc")){
//				restaurantPage = restaurantRepository.findByCategoryOrderByHighestPriceDesc(category, pageable);
//			}else {
//				restaurantPage = restaurantRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
//			}
//		
////		} else if (price != null) {			
////			if (order != null && order.equals("priceAsc")) {
////				restaurantPage = restaurantRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
////			} else {
////				restaurantPage = restaurantRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
////			}
//		} else if (price != null) {
//			
//		} else {			
//			if (order != null && order.equals("priceAsc")) {
//				restaurantPage = restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
//			} else {
//				restaurantPage = restaurantRepository.findAllByOrderByCreatedAtDesc(pageable); 
//			}
//		}
		
		model.addAttribute("restaurantSearchForm", restaurantSearchForm);
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("price", price);
		model.addAttribute("order", order);
		model.addAttribute("categories", categories);
		
		return "restaurants/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		
		List<Holiday> holidays = holidayRepository.findAllByRestaurantId(id);
		List<String> weeklyHolidayNames = new ArrayList<String>();
		List<Review> reviews = reviewRepository.findTop10ByRestaurantOrderByCreatedAtDesc(restaurant);
		boolean isFavorited = false;
		
		if(holidays == null) {
			holidays = new ArrayList<Holiday>();			
		} else {
			weeklyHolidayNames = holidayService.getWeeklyHolidayNames(holidays);
		}
		
		User user;
		boolean hasReviewed;
		if(userDetailsImpl != null) {
			// ログイン済ユーザーの場合
			user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
			model.addAttribute("currentUser", user);
			hasReviewed = reviewService.hasUserReviewedRestaurant(restaurant, user);
			isFavorited = favoriteService.isFavorited(user, id);
		} else {
			// 未ログインユーザーの場合
			model.addAttribute("currentUser", "");
			hasReviewed = false;
		}
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("weeklyHolidayNames", weeklyHolidayNames);
		model.addAttribute("reviews", reviews);
		model.addAttribute("hasReviewed", hasReviewed);
		model.addAttribute("isFavorited", isFavorited);
		
		return "restaurants/show";
	}
	
	// レビュー一覧を表示
	@GetMapping("/{id}/reviews")
	public String index(@PathVariable(name = "id") Integer restaurantId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
		Page<Review> reviews = reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
		User user;
		boolean hasReviewed;
		if(userDetailsImpl != null) {
			// ログイン済ユーザーの場合
			user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
			model.addAttribute("currentUser", user);
			hasReviewed = reviewService.hasUserReviewedRestaurant(restaurant, user);
		} else {
			// 未ログインユーザーの場合
			model.addAttribute("currentUser", "");
			hasReviewed = false;
		}
		
		Page<Review> reviewPage;
		reviewPage = reviewRepository.findAll(pageable);
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("reviews", reviews);
		model.addAttribute("hasReviewed", hasReviewed);
		model.addAttribute("reviewPage", reviewPage); 
		return "restaurants/reviews";
	}
	
}

