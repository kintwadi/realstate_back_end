// src/main/java/com/imovel/api/services/ForgotPasswordService.java
package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.model.AuthDetails;
import com.imovel.api.model.PasswordResetCode;
import com.imovel.api.model.User;
import com.imovel.api.repository.PasswordResetCodeRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.ForgotPasswordRequest;
import com.imovel.api.request.ResetPasswordRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.security.PasswordManager;
import com.imovel.api.util.Util;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final AuthDetailsService authDetailsService;
    private final PasswordManager passwordManager;
    private final MailService mailService;

    private static final SecureRandom RNG = new SecureRandom();
    private static final int CODE_TTL_MINUTES = 10;

    public ForgotPasswordService(UserRepository userRepository,
                                 PasswordResetCodeRepository passwordResetCodeRepository,
                                 AuthDetailsService authDetailsService,
                                 PasswordManager passwordManager,
                                 MailService mailService) {
        this.userRepository = userRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.authDetailsService = authDetailsService;
        this.passwordManager = passwordManager;
        this.mailService = mailService;
    }

    @Transactional
    public ApplicationResponse<Void> requestReset(ForgotPasswordRequest req) {
        ApiLogger.debug("ForgotPasswordService.requestReset", "Attempting reset request",
                req != null ? req.getEmail() : null);

        // Basic payload + email validation using your Util + ApiCode
        if (req == null || req.getEmail() == null || Util.isEmailInvalid(req.getEmail())) {
            return ApplicationResponse.error(
                    ApiCode.INVALID_EMAIL.getCode(),
                    ApiCode.INVALID_EMAIL.getMessage(),
                    HttpStatus.CONFLICT  // matches your registerUser usage for INVALID_EMAIL
            );
        }

        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Invalidate previous unconsumed code (reduces spam/race)
                passwordResetCodeRepository.findTopByUserAndConsumedFalseOrderByCreatedAtDesc(user)
                        .ifPresent(old -> {
                            old.setConsumed(true);
                            passwordResetCodeRepository.save(old);
                        });

                String code = generateFiveDigitCode();
                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES);

                PasswordResetCode prc = new PasswordResetCode(user, code, expiresAt);
                passwordResetCodeRepository.save(prc);

                try {
                    mailService.send(
                            email,
                            "Your password reset code",
                            "Use this code to reset your password: " + code + "\n" +
                                    "It expires in " + CODE_TTL_MINUTES + " minutes."
                    );
                    ApiLogger.info("ForgotPasswordService.requestReset", "Reset code emailed to " + email);
                } catch (Exception mailEx) {
                    // We don't expose this to the client (avoid enumeration), but log as EMAIL_SERVICE_ERROR
                    ApiLogger.error("ForgotPasswordService.requestReset.mail",
                            ApiCode.EMAIL_SERVICE_ERROR.getMessage() + ": " + mailEx.getMessage());
                }
            } else {
                // Don’t reveal existence
                ApiLogger.info("ForgotPasswordService.requestReset", "Request for non-existent email " + email);
            }

            // Always generic success to avoid account enumeration
            return ApplicationResponse.success(null, "If that address exists, a code has been sent.");

        } catch (Exception e) {
            ApiLogger.error("ForgotPasswordService.requestReset", e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    ApiCode.SYSTEM_ERROR.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    @Transactional
    public ApplicationResponse<Void> reset(ResetPasswordRequest req) {
        ApiLogger.debug("ForgotPasswordService.reset", "Attempting reset",
                req != null ? req.getEmail() : null);

        // Validate payload with your ApiCodes
        if (req == null
                || req.getEmail() == null
                || Util.isEmailInvalid(req.getEmail())
                || req.getCode() == null
                || !req.getCode().trim().matches("^\\d{5}$")
                || req.getNewPassword() == null
                || req.getNewPassword().isBlank()) {

            return ApplicationResponse.error(
                    ApiCode.INVALID_CREDENTIALS.getCode(),
                    ApiCode.INVALID_CREDENTIALS.getMessage(),
                    ApiCode.INVALID_CREDENTIALS.getHttpStatus()
            );
        }

        final String email = req.getEmail().trim().toLowerCase(Locale.ROOT);
        final String code = req.getCode().trim();
        final String newPassword = req.getNewPassword();

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                ApiLogger.info("ForgotPasswordService.reset", "Reset for non-existent email " + email);
                return ApplicationResponse.error(
                        ApiCode.INVALID_CREDENTIALS.getCode(),
                        "Invalid code or expired",
                        ApiCode.INVALID_CREDENTIALS.getHttpStatus()
                );
            }
            User user = userOpt.get();

            Optional<PasswordResetCode> codeOpt =
                    passwordResetCodeRepository.findTopByUserAndCodeAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                            user, code, LocalDateTime.now());

            if (codeOpt.isEmpty()) {
                ApiLogger.info("ForgotPasswordService.reset", "Invalid/expired code for " + email);
                return ApplicationResponse.error(
                        ApiCode.INVALID_CREDENTIALS.getCode(),
                        "Invalid code or expired",
                        ApiCode.INVALID_CREDENTIALS.getHttpStatus()
                );
            }

            // Consume code
            PasswordResetCode prc = codeOpt.get();
            prc.setConsumed(true);
            passwordResetCodeRepository.save(prc);

            // Create fresh salt/hash with PasswordManager
            AuthDetails newAuth = passwordManager.createAuthDetails(newPassword);

            // Fetch or create AuthDetails via your service contract
            AuthDetails existing;
            try {
                // your service returns ApplicationResponse<AuthDetails>
                ApplicationResponse<AuthDetails> existingResp = authDetailsService.findByUserId(user.getId());
                existing = existingResp.getData();
                existing.setSalt(newAuth.getSalt());
                existing.setHash(newAuth.getHash());
            } catch (ResourceNotFoundException notFound) {
                existing = new AuthDetails();
                existing.setUserId(user.getId());
                existing.setSalt(newAuth.getSalt());
                existing.setHash(newAuth.getHash());
            }

            // Persist using your service (return value not used here)
            authDetailsService.save(existing);

            ApiLogger.info("ForgotPasswordService.reset", "Password reset for userId=" + user.getId());
            return ApplicationResponse.success(null, "Password has been reset successfully.");

        } catch (Exception e) {
            ApiLogger.error("ForgotPasswordService.reset", e.getMessage());
            return ApplicationResponse.error(
                    ApiCode.SYSTEM_ERROR.getCode(),
                    ApiCode.SYSTEM_ERROR.getMessage(),
                    ApiCode.SYSTEM_ERROR.getHttpStatus()
            );
        }
    }

    private static String generateFiveDigitCode() {
        int n = RNG.nextInt(100000); // 0..99999
        return String.format("%05d", n);
    }
}
