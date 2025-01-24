package com.example.samuraitabelog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.PlanType;
import com.example.samuraitabelog.entity.Role;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.PasswordResetForm;
import com.example.samuraitabelog.form.SignupForm;
import com.example.samuraitabelog.form.UserEditForm;
import com.example.samuraitabelog.repository.GenderRepository;
import com.example.samuraitabelog.repository.OccupationRepository;
import com.example.samuraitabelog.repository.PlanTypeRepository;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.repository.projection.UserSubscriptionProjection;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final GenderRepository genderRepository;
	private final OccupationRepository occupationRepository;
	private final PlanTypeRepository planTypeRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, GenderRepository genderRepository, OccupationRepository occupationRepository, PlanTypeRepository planTypeRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.genderRepository = genderRepository;
		this.occupationRepository = occupationRepository;
		this.planTypeRepository = planTypeRepository;
		this.passwordEncoder = passwordEncoder;		
	}
	
	@Transactional
	public User create(SignupForm signupForm) {
		User user = new User();
		Role role = roleRepository.findByName("ROLE_GENERAL_FREE");
		PlanType planType = planTypeRepository.findByName("フリー");
		
		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setGender(genderRepository.getReferenceById(signupForm.getGenderId()));
		user.setOccupation(occupationRepository.getReferenceById(signupForm.getOccupationId()));
		user.setPlanType(planType);
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setRole(role);
		user.setEnabled(false);
		
		return userRepository.save(user);
	}
	
	@Transactional
	public void update(UserEditForm userEditForm) {
		User user = userRepository.getReferenceById(userEditForm.getId());
		
		user.setName(userEditForm.getName());
		user.setFurigana(userEditForm.getFurigana());
		user.setGender(genderRepository.getReferenceById(userEditForm.getGenderId()));
		user.setOccupation(occupationRepository.getReferenceById(userEditForm.getOccupationId()));
		user.setPostalCode(userEditForm.getPostalCode());
		user.setAddress(userEditForm.getAddress());
		user.setPhoneNumber(userEditForm.getPhoneNumber());
		user.setEmail(userEditForm.getEmail());
		
		userRepository.save(user);
	}
		
	@Transactional
	public void disable(User user) {
				
		user.setEnabled(false);
		
		userRepository.save(user);
	}
	
	@Transactional
	public User reactive(SignupForm signupForm) {
		User user = userRepository.findByEmail(signupForm.getEmail());
		Role role = roleRepository.findByName("ROLE_GENERAL_FREE");
		PlanType planType = planTypeRepository.findByName("フリー");
		
		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setGender(genderRepository.getReferenceById(signupForm.getGenderId()));
		user.setOccupation(occupationRepository.getReferenceById(signupForm.getOccupationId()));
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setPlanType(planType);
		user.setRole(role);
		
		return userRepository.save(user);
	}
	
	@Transactional
	public void resetPassword(PasswordResetForm passwordResetForm) {
		User user = userRepository.getReferenceById(passwordResetForm.getUserId());

		user.setPassword(passwordEncoder.encode(passwordResetForm.getPassword()));
		
		userRepository.save(user);
	}
	
	// メールアドレスが登録済みかどうかをチェックする
	public boolean isEmailRegistered(String email) {
		User user = userRepository.findByEmail(email);
		return user != null;
	}
	
	// パスワードとパスワード（確認用）の入力値が一致するかどうかをチェックする
	public boolean isSamePassword(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}
	
	// ユーザーを有効にする
	@Transactional
	public void enableUser(User user) {
		user.setEnabled(true);
		userRepository.save(user);
	}
	
	// メールアドレスが変更されたかどうかをチェックする
	public boolean isEmailChanged(UserEditForm userEditForm) {
		User currentUser = userRepository.getReferenceById(userEditForm.getId());
		return !userEditForm.getEmail().equals(currentUser.getEmail());
	}
	
	// 全会員の情報とサブスクリプション状態を合わせて取得する
    public Page<UserSubscriptionProjection> getAllUsers(Pageable pageable) {
        return userRepository.findAllWithLatestSubscriptions(pageable);
    }
    
    // 全会員の情報とサブスクリプション状態を合わせて取得する
    public Page<UserSubscriptionProjection> getSearchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUserWithLatestSubscriptions(keyword, pageable);
    }

}
