package com.example.samuraitabelog.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.VerificationTokenRepository;
import com.example.samuraitabelog.service.VerificationTokenService;

@Component
public class SignupEventListener {
	private final VerificationTokenService verificationTokenService;
	private final VerificationTokenRepository verificationTokenRepository;
	private final JavaMailSender javaMailSender;
	
	public SignupEventListener(VerificationTokenService verificationTokenService, VerificationTokenRepository verificationTokenRepository, JavaMailSender mailSender) {
		this.verificationTokenService = verificationTokenService;
		this.verificationTokenRepository = verificationTokenRepository;
		this.javaMailSender = mailSender;
	}
	
	@EventListener
	private void onSignupEvent(SignupEvent signupEvent) {
		User user = signupEvent.getUser();
		String token = UUID.randomUUID().toString();
		if(verificationTokenRepository.findByUser(user) != null) {
			verificationTokenService.update(user, token);
		} else {
			verificationTokenService.create(user, token);
		}
				
		String recipientAddress = user.getEmail();
		String subject = "メール認証";
		String confirmationUrl = signupEvent.getRequestUrl() + "/verify?token=" + token;
		String message = "以下のリンクをクリックして会員登録を完了してください。";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(recipientAddress);
		mailMessage.setSubject(subject);
		mailMessage.setText(message + "\n" + confirmationUrl);
		javaMailSender.send(mailMessage);
	}
}
