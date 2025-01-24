package com.example.samuraitabelog.enums;

public enum SubscriptionStatus {
	ACTIVE("active", "有効"),
	CANCELED("canceled", "解約");
	
	private final String code;
    private final String displayName;

    SubscriptionStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public static String getDisplayNameByCode(String code) {
        for (SubscriptionStatus status : values()) {
            if (status.getCode() == code) {
                return status.getDisplayName();
            }
        }
        throw new IllegalArgumentException("Invalid Code: " + code);
    }
    
    public boolean matchesCode(String code) {
        return this.code.equals(code);
    }
}
