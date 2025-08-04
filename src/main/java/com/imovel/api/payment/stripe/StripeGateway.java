package com.imovel.api.payment.stripe;

import com.imovel.api.payment.PaymentGateway;
import com.imovel.api.payment.PaymentResponse;
import com.imovel.api.request.PaymentRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

public class StripeGateway implements PaymentGateway {

    @Value("${stripe.secret.key}")
    private String secretKey;
    @Value("${payment.success.url}")
    private String successUrl;
    @Value("${payment.cancel.url}")
    private String cancelUrl;

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            Stripe.apiKey = secretKey;
            Session session = createCheckoutSession(paymentRequest);
            return buildSuccessResponse(session);
        } catch (StripeException e) {
            return buildErrorResponse(e);
        }
    }

    private Session createCheckoutSession(PaymentRequest paymentRequest) throws StripeException {
        SessionCreateParams params = buildSessionParams(paymentRequest);
        return Session.create(params);
    }

    private SessionCreateParams buildSessionParams(PaymentRequest paymentRequest) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(createLineItem(paymentRequest))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(PaymentRequest paymentRequest) {

        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(getCurrency(paymentRequest))
                .setUnitAmount(StripeAmountConverter.convertToStripeAmount(paymentRequest.getAmount(),paymentRequest.getCurrency()))
                .setProductData(createProductData(paymentRequest))
                .build();
    }
    private SessionCreateParams.LineItem createLineItem(PaymentRequest paymentRequest) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(paymentRequest.getQuantity())
                .setPriceData(createPriceData(paymentRequest))
                .build();
    }


    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(PaymentRequest paymentRequest) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(paymentRequest.getName())
                .build();
    }

    private String getCurrency(PaymentRequest paymentRequest) {
        return paymentRequest.getCurrency() != null ? paymentRequest.getCurrency() : "USD";
    }

    private PaymentResponse buildSuccessResponse(Session session) {
        return PaymentResponse.builder()
                .status("SUCCESS")
                .message("Payment session created successfully")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }

    private PaymentResponse buildErrorResponse(StripeException e) {
        return PaymentResponse.builder()
                .status("FAILED")
                .message("Stripe payment failed: " + e.getMessage())
                .build();
    }
}