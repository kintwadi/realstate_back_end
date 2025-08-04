package com.imovel.api.payment;


import com.imovel.api.request.PaymentRequest;

public interface PaymentGateway {
    PaymentResponse  processPayment(PaymentRequest request);
}
