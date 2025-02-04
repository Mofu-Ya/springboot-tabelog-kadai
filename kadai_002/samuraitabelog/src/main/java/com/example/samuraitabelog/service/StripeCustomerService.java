package com.example.samuraitabelog.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.samuraitabelog.entity.PlanType;
import com.example.samuraitabelog.entity.SubscriptionEntity;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.enums.InvoiceStatus;
import com.example.samuraitabelog.enums.SubscriptionStatus;
import com.example.samuraitabelog.repository.PlanTypeRepository;
import com.example.samuraitabelog.repository.SubscriptionRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.billingportal.Session;
import com.stripe.param.billingportal.SessionCreateParams;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class StripeCustomerService {
	@Value("${stripe.api-key}")
	private String stripeApiKey;
	
    @Value("${stripe.price_id}")
    private String priceId;
    
	private final SubscriptionService subscriptionService;
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;
	private final PlanTypeRepository planTypeRepository;
	
	public StripeCustomerService(SubscriptionService subscriptionService, SubscriptionRepository subscriptionRepository, UserRepository userRepository, PlanTypeRepository planTypeRepository) {
		this.subscriptionService = subscriptionService;
		this.subscriptionRepository = subscriptionRepository;
		this.userRepository = userRepository;
		this.planTypeRepository = planTypeRepository;
	}	
		
	// カスタマーポータルを開く用のセッションを作成
	public String createSessionAndGetUrl(User user, HttpServletRequest httpServletRequest) {
		Stripe.apiKey = stripeApiKey;
		
		List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAllByUserAndPriceIdOrderByUpdatedAtDesc(user, user.getPlanType().getPriceId());
				
		String customerId = "";
		
		if(subscriptionEntities !=  null && subscriptionEntities.size() > 0) {
			for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
				if(subscriptionEntity.getEnabled()) {
					customerId = subscriptionEntity.getCustomerId();		
				}					
	        }
		} 
		
		if(customerId == "" || subscriptionEntities ==  null || subscriptionEntities.size() == 0) {		
			// イベント通知の順番次第で price ID が入らない時があるので、その場合は user のみで検索
			subscriptionEntities = subscriptionRepository.findAllByUserOrderByUpdatedAtDesc(user);
			if(subscriptionEntities !=  null && subscriptionEntities.size() > 0) {
				for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
					if(subscriptionEntity.getEnabled()) {
						customerId = subscriptionEntity.getCustomerId();		
					}					
		        }
			} 
		}
		String requestUrl = new String(httpServletRequest.getRequestURL());
		System.out.println("requestUrl : " + requestUrl);
		SessionCreateParams params = 
			SessionCreateParams.builder()
				.setCustomer(customerId)
				.setReturnUrl(requestUrl.replace("/subscribe/customer", "") + "/subscribe/complete")			
