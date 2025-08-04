package com.imovel.api.payment.paypal;

import com.imovel.api.payment.PaymentGateway;
import com.imovel.api.payment.PaymentResponse;
import com.imovel.api.request.PaymentRequest;

public class PayPalGateway implements PaymentGateway {
    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        System.out.println("Processing payment via PayPal:");
        System.out.println("Amount: " + request.getAmount());
        System.out.println("Name: " + request.getName());
        // Actual PayPal integration would go here
        return null;
    }
}