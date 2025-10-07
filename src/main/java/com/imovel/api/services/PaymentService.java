package com.imovel.api.services;

import com.imovel.api.response.ApplicationResponse;
import java.math.BigDecimal;

public interface PaymentService {
    ApplicationResponse<Boolean> processPayment(Long userId, BigDecimal amount, String description);
    ApplicationResponse<Boolean> processRefund(Long userId, BigDecimal amount, String description);
    ApplicationResponse<BigDecimal> calculateProratedAmount(Long subscriptionId, Long newPlanId);
}
