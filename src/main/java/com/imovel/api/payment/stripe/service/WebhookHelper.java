package com.imovel.api.payment.stripe.service;

import com.imovel.api.logger.ApiLogger;
import com.imovel.api.payment.model.Payment;
import com.imovel.api.payment.model.enums.PaymentStatus;
import com.imovel.api.payment.repository.PaymentRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.EventDataObjectDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class WebhookHelper {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public WebhookHelper(PaymentRepository paymentRepository){
        this.paymentRepository = paymentRepository;
    }

    public void handlePaymentIntentSucceeded(Event event) {
        try {
            ApiLogger.info("=== Starting handlePaymentIntentSucceeded ===");
            ApiLogger.info("Event ID: " + event.getId());
            ApiLogger.info("Event Type: " + event.getType());
            ApiLogger.info("Event API Version: " + event.getApiVersion());

            // Method 1: Try to get the object directly
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

            if (paymentIntent != null) {
                ApiLogger.info("PaymentIntent found via direct deserialization: " + paymentIntent.getId());
                updatePaymentStatus(paymentIntent.getId(), PaymentStatus.SUCCEEDED, null);
                return;
            }
            ApiLogger.warn("PaymentIntent is null from direct deserialization, trying alternative methods...");
            // Method 2: Try to get the raw JSON and parse manually with Jackson
            try {
                String paymentIntentId = extractPaymentIntentIdWithJackson(event);
                if (paymentIntentId != null) {
                    ApiLogger.info("PaymentIntent ID extracted with Jackson: " + paymentIntentId);

                    // Retrieve the PaymentIntent from Stripe API
                    PaymentIntent retrievedIntent = PaymentIntent.retrieve(paymentIntentId);
                    if (retrievedIntent != null) {
                        ApiLogger.info("Successfully retrieved PaymentIntent from API: " + retrievedIntent.getId());
                        updatePaymentStatus(retrievedIntent.getId(), PaymentStatus.SUCCEEDED, null);
                        return;
                    }
                }
            } catch (Exception e) {
                ApiLogger.error("Error extracting PaymentIntent ID with Jackson", e);
            }
            // Method 3: Log the raw event data for debugging
            logRawEventDataWithJackson(event);

            ApiLogger.error("Could not retrieve PaymentIntent using any method for event: " + event.getId());

        } catch (Exception e)
        {
            ApiLogger.error("Error handling payment_intent.succeeded webhook", e);
        }
    }

    private String extractPaymentIntentIdWithJackson(Event event) {
        try {
            // Get the raw JSON string from the event
            String eventJson = event.toJson();
            JsonNode rootNode = objectMapper.readTree(eventJson);

            // Navigate to data.object.id
            JsonNode dataNode = rootNode.path("data");
            JsonNode objectNode = dataNode.path("object");
            JsonNode idNode = objectNode.path("id");

            if (!idNode.isMissingNode() && idNode.isTextual()) {
                return idNode.asText();
            }

            // Alternative: Try from raw object deserializer
            String rawJsonOptional = event.getDataObjectDeserializer().getRawJson();
            if (rawJsonOptional != null) {
                String rawJson = rawJsonOptional;
                JsonNode rawObjectNode = objectMapper.readTree(rawJson).path("object");
                JsonNode rawIdNode = rawObjectNode.path("id");

                if (!rawIdNode.isMissingNode() && rawIdNode.isTextual()) {
                    return rawIdNode.asText();
                }
            }

        } catch (Exception e) {
            ApiLogger.error("Error extracting PaymentIntent ID with Jackson", e);
        }
        return null;
    }

    private void logRawEventDataWithJackson(Event event) {
        try {
            ApiLogger.info("=== Raw Event Data (Jackson) ===");
            ApiLogger.info("Event JSON: " + event.toJson());

            if (event.getData() != null) {
                ApiLogger.info("Data Object: " + event.getData().getObject());
            }
            // Parse and log structured JSON using Jackson
            String eventJson = event.toJson();
            JsonNode rootNode = objectMapper.readTree(eventJson);

            ApiLogger.info("Structured Event Data:");
            ApiLogger.info("Event ID: " + rootNode.path("id").asText("N/A"));
            ApiLogger.info("Event Type: " + rootNode.path("type").asText("N/A"));

            JsonNode dataNode = rootNode.path("data");
            if (!dataNode.isMissingNode()) {
                JsonNode objectNode = dataNode.path("object");
                if (!objectNode.isMissingNode()) {
                    ApiLogger.info("Object Type: " + objectNode.path("object").asText("N/A"));
                    ApiLogger.info("Object ID: " + objectNode.path("id").asText("N/A"));
                    ApiLogger.info("Object Amount: " + objectNode.path("amount").asText("N/A"));
                    ApiLogger.info("Object Status: " + objectNode.path("status").asText("N/A"));
                }
            }
            // Log the pretty-printed JSON for better readability
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            ApiLogger.info("Pretty JSON:\n" + prettyJson);

        } catch (Exception e) {
            ApiLogger.error("Error logging raw event data with Jackson", e);
        }
    }
    // Alternative method using Jackson for JSON processing
    public void handlePaymentIntentSucceededAlternative(Event event) {
        try {
            ApiLogger.info("=== Starting alternative PaymentIntent handler (Jackson) ===");

            // Get the event data
            EventDataObjectDeserializer dataDeserializer = event.getDataObjectDeserializer();

            // Check if deserialization failed
            if (dataDeserializer.getObject().isEmpty()) {
                ApiLogger.warn("Deserialization failed, attempting manual extraction with Jackson");

                // Extract using Jackson
                String paymentIntentId = extractPaymentIntentIdWithJackson(event);
                if (paymentIntentId != null) {
                    ApiLogger.info("Found PaymentIntent ID with Jackson: " + paymentIntentId);
                    updatePaymentStatus(paymentIntentId, PaymentStatus.SUCCEEDED, null);
                } else {
                    ApiLogger.error("Could not extract PaymentIntent ID with Jackson");
                }
            } else {
                // Normal deserialization worked
                PaymentIntent paymentIntent = (PaymentIntent) dataDeserializer.getObject().get();
                ApiLogger.info("PaymentIntent found: " + paymentIntent.getId());
                updatePaymentStatus(paymentIntent.getId(), PaymentStatus.SUCCEEDED, null);
            }

        } catch (Exception e) {
            ApiLogger.error("Error in alternative PaymentIntent handler", e);
        }
    }
    // Simplified method using Jackson
    public void handlePaymentIntentSucceededSimplified(Event event) {
        try {

            ApiLogger.info("=== Starting simplified PaymentIntent handler (Jackson) ===");

            String paymentIntentId = null;

            // First try: Direct deserialization
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent != null)
            {
                paymentIntentId = paymentIntent.getId();
            } else {
                // Fallback: Extract using Jackson
                paymentIntentId = extractPaymentIntentIdWithJackson(event);

                // If still null, try one more approach with Jackson
                if (paymentIntentId == null)
                {
                    paymentIntentId = extractPaymentIntentIdFromRawJson(event);
                }
            }

            if (paymentIntentId != null) {
                ApiLogger.info("Processing PaymentIntent: " + paymentIntentId);
                updatePaymentStatus(paymentIntentId, PaymentStatus.SUCCEEDED, null);
            } else {
                ApiLogger.error("Unable to determine PaymentIntent ID from event: " + event.getId());
                logRawEventDataWithJackson(event);
            }

        } catch (Exception e) {
            ApiLogger.error("Error in simplified PaymentIntent handler", e);
        }
    }

    private String extractPaymentIntentIdFromRawJson(Event event) {
        try {
            String rawJsonOptional = event.getDataObjectDeserializer().getRawJson();

            if (rawJsonOptional != null) {
                String rawJson = rawJsonOptional;
                JsonNode jsonNode = objectMapper.readTree(rawJson);
                // Extract ID from the object
                JsonNode idNode = jsonNode.path("id");
                if (!idNode.isMissingNode() && idNode.isTextual()) {
                    return idNode.asText();
                }
            }
        } catch (Exception e) {
            ApiLogger.error("Error extracting PaymentIntent ID from raw JSON", e);
        }
        return null;
    }

    // Method to handle webhook with full Jackson parsing
    public void handlePaymentIntentWithFullJackson(Event event) {
        try {
            ApiLogger.info("=== Starting full Jackson PaymentIntent handler ===");
            // Parse the entire event with Jackson
            String eventJson = event.toJson();
            JsonNode rootNode = objectMapper.readTree(eventJson);

            // Extract all relevant information
            String eventId = rootNode.path("id").asText();
            String eventType = rootNode.path("type").asText();

            JsonNode dataNode = rootNode.path("data");
            JsonNode objectNode = dataNode.path("object");

            String paymentIntentId = objectNode.path("id").asText();
            String status = objectNode.path("status").asText();
            String amount = objectNode.path("amount").asText();
            String currency = objectNode.path("currency").asText();

            ApiLogger.info("Parsed with Jackson - ID: " + paymentIntentId +
                    ", Status: " + status +
                    ", Amount: " + amount +
                    ", Currency: " + currency);

            if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
                updatePaymentStatus(paymentIntentId, PaymentStatus.SUCCEEDED, null);
            } else {
                ApiLogger.error("PaymentIntent ID is empty in Jackson parsing");
            }

        } catch (Exception e) {
            ApiLogger.error("Error in full Jackson PaymentIntent handler", e);
        }
    }

    private void updatePaymentStatus(String paymentIntentId, PaymentStatus status, String errorMessage) {
        try {

            ApiLogger.info("Updating payment status for " + paymentIntentId + " to " + status);
            Optional<Payment> paymentOpt = paymentRepository.findByGatewayPaymentId(paymentIntentId);

            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus(status);
                paymentRepository.save(payment);
                ApiLogger.info("Updated payment status to " + status + " for gateway payment ID: " + paymentIntentId);

            } else {

                ApiLogger.info("Payment not found for gateway payment ID: " + paymentIntentId);
            }
        } catch (Exception e) {
            ApiLogger.error("Error updating payment status for: " + paymentIntentId, e);
        }
    }
}