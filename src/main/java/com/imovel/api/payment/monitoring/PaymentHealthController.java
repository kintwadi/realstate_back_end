package com.imovel.api.payment.monitoring;

import com.imovel.api.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check controller for payment system monitoring
 * Alternative to Spring Boot Actuator HealthIndicator
 */
@RestController
@RequestMapping("/api/health")
public class PaymentHealthController {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentHealthController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> checkPaymentHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        try {
            // Check database connectivity by counting payments
            long totalPayments = paymentRepository.count();
            
            // Check recent payment activity (last 24 hours)
            LocalDateTime yesterday = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
            long recentPayments = paymentRepository.countByCreatedAtAfter(yesterday);
            
            // Check for failed payments in the last hour
            LocalDateTime lastHour = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
            long recentFailedPayments = paymentRepository.countByCreatedAtAfterAndStatus(
                lastHour, "FAILED");
            
            // Calculate failure rate
            double failureRate = recentPayments > 0 ? 
                (double) recentFailedPayments / recentPayments * 100 : 0;
            
            healthInfo.put("status", failureRate > 10.0 ? "DOWN" : "UP");
            healthInfo.put("totalPayments", totalPayments);
            healthInfo.put("recentPayments24h", recentPayments);
            healthInfo.put("recentFailedPayments1h", recentFailedPayments);
            healthInfo.put("failureRate1h", String.format("%.2f%%", failureRate));
            healthInfo.put("databaseConnected", true);
            healthInfo.put("timestamp", LocalDateTime.now().toString());
            
            if (failureRate > 10.0) {
                healthInfo.put("reason", "High failure rate detected");
                return ResponseEntity.status(503).body(healthInfo); // Service Unavailable
            }
            
            return ResponseEntity.ok(healthInfo);
            
        } catch (Exception e) {
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("databaseConnected", false);
            healthInfo.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(503).body(healthInfo);
        }
    }
}