package com.imovel.api.booking.model.enums;

import java.math.BigDecimal;

public enum CancellationPolicyType {
    FLEXIBLE("Flexible", "Full refund 1 day prior to arrival", BigDecimal.valueOf(100), 1),
    MODERATE("Moderate", "Full refund 5 days prior to arrival", BigDecimal.valueOf(100), 5),
    STRICT("Strict", "50% refund up until 1 week prior to arrival", BigDecimal.valueOf(50), 7),
    SUPER_STRICT_30("Super Strict 30", "50% refund up until 30 days prior to arrival", BigDecimal.valueOf(50), 30),
    SUPER_STRICT_60("Super Strict 60", "50% refund up until 60 days prior to arrival", BigDecimal.valueOf(50), 60),
    NON_REFUNDABLE("Non-refundable", "No refund", BigDecimal.ZERO, 0);

    private final String displayName;
    private final String description;
    private final BigDecimal defaultRefundPercentage;
    private final Integer defaultDaysBeforeCheckin;

    CancellationPolicyType(String displayName, String description, 
                          BigDecimal defaultRefundPercentage, Integer defaultDaysBeforeCheckin) {
        this.displayName = displayName;
        this.description = description;
        this.defaultRefundPercentage = defaultRefundPercentage;
        this.defaultDaysBeforeCheckin = defaultDaysBeforeCheckin;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDefaultRefundPercentage() {
        return defaultRefundPercentage;
    }

    public Integer getDefaultDaysBeforeCheckin() {
        return defaultDaysBeforeCheckin;
    }

    public boolean isRefundable() {
        return defaultRefundPercentage.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isFlexible() {
        return this == FLEXIBLE || this == MODERATE;
    }

    public boolean isStrict() {
        return this == STRICT || this == SUPER_STRICT_30 || this == SUPER_STRICT_60;
    }
}
