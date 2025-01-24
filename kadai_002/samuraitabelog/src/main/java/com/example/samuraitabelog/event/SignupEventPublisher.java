package com.example.samuraitabelog.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.samuraitabelog.entity.User;

@Component
public class SignupEventPublisher {
	private final ApplicationEventPublisher applicationEventPublisher;
	
	public SignupEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	// サインアップイベント発行
	public void publishSignupEvent(User user, String requestUrl) {
		applicationEventPublisher.publishEvent(new SignupEvent(this, user, requestUrl));
	}
	
	// パスワードリセットイベント発行
	public void publishPasswordResetEvent(User user, String requestUrl) {
		applicationEventPublisher.publishEvent(new PasswordResetEvent(this, user, requestUrl));
	}
}
