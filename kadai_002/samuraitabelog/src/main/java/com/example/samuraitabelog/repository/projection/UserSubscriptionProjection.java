package com.example.samuraitabelog.repository.projection;

public interface UserSubscriptionProjection {
	Integer getId();
    String getName();
    String getFurigana();
    String getEmail();
    String getPlanTypeName();
    String getInvoiceStatus();
    Boolean getEnabled();

}
