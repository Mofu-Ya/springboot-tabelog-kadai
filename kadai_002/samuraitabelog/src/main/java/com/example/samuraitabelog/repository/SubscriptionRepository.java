package com.example.samuraitabelog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.SubscriptionEntity;
import com.example.samuraitabelog.entity.User;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Integer>{
	public List<SubscriptionEntity> findAllByUserOrderByUpdatedAtDesc(User user);
	public List<SubscriptionEntity> findAllByUserAndCustomerIdAndSubscriptionId(User user, String customerId, String subscriptionId);
	public List<SubscriptionEntity> findAllByCustomerIdAndSubscriptionId(String customerId, String subscriptionId);
	public List<SubscriptionEntity> findAllByUserAndPriceIdOrderByUpdatedAtDesc(User user, String priceId);
}
