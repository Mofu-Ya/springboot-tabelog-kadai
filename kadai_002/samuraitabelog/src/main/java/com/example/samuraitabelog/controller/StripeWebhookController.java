package com.example.samuraitabelog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.samuraitabelog.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@Controller
public class StripeWebhookController {
	private final StripeService stripeService;
	
	@Value("${stripe.api-key}")
	private String stripeApiKey;
	
	@Value("${stripe.webhook-secret}")
	private String webhookSecret;
	
	public StripeWebhookController(StripeService stripeService) {
		this.stripeService = stripeService;
	}
	
	@PostMapping("/stripe/webhook")
	public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
		Stripe.apiKey = stripeApiKey;
		Event event = null;
		
		try {
			event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
		} catch (SignatureVerificationException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		// セッション完了の場合、ユーザー情報を登録する
		// サブスク購入イベントの順番としては一番最後
		if ("checkout.session.completed".equals(event.getType())) {
			stripeService.processSessionCompleted(event);
		}				
		
		// サブスクリプションが更新・解約された時、ステータス更新や解約処理を行う		
		if ("customer.subscription.updated".equals(event.getType())) {
			stripeService.subscriptionUpdated(event);
		}
		
		// サブスクリプションが作成された場合、登録処理を行う
		// サブスク購入イベントの順番としては最も早い
		if ("customer.subscription.created".equals(event.getType())) {
			stripeService.subscriptionCreated(event);
		}
		
		// サブスクリプションが削除・自動解約された時、解約処理を行う		
		if ("customer.subscription.deleted".equals(event.getType())) {
			stripeService.subscriptionDeleted(event);
		}
			
		// 料金の支払いが成功したとき、invoice ステータス更新および最終支払日更新
		if ("invoice.payment_succeeded".equals(event.getType())) {
			stripeService.paymentSucceeded(event);
		}
		
		// 料金の支払いが失敗した時、ステータス更新
		// アプリ側では失敗のステータス表示のみ
		if ("invoice.payment_failed".equals(event.getType())) {
			stripeService.paymentFailure(event);
		}
		
		return new ResponseEntity<>("Success", HttpStatus.OK); 
	}
	
}

