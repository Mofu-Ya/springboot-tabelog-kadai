package com.example.samuraitabelog.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitabelog.entity.SubscriptionEntity;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.SubscriptionRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.repository.projection.UserSubscriptionProjection;
import com.example.samuraitabelog.service.UserService;


@Controller
@RequestMapping("admin/users")
public class AdminUserController {
	private final UserRepository userRepository;
	private final UserService userService;
	private final SubscriptionRepository subscriptionRepository;
	
	public AdminUserController(UserRepository userRepository, UserService userService, SubscriptionRepository subscriptionRepository ) {
		this.userRepository = userRepository;	
		this.userService = userService;
		this.subscriptionRepository = subscriptionRepository;
	}
	
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
		Page<UserSubscriptionProjection> userPage;
		
		if (keyword != null && !keyword.isEmpty()) {	
			userPage = userService.getSearchUsers(keyword, pageable);
		} else {
			userPage = userService.getSearchUsers(null, pageable);

		}
				
		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);
		
		return "admin/users/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model) {
		User user = userRepository.getReferenceById(id);
		List<SubscriptionEntity> subscriptions = subscriptionRepository.findAllByUserOrderByUpdatedAtDesc(user);
		
		model.addAttribute("user", user);
		model.addAttribute("subscriptions", subscriptions);
		
		return "admin/users/show";
	}
}
