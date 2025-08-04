package com.imovel.api.payment;

import com.imovel.api.payment.paypal.PayPalGateway;
import com.imovel.api.payment.stripe.StripeGateway;

public class PaymentGatewayFactory {

    public PaymentGateway createPaymentGateway(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Payment gateway type cannot be null or empty");
        }

        if(type.equals("STRIPE")){
            return new StripeGateway();
        }

        if(type.equals("PAYPAL")){
            return new PayPalGateway();
        }
        throw new IllegalArgumentException("Unknown payment gateway type: " + type);
//        switch (type) {
//            case "STRIPE":
//                return new StripeGateway();
//            case "PAYPAL":
//                return new PayPalGateway();
//            default:
//                throw new IllegalArgumentException("Unknown payment gateway type: " + type);
//        }
    }
}