package com.imovel.api.config;

import com.imovel.api.payment.stripe.config.StripeConfig;
import com.stripe.Stripe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StripeConfigTest {

    private StripeConfig stripeConfig;

    @BeforeEach
    void setUp() {
        stripeConfig = new StripeConfig();
    }

    @Test
    void init_WithValidConfiguration_ShouldSetApiKey() {
        // Given
        String testSecretKey = "sk_test_123456789";
        String testPublicKey = "pk_test_123456789";
        String testWebhookSecret = "whsec_test123";

        ReflectionTestUtils.setField(stripeConfig, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(stripeConfig, "publicKey", testPublicKey);
        ReflectionTestUtils.setField(stripeConfig, "webhookSecret", testWebhookSecret);

        // When
        stripeConfig.init();

        // Then
        assertEquals(testSecretKey, Stripe.apiKey);
        assertEquals(testSecretKey, stripeConfig.getSecretKey());
        assertEquals(testPublicKey, stripeConfig.getPublicKey());
        assertEquals(testWebhookSecret, stripeConfig.getWebhookSecret());
    }

    @Test
    void init_WithNullSecretKey_ShouldThrowException() {
        // Given
        ReflectionTestUtils.setField(stripeConfig, "secretKey", null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> stripeConfig.init());
    }

    @Test
    void init_WithEmptySecretKey_ShouldThrowException() {
        // Given
        ReflectionTestUtils.setField(stripeConfig, "secretKey", "");

        // When & Then
        assertThrows(IllegalStateException.class, () -> stripeConfig.init());
    }

    @Test
    void init_WithInvalidSecretKeyFormat_ShouldThrowException() {
        // Given
        ReflectionTestUtils.setField(stripeConfig, "secretKey", "invalid_key_format");

        // When & Then
        assertThrows(IllegalStateException.class, () -> stripeConfig.init());
    }

    @Test
    void init_WithValidKeys_ShouldPass() {
        // Given
        String validSecretKey = "sk_test_123456789abcdef";
        String validPublicKey = "pk_test_123456789abcdef";
        String validWebhookSecret = "whsec_123456789abcdef";

        ReflectionTestUtils.setField(stripeConfig, "secretKey", validSecretKey);
        ReflectionTestUtils.setField(stripeConfig, "publicKey", validPublicKey);
        ReflectionTestUtils.setField(stripeConfig, "webhookSecret", validWebhookSecret);

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> stripeConfig.init());
    }

    @Test
    void getSecretKey_ShouldReturnConfiguredValue() {
        // Given
        String testSecretKey = "sk_test_123456789";
        ReflectionTestUtils.setField(stripeConfig, "secretKey", testSecretKey);

        // When
        String result = stripeConfig.getSecretKey();

        // Then
        assertEquals(testSecretKey, result);
    }

    @Test
    void getPublicKey_ShouldReturnConfiguredValue() {
        // Given
        String testPublicKey = "pk_test_123456789";
        ReflectionTestUtils.setField(stripeConfig, "publicKey", testPublicKey);

        // When
        String result = stripeConfig.getPublicKey();

        // Then
        assertEquals(testPublicKey, result);
    }

    @Test
    void getWebhookSecret_ShouldReturnConfiguredValue() {
        // Given
        String testWebhookSecret = "whsec_test123";
        ReflectionTestUtils.setField(stripeConfig, "webhookSecret", testWebhookSecret);

        // When
        String result = stripeConfig.getWebhookSecret();

        // Then
        assertEquals(testWebhookSecret, result);
    }

    @Test
    void toString_ShouldNotExposeSecretValues() {
        // Given
        String testSecretKey = "sk_test_123456789";
        String testPublicKey = "pk_test_123456789";
        String testWebhookSecret = "whsec_test123";

        ReflectionTestUtils.setField(stripeConfig, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(stripeConfig, "publicKey", testPublicKey);
        ReflectionTestUtils.setField(stripeConfig, "webhookSecret", testWebhookSecret);

        // When
        String result = stripeConfig.toString();

        // Then
        assertFalse(result.contains(testSecretKey), "Secret key should not be exposed in toString()");
        assertFalse(result.contains(testWebhookSecret), "Webhook secret should not be exposed in toString()");
        assertFalse(result.contains(testPublicKey), "Public key should be masked in toString()");
        assertTrue(result.contains("pk_test***"), "Masked public key should be shown in toString()");
    }
}