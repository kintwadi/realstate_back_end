package com.imovel.api.payment;

import com.imovel.api.request.PaymentRequest;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        PaymentRequest request = new PaymentRequest("Premium Subscription",new BigDecimal(100), 1L, "USD");
        
        PaymentGatewayFactory factory = new PaymentGatewayFactory();
        
        // Process payment via Stripe
        PaymentGateway stripeGateway = factory.createPaymentGateway("STRIPE");
        stripeGateway.processPayment(request);
        
        // Process payment via PayPal
        PaymentGateway paypalGateway = factory.createPaymentGateway("PAYPAL");
        paypalGateway.processPayment(request);

    }
}