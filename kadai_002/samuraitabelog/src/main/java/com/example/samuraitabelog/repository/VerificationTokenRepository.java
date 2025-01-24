package com.example.samuraitabelog.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository< VerificationToken, Integer > {
	public VerificationToken findByToken(String token);
	public VerificationToken findByUser(User user);
	
}
