package com.example.samuraitabelog.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.PlanType;
import com.example.samuraitabelog.entity.Role;
import com.example.samuraitabelog.entity.SubscriptionEntity;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.PlanTypeRepository;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.repository.SubscriptionRepository;
import com.example.samuraitabelog.repository.UserRepository;

@Service
public class SubscriptionService {
	private final UserRepository userRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final PlanTypeRepository planTypeRepository;
	private final RoleRepository roleRepository;
	
	public SubscriptionService(UserRepository userRepository, SubscriptionRepository subscriptionRepository, PlanTypeRepository planTypeRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.subscriptionRepository = subscriptionRepository;
		this.planTypeRepository = planTypeRepository;
		this.roleRepository = roleRepository;
	}
	
	@Transactional
	public void create(String subscriptionId, String customerId, String priceId, String invoiceStatus) {
		SubscriptionEntity subscriptionEntity = new SubscriptionEntity();	
		
		subscriptionEntity.setCustomerId(customerId);
		subscriptionEntity.setSubscriptionId(subscriptionId);
		if(priceId != null) {
			subscriptionEntity.setPriceId(priceId);
		}	
		
		if(invoiceStatus != null) {
			subscriptionEntity.setInvoiceStatus(invoiceStatus);
		}
		subscriptionEntity.setEnabled(true);
		
		subscriptionRepository.save(subscriptionEntity);	
	}
	
	@Transactional
	public void upgrade(Integer id, Integer userId) {
		SubscriptionEntity subscriptionEntity = subscriptionRepository.getReferenceById(id);
		User user = userRepository.getReferenceById(userId);
//		String priceId = subscriptionEntity.getPriceId();
		
		// priceId で PlanType を取得したかったが、webhook の順番次第で null の時がある
		// name で取得に変更 (要再検討)
//		PlanType planType = planTypeRepository.findByPriceId(priceId);
		PlanType planType = planTypeRepository.findByName("ベーシック");
		
		// 登録されている price ID と一致すれば有料プランへアップグレード
		if(planType != null) {
			// userId はサブスク作成処理の一番最後のイベントで取得できる
			subscriptionEntity.setUser(user);
			subscriptionRepository.save(subscriptionEntity);
			
			Role role = roleRepository.findByName("ROLE_GENERAL_PAID");
			user.setPlanType(planType);
			user.setRole(role);				
			userRepository.save(user);
		}				
	}
	
	@Transactional
	public void subscriptionUpdate(Integer id, String priceId, String status) {
		SubscriptionEntity subscriptionEntity = subscriptionRepository.getReferenceById(id);
		
		if(priceId != null && !priceId.equals(subscriptionEntity.getPriceId())) {
			// Price ID が違う場合は、Stripe上の商品が変更された可能性がある
			// 現状は Price ID を上書きとする
			subscriptionEntity.setPriceId(priceId);
		} else if(subscriptionEntity.getPriceId() == null) {
			// サブスクリプション登録時はイベントが前後して、Price ID がセットできていないこともある
			// なので、null の場合もセットする
			subscriptionEntity.setPriceId(priceId);
		}
		
		subscriptionEntity.setSubscriptionStatus(status);
		subscriptionRepository.save(subscriptionEntity);
	}
			
	@Transactional
	public void paymentUpdate(Integer id, String status, LocalDateTime paidAt) {
		SubscriptionEntity subscriptionEntity = subscriptionRepository.getReferenceById(id);
		
		subscriptionEntity.setInvoiceStatus(status);
		if(paidAt != null) {
			subscriptionEntity.setLastPaidAt(paidAt);
		}
		
		subscriptionRepository.save(subscriptionEntity);
	}
	
	@Transactional
	public void paymentFailure(String priceId, String subscriptionId, String customerId, String status, LocalDateTime paidAt) {
		
	}
	
	@Transactional
	public void disable(String priceId, Integer userId, String subscriptionId, String customerId, String status, LocalDateTime paidAt) {
		
	}
	
	@Transactional
	public void subscriptionDelete(Integer id, String status) {	
		SubscriptionEntity subscriptionEntity = subscriptionRepository.getReferenceById(id);
		User user = subscriptionEntity.getUser();		
		PlanType planType = planTypeRepository.findByName("フリー");
		Role role = roleRepository.findByName("ROLE_GENERAL_FREE");
				
		// サブスクリプション論理削除。ステータスを残すため
		subscriptionEntity.setSubscriptionStatus(status);
		subscriptionEntity.setEnabled(false);
		subscriptionRepository.save(subscriptionEntity);
		
		if(planType != null && role != null) {
			user.setPlanType(planType);
			user.setRole(role);				
			userRepository.save(user);		
		}		
	}
}
