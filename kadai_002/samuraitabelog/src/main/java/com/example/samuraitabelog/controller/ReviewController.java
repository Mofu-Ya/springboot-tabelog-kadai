package com.example.samuraitabelog.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.Review;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.ReviewEditForm;
import com.example.samuraitabelog.form.ReviewRegisterForm;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.repository.ReviewRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.RestaurantService;
import com.example.samuraitabelog.service.ReviewService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final RestaurantService restaurantService;
	private final RestaurantRepository restaurantRepository;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;

	public ReviewController(ReviewService reviewService, RestaurantService restaurantService, RestaurantRepository restaurantRepository, ReviewRepository reviewRepository, UserRepository userRepository) {
		this.reviewService = reviewService;
		this.restaurantService = restaurantService;
		this.restaurantRepository = restaurantRepository;
		this.reviewRepository = reviewRepository;
		this.userRepository = userRepository;
	}

	// レビュー一覧を表示
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
		User user = userDetailsImpl.getUser();
		
		Page<Review> reviewPage = reviewRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		
		model.addAttribute("reviewPage", reviewPage);
		
		return "reviews/index";		
	}
	

	@GetMapping("/{restaurant_id}/register")
	public String register(@PathVariable(name = "restaurant_id") Integer restaurantId, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
		model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
		model.addAttribute("restaurant", restaurant);
		return "reviews/register";
		
	}
	
	@PostMapping("/{restaurant_id}/create")
    public String create(@PathVariable(name = "restaurant_id") Integer restaurantId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {        
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
		User user = userDetailsImpl.getUser();
		if (bindingResult.hasErrors()) {
			model.addAttribute("reviewRegisterForm", reviewRegisterForm);
			model.addAttribute("restaurant", restaurant);
            return "reviews/register";
        }

        reviewService.create(restaurant, user, reviewRegisterForm);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");    
        
        return "redirect:/restaurants/" + restaurantId;
    } 
	
	@GetMapping("/{review_id}/edit")
	public String edit(@PathVariable(name = "review_id") Integer reviewId, Model model) {
		Review review = reviewRepository.getReferenceById(reviewId);
		Restaurant restaurant = review.getRestaurant();
		
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), review.getScore(), review.getImpression());
		
		model.addAttribute("reviewEditForm", reviewEditForm);
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("review", review);
		return "reviews/edit";
		
	}
	
	@PostMapping("/{review_id}/update")
    public String update(@PathVariable(name = "review_id") Integer reviewId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @ModelAttribute @Validated ReviewEditForm reviewEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {        
		Review review = reviewRepository.getReferenceById(reviewId);
		Restaurant restaurant = review.getRestaurant();
		User user = userDetailsImpl.getUser();
		if (bindingResult.hasErrors()) {
			model.addAttribute("reviewEditForm", reviewEditForm);
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("review", review);
            return "reviews/edit";
        }

        reviewService.update(restaurant, user, reviewEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを更新しました。");    
        
        return "redirect:/restaurants/" + restaurant.getId();
    } 
	
	@PostMapping("/{review_id}/delete")
	public String delete(@PathVariable(name = "review_id") Integer reviewId, RedirectAttributes redirectAttributes) {
		Restaurant restaurant = reviewRepository.getReferenceById(reviewId).getRestaurant();
		reviewRepository.deleteById(reviewId);
		
		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
		
		return "redirect:/restaurants/" + restaurant.getId();
	}
}