//				.setUiMode(SessionCreateParams.UiMode.EMBEDDED)
				.build();
		try {
			Session session = Session.create(params);
			return session.getUrl();
		} catch (StripeException e) {
			e.printStackTrace();
			return "";
		}
		
	}
	

	
	// サブスクリプションの作成時に受信。サブスクリプション登録処理を行う
	public void subscriptionCreated(Event event) {
		Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
		
		// サブスクリプション作成時は、subscriptions テーブルがまだ作成されていないはず
		// 受信の順番によっては、こちらで登録になるかも
		optionalStripeObject.ifPresentOrElse(stripeObject -> {
			Subscription subscription = (Subscription)stripeObject;			
			
	        String subscriptionId = subscription.getId();
	        String customerId = subscription.getCustomer();
	        String priceId = subscription
	                .getItems()
	                .getData()
	                .get(0) // 最初のアイテムを取得
	                .getPrice()
	                .getId();
	        String status = subscription.getStatus();  
			
			List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAllByCustomerIdAndSubscriptionId(customerId, subscriptionId);
			
			if(subscriptionEntities ==  null || subscriptionEntities.size() == 0) {
				// サブスクリプション作成時は、subscriptions テーブルがまだ作成されていない
				System.out.println("サブスクリプション登録処理を行います。");
				subscriptionService.create(subscriptionId, customerId, priceId, null);
			} else {
				// subscriptions テーブルが既にある場合。session completeと前後する場合があるので、
				// price ID と status の保存のみ行う
				System.out.println("データが既に存在しています。ステータスのみ更新します。");
				for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {										
					if(subscriptionEntity.getEnabled()
							&& (priceId.equals(subscriptionEntity.getPriceId()))) {
						System.out.println("サブスクリプション更新処理を行います。");
						subscriptionService.subscriptionUpdate(subscriptionEntity.getId(), priceId, status);
					}					
		        }
				
				System.out.println("サブスクリプション登録処理をスキップしました。");
				System.out.println("Stripe API Version: " + event.getApiVersion());
				System.out.println("stripe-java Version: " + Stripe.VERSION);
				return;
			}
												
			System.out.println("サブスクリプション登録処理が成功しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		},
		() -> {
			System.out.println("サブスクリプション登録処理が失敗しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		});
	}
		
	// サブスクリプションの作成、更新、解約時に受信。ステータスを保存し、解約の場合は解約処理を行う
	public void subscriptionUpdated(Event event) {
		Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
		
		optionalStripeObject.ifPresentOrElse(stripeObject -> {
			Subscription subscription = (Subscription)stripeObject;			
			
	        String subscriptionId = subscription.getId();
	        String customerId = subscription.getCustomer();
	        String priceId = subscription
	                .getItems()
	                .getData()
	                .get(0) // 最初のアイテムを取得
	                .getPrice()
	                .getId();
			String status = subscription.getStatus();  
			
			List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAllByCustomerIdAndSubscriptionId(customerId, subscriptionId);
			
			// Subscriptionステータスの更新のみ。無効になったサブスクリプションも更新する。
			if(subscriptionEntities !=  null && subscriptionEntities.size() > 0) {							
				for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
					if(priceId.equals(subscriptionEntity.getPriceId())) {
						System.out.println("サブスクリプション更新処理を行います。");	
						subscriptionService.subscriptionUpdate(subscriptionEntity.getId(), priceId, status);
					} else {
						// 無効なサブスクリプションの場合は、今後の拡張でサブスク回復処理を検討 TODO
						
					}
		        }				
			} else {
				System.out.println("サブスクリプションデータがありません。");
				System.out.println("サブスクリプション更新処理をスキップしました。");
				System.out.println("Stripe API Version: " + event.getApiVersion());
				System.out.println("stripe-java Version: " + Stripe.VERSION);
				return;
			}
						
			System.out.println("サブスクリプション更新処理が成功しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		},
		() -> {
			System.out.println("サブスクリプション更新処理が失敗しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		});
	}
	
	// サブスクリプションが Stripe 上で削除、自動解約された場合は、解約処理を行う
	public void subscriptionDeleted(Event event) {
		Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
				
		optionalStripeObject.ifPresentOrElse(stripeObject -> {
			Subscription subscription = (Subscription)stripeObject;
			
	        String subscriptionId = subscription.getId();
	        String customerId = subscription.getCustomer();
	        String priceId = subscription
	                .getItems()
	                .getData()
	                .get(0) // 最初のアイテムを取得
	                .getPrice()
	                .getId();
			String status = subscription.getStatus();  
			
			List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAllByCustomerIdAndSubscriptionId(customerId, subscriptionId);
			
			System.out.println("サブスクリプション削除を受け付けました。");
			if(SubscriptionStatus.CANCELED.matchesCode(status)) {
				// ステータスが canceled の場合、解約処理を行う
				System.out.println("サブスクリプション解約処理を開始します。");
				
				if(subscriptionEntities == null || subscriptionEntities.size() == 0) {
					// サブスクリプションがない場合は何もしない
					System.out.println("データがありません。");
					System.out.println("サブスクリプション解約処理をスキップしました。");
					System.out.println("Stripe API Version: " + event.getApiVersion());
					System.out.println("stripe-java Version: " + Stripe.VERSION);
					return;
				} else {
					for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
						User user = subscriptionEntity.getUser();
						PlanType planType = user.getPlanType();
						if(planType.getId() == planTypeRepository.findByName("フリー").getId()) {
							// 無料会員の場合、解約の必要なし。何もしない
							System.out.println("サブスクリプションは既に解約されています。");
							
							if(priceId.equals(subscriptionEntity.getPriceId())) {
								System.out.println("ステータスの更新のみ行います。");
								subscriptionService.subscriptionUpdate(subscriptionEntity.getId(), priceId, status);
							}
							
							System.out.println("サブスクリプション解約処理をスキップしました。");
							System.out.println("Stripe API Version: " + event.getApiVersion());
							System.out.println("stripe-java Version: " + Stripe.VERSION);
							return;
						} else if (subscriptionEntity.getEnabled() 
							&& (planType.getId() != planTypeRepository.findByName("フリー").getId() 
							&& (priceId.equals(subscriptionEntity.getPriceId())))){
							// Price IDが登録されているものと一致すれば解約処理を行う
							subscriptionService.subscriptionDelete(subscriptionEntity.getId(), status);							
						}
			        }
				}				
			} else {				
				if(subscriptionEntities !=  null && subscriptionEntities.size() > 0) {
					for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {					
						if(subscriptionEntity.getEnabled()
							&& (priceId.equals(subscriptionEntity.getPriceId()))) {
							// 有効なサブスクリプションがある場合はステータス保存
							System.out.println("ステータスが CANCELED ではないのでステータス更新のみ行います。");	
							subscriptionService.subscriptionUpdate(subscriptionEntity.getId(), priceId, status);													
						} 
			        }
				}
				System.out.println("サブスクリプション解約処理をスキップしました。");
				System.out.println("Stripe API Version: " + event.getApiVersion());
				System.out.println("stripe-java Version: " + Stripe.VERSION);
				return;
			}
						
			System.out.println("サブスクリプション解約処理が成功しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		},
		() -> {
			System.out.println("サブスクリプション解約処理が失敗しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		});
	}
	
	// 支払い更新処理。支払いが完了していたら最終支払い日の更新を行う
	public void paymentSucceeded(Event event) {
		Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
		
		optionalStripeObject.ifPresentOrElse(stripeObject -> {
			Invoice invoice = (Invoice)stripeObject;			
			
	        String subscriptionId = invoice.getSubscription();	        						
			String customerId = invoice.getCustomer();
			String priceId = invoice
	                .getLines()
	                .getData()
	                .get(0) // 最初のアイテムを取得
	                .getPrice()
	                .getId();
			String status = invoice.getStatus();
			
			
			LocalDateTime paramDateTime = null;
			List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAllByCustomerIdAndSubscriptionId(customerId, subscriptionId);
						
			
			if(subscriptionEntities !=  null && subscriptionEntities.size() > 0) {
				System.out.println("支払い更新処理を開始します。");
				for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
					User user = subscriptionEntity.getUser();
					PlanType planType = user.getPlanType();
					
					if(subscriptionEntity.getEnabled()) {
						if(InvoiceStatus.PAID.matchesCode(status)) {
							Long paymentTimestamp = invoice.getStatusTransitions().getPaidAt();
							LocalDateTime paymentDateTime = Instant.ofEpochSecond(paymentTimestamp)
							        .atZone(ZoneId.systemDefault())
							        .toLocalDateTime(); // UNIXタイムスタンプを変換
							System.out.println("支払い完了を受け付けました。最終支払日を更新します。");
							// ステータスが paid で Price ID が正しい場合、payment 更新処理を行う
							subscriptionService.paymentUpdate(subscriptionEntity.getId(), status, paymentDateTime);
						} else {
							System.out.println("支払いが完了していません。ステータスのみ更新します。");
							// ステータスが paid 以外の場合は、ステータス更新のみ行う
							subscriptionService.paymentUpdate(subscriptionEntity.getId(), status, null);
						}														
					} 
		        }
			} else {
				// サブスクリプションがない場合は何もしない
				System.out.println("データがありません。");
				System.out.println("支払い更新処理をスキップしました。");
				System.out.println("Stripe API Version: " + event.getApiVersion());
				System.out.println("stripe-java Version: " + Stripe.VERSION);
				return;
			}			 
					
			System.out.println("支払い更新処理が成功しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		},
		() -> {
			System.out.println("支払い更新処理が失敗しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		});
	}
	
	// 支払い失敗処理。invoice status の更新を行う
	public void paymentFailure(Event event) {
		Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
		
		optionalStripeObject.ifPresentOrElse(stripeObject -> {
			Invoice invoice = (Invoice)stripeObject;			
			
	        String subscriptionId = invoice.getSubscription();	        						
			String customerId = invoice.getCustomer();
			String priceId = invoice
	                .getLines()
	                .getData()
	                .get(0) // 最初のアイテムを取得
	                .getPrice()
	                .getId();
			String status = invoice.getStatus();
			
			List<SubscriptionEntity> subscriptionEntities = subscriptionRepository.findAllByCustomerIdAndSubscriptionId(customerId, subscriptionId);
								
			System.out.println("支払い失敗を受け付けました。支払いステータスを更新します。");
			if(subscriptionEntities !=  null) {
				for (SubscriptionEntity subscriptionEntity : subscriptionEntities) {
					User user = subscriptionEntity.getUser();
					PlanType planType = user.getPlanType();
					
					if(priceId.equals(planType.getPriceId())) {
						subscriptionService.paymentUpdate(subscriptionEntity.getId(), status, null);																			
					} 
		        }
			} else {
				// サブスクリプションがない場合は何もしない
				System.out.println("支払い失敗処理をスキップしました。");
				return;
			}			 
					
			System.out.println("支払い失敗処理が成功しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		},
		() -> {
			System.out.println("支払い失敗処理が失敗しました。");
			System.out.println("Stripe API Version: " + event.getApiVersion());
			System.out.println("stripe-java Version: " + Stripe.VERSION);
		});
	}
}

