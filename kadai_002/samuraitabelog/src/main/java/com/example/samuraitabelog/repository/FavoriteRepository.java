package com.example.samuraitabelog.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.Favorite;
import com.example.samuraitabelog.entity.User;

public interface FavoriteRepository extends JpaRepository <Favorite, Integer>{
	public Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
	public boolean existsByUserAndRestaurantId(User user, Integer restaurantId);
	public Optional<Favorite> findByUserAndRestaurantId(User user, Integer restaurantId);
}

