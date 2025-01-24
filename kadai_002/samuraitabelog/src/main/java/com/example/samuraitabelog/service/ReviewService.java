package com.example.samuraitabelog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.Review;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.ReviewEditForm;
import com.example.samuraitabelog.form.ReviewRegisterForm;
import com.example.samuraitabelog.repository.ReviewRepository;

@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;
	
	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}
	
	@Transactional
	public void create(Restaurant restaurant, User user, ReviewRegisterForm reviewRegisterForm) {
		Review review = new Review();
		
		review.setRestaurant(restaurant);
		review.setUser(user);
		review.setScore(reviewRegisterForm.getScore());
		review.setImpression(reviewRegisterForm.getImpression());
		
		reviewRepository.save(review);		
		
	}
	
	@Transactional
	public void update(Restaurant restaurant, User user, ReviewEditForm reviewEditForm) {
		Review review = reviewRepository.getReferenceById(reviewEditForm.getId());
				
		review.setRestaurant(restaurant);
		review.setUser(user);
		review.setScore(reviewEditForm.getScore());
		review.setImpression(reviewEditForm.getImpression());		
		
		reviewRepository.save(review);		
	}
	
	// ログインユーザーがその店舗にレビュー投稿済みかどうかをチェックする
	public boolean hasUserReviewedRestaurant(Restaurant restaurant, User user) {
		
		return reviewRepository.existsByRestaurantAndUser(restaurant, user);		
	}
}
