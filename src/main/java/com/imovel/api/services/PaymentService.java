package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.response.StandardResponse;
import java.math.BigDecimal;

public interface PaymentService {
    StandardResponse<Boolean> processPayment(Long userId, BigDecimal amount, String description);
    StandardResponse<Boolean> processRefund(Long userId, BigDecimal amount, String description);
    StandardResponse<BigDecimal> calculateProratedAmount(Long subscriptionId, Long newPlanId);
}