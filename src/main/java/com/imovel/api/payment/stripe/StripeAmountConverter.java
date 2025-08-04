package com.imovel.api.payment.stripe;
import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles currency amount conversions according to Stripe's specifications.
 * Uses hardcoded zero-decimal currencies list with optional auto-refresh capability.
 */
public class StripeAmountConverter {
    @Value("${stripe.secret.key}")
    private static String secretKey;

    // Hardcoded list from Stripe's documentation (as of June 2024)
    private static final Set<String> HARDCODED_ZERO_DECIMAL_CURRENCIES = Set.of(
            "BIF", // Burundian Franc
            "CLP", // Chilean Peso
            "DJF", // Djiboutian Franc
            "GNF", // Guinean Franc
            "JPY", // Japanese Yen
            "KMF", // Comorian Franc
            "KRW", // South Korean Won
            "MGA", // Malagasy Ariary
            "PYG", // Paraguayan Guaraní
            "RWF", // Rwandan Franc
            "UGX", // Ugandan Shilling
            "VND", // Vietnamese Đồng
            "VUV", // Vanuatu Vatu
            "XAF", // CFA Franc BEAC
            "XOF", // CFA Franc BCEAO
            "XPF", // CFP Franc
            "HUF", // Hungarian Forint (special case - subunits exist but aren't used)
            "TWD"  // New Taiwan Dollar (subunits exist but aren't used in practice)
    );

    // Thread-safe cache that can be updated
    private static final Set<String> ZERO_DECIMAL_CURRENCIES =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        // Initialize with hardcoded values
        ZERO_DECIMAL_CURRENCIES.addAll(HARDCODED_ZERO_DECIMAL_CURRENCIES);

        // Initialize Stripe with your API key
        Stripe.apiKey = secretKey;
    }

    /**
     * Converts an amount to Stripe's required format
     * @param amount The original amount (e.g., 19.99 for $19.99)
     * @param currencyCode 3-letter ISO currency code (e.g., "USD")
     * @return Amount in smallest currency unit
     * @throws IllegalArgumentException for invalid inputs
     */
    public static long convertToStripeAmount(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currencyCode == null || currencyCode.length() != 3) {
            throw new IllegalArgumentException("Invalid currency code");
        }

        String normalizedCurrency = currencyCode.toUpperCase();
        boolean isZeroDecimal = ZERO_DECIMAL_CURRENCIES.contains(normalizedCurrency);

        BigDecimal result = amount.setScale(2, RoundingMode.HALF_UP);
        if (!isZeroDecimal) {
            result = result.multiply(new BigDecimal("100"));
        }

        return result.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /**
     * Helper method to check if a currency is zero-decimal
     */
    public static boolean isZeroDecimalCurrency(String currencyCode) {
        return ZERO_DECIMAL_CURRENCIES.contains(currencyCode.toUpperCase());
    }
}