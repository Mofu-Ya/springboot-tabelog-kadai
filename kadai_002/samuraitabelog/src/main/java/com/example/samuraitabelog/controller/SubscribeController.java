package com.example.samuraitabelog.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.PlanTypeRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.StripeCustomerService;
import com.example.samuraitabelog.service.StripeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/subscribe")
public class SubscribeController {
	private final StripeService stripeService;
	private final StripeCustomerService stripeCustomerService;
	private final PlanTypeRepository planTypeRepository;
	private final UserRepository userRepository;
	
	public SubscribeController (StripeService stripeService, StripeCustomerService stripeCustomerService, UserRepository userRepository, PlanTypeRepository planTypeRepository) {
		this.stripeService = stripeService;
		this.stripeCustomerService = stripeCustomerService;
		this.userRepository = userRepository;
		this.planTypeRepository = planTypeRepository;
	}
	
	@GetMapping("/confirm")
	public String confirm(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, HttpServletRequest httpServletRequest, Model model) {
		String sessionId = stripeService.createStripeSession(userDetailsImpl.getUser(), httpServletRequest);
		model.addAttribute("sessionId", sessionId);
		return "subscribe/confirm";
	}
	
	@GetMapping("/customer")
	public String customer(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, HttpServletRequest httpServletRequest, HttpServletResponse response, Authentication authentication) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		
		if(userDetailsImpl.getUser().getPlanType().getId() 
				== planTypeRepository.findByName("フリー").getId()) {
			if (authentication != null) { 
				// 認証されている場合のみ
	            new SecurityContextLogoutHandler().logout(httpServletRequest, response, authentication);
	        }
	        return "redirect:/?loggedOut";
		}
		
		String portalUrl = stripeCustomerService.createSessionAndGetUrl(userDetailsImpl.getUser(), httpServletRequest);

		return "redirect:" + portalUrl;
	}
	
	@GetMapping("/complete")
	public String complete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		boolean isDeleteSubscripton = false;
			
		// もし解約処理が行われて無料プランに変更されいた場合、強制ログアウトを行う
		if(userDetailsImpl.getUser().getPlanType().getId() 
				== planTypeRepository.findByName("フリー").getId()) {
			isDeleteSubscripton = true;
		}
		
		model.addAttribute("isDeleteSubscripton", isDeleteSubscripton);
		return "subscribe/complete";
	}
	
	@GetMapping("/upgraded")
	public String upgraded(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		
		return "subscribe/upgraded";
	}
	
	@GetMapping("/cancel")
	public String cancel(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {		
		
		return "subscribe/cancel";
	}
	
}
