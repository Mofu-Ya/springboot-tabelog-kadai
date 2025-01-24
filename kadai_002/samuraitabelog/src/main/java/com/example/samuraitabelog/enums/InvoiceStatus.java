package com.example.samuraitabelog.enums;

public enum InvoiceStatus {
	PAID("paid", "支払完了"),
	OPEN("open", "支払未完了"),
	UNCOLLECTIBLE("uncollectible", "回収不能");
	
	private final String code;
    private final String displayName;

    InvoiceStatus(String code, String displayName) {
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
        for (InvoiceStatus status : values()) {
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
