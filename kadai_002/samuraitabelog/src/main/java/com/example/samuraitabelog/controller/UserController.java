package com.example.samuraitabelog.controller;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.Gender;
import com.example.samuraitabelog.entity.Occupation;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.UserEditForm;
import com.example.samuraitabelog.repository.GenderRepository;
import com.example.samuraitabelog.repository.OccupationRepository;
import com.example.samuraitabelog.repository.PlanTypeRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/user")
public class UserController {
	private final UserRepository userRepository;
	private final GenderRepository genderRepository;
	private final OccupationRepository occupationRepository;
	private final PlanTypeRepository planTypeRepository;
	private final UserService userService;
	
	public UserController(UserRepository userRepository, UserService userService, GenderRepository genderRepository, OccupationRepository occupationRepository, PlanTypeRepository planTypeRepository) {
		this.userRepository = userRepository;
		this.genderRepository = genderRepository;
		this.occupationRepository = occupationRepository;
		this.planTypeRepository = planTypeRepository;
		this.userService = userService;
	}
	
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, HttpServletRequest request, HttpServletResponse response, Authentication authentication, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		boolean isChangePlan = false;
		
		String username = userDetailsImpl.getUsername();
	    Collection<? extends GrantedAuthority> roles = userDetailsImpl.getAuthorities();
	    boolean isPaidUser = roles.stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_GENERAL_PAID"));
	    if (isPaidUser && (user.getPlanType().getId() == planTypeRepository.findByName("フリー").getId())) {
	        
	        isChangePlan = true;
	        if (authentication != null) { // 認証されている場合のみ
	            new SecurityContextLogoutHandler().logout(request, response, authentication);
	        }
	        return "redirect:/?loggedOut";
	    } 
	    
	    model.addAttribute("user", user);
	    model.addAttribute("isChangePlan", isChangePlan);
		
		return "user/index";
	}
	
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		UserEditForm userEditForm = new UserEditForm(user.getId(), user.getName(), user.getFurigana(),user.getGender().getId(), user.getOccupation().getId(), user.getPostalCode(), user.getAddress(), user.getPhoneNumber(), user.getEmail());
		
		List<Gender> genders = genderRepository.findAll();
		List<Occupation> occupations = occupationRepository.findAll();
		
		model.addAttribute("userEditForm", userEditForm);
		model.addAttribute("genders", genders);
		model.addAttribute("occupations", occupations);
		
		return "user/edit";
	}
	
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated UserEditForm userEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
		List<Gender> genders = genderRepository.findAll();
		List<Occupation> occupations = occupationRepository.findAll();
		
		// メールアドレスが変更されており、かつ登録済みであれば、BindingResultオブジェクトにエラー内容を追加する	
		if (userService.isEmailChanged(userEditForm) && userService.isEmailRegistered(userEditForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("userEditForm", userEditForm);
			model.addAttribute("genders", genders);
			model.addAttribute("occupations", occupations);
			return "user/edit";
		}
		
		userService.update(userEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
		
		return "redirect:/user";
		
	}
	
	@GetMapping("/confirm")
	public String confirm(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
		
		model.addAttribute("user", user);
		
		return "user/delete";
	}
		
	@GetMapping("/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, HttpServletRequest httpServletRequest, HttpServletResponse response, Authentication authentication) {
		// ユーザーを論理削除
		userService.disable(userDetailsImpl.getUser());

		// 強制ログアウト
		if (authentication != null) { 
			// 認証されている場合のみ
            new SecurityContextLogoutHandler().logout(httpServletRequest, response, authentication);
        }
        return "redirect:/?loggedOut";
	}
}

