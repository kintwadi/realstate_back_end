package com.imovel.api.payment.factory;

import com.imovel.api.payment.gateway.PaymentGatewayInterface;
import com.imovel.api.payment.stripe.service.StripePaymentGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class PaymentGatewayFactory {
    
    private final Map<String, PaymentGatewayInterface> gateways;
    
    @Autowired
    public PaymentGatewayFactory(Optional<StripePaymentGateway> stripePaymentGateway) {
        this.gateways = new HashMap<>();
        
        // Register available payment gateways only if they are available
        stripePaymentGateway.ifPresent(gateway -> gateways.put("stripe", gateway));
        // Future gateways can be added here:
        // gateways.put("paypal", paypalPaymentGateway);
        // gateways.put("square", squarePaymentGateway);
    }
    
    /**
     * Get payment gateway implementation by gateway name
     * 
     * @param gatewayName The name of the payment gateway (e.g., "stripe", "paypal")
     * @return PaymentGatewayInterface implementation or null if not found
     */
    public PaymentGatewayInterface getPaymentGateway(String gatewayName) {
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            return null;
        }
        
        return gateways.get(gatewayName.toLowerCase());
    }
    
    /**
     * Check if a payment gateway is supported
     * 
     * @param gatewayName The name of the payment gateway
     * @return true if supported, false otherwise
     */
    public boolean isGatewaySupported(String gatewayName) {
        if (gatewayName == null || gatewayName.trim().isEmpty()) {
            return false;
        }
        
        return gateways.containsKey(gatewayName.toLowerCase());
    }
    
    /**
     * Get all supported gateway names
     * 
     * @return Set of supported gateway names
     */
    public java.util.Set<String> getSupportedGateways() {
        return gateways.keySet();
    }
}
