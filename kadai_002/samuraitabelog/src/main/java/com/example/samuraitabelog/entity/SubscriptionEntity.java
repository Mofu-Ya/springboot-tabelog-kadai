package com.example.samuraitabelog.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "subscriptions")
@Data
public class SubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "subscription_id")
    private String subscriptionId;
    
    @Column(name = "price_id")
    private String priceId;
    
    @Column(name = "subscription_status")
    private String subscriptionStatus;
    
    @Column(name = "invoice_status")
    private String invoiceStatus;
    
    @Column(name = "last_paid_at")
    private LocalDateTime lastPaidAt;       
    
    @Column(name = "enabled")
    private Boolean enabled;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;     
}

