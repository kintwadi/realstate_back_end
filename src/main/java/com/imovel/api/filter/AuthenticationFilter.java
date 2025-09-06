package com.imovel.api.filter;

import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.services.ConfigurationService;
import com.imovel.api.util.ResourceLoader;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter for authenticating requests using JWT or other authentication mechanisms
 */
@Component
public class AuthenticationFilter implements Filter {

    private List<String> protectedEndpoints = Collections.emptyList();
    private List<String> excludedEndpoints = Collections.emptyList();
    @Autowired
    JWTProvider jwtProcessor;
    @Autowired
    private ConfigurationService configurationService;

    /**
     * Initializes the filter by reading secret keys from keystore
     *
     * @param filterConfig The filter configuration object.
     */
    @Override
    public void init( FilterConfig filterConfig )
    {
        configurationService.setDefaultConfigurations();
        jwtProcessor.initialize();
        try {
            // Load configuration using ResourceLoader
            ResourceLoader resourceLoader = new ResourceLoader();
            // Get protected endpoints
            protectedEndpoints = parseEndpoints(resourceLoader.getProperty("jwt.protected.endpoints"));
            // Get excluded endpoints
            excludedEndpoints = parseEndpoints(resourceLoader.getProperty("jwt.excluded.endpoints"));

        } catch (IOException ex)
        {
            System.out.println("Error loading filter configuration: "+ex.getMessage());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Check if path is excluded
        if (isPathMatch(path, excludedEndpoints)) {

            chain.doFilter(request, response);
            return;
        }

        // Check if path is protected
        if (isPathMatch(path, protectedEndpoints)) {
            // Validate JWT token
            String token = httpRequest.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing JWT token");
                return;
            }
            // pass on the claims
            String currentToken = removeBearerPrefix(token);

            if(!isValidToken(currentToken)){
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing JWT token");
                return;
            }

            httpRequest.getSession().setAttribute("claims",jwtProcessor.getAllClaim(currentToken));
            httpRequest.getSession().setAttribute("token",currentToken);
        }
        chain.doFilter(request, response);
    }

    private List<String> parseEndpoints(String endpointsStr) {
        return Arrays.stream(endpointsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
    private boolean isPathMatch(String path, List<String> patterns) {
        return patterns.stream().anyMatch(pattern ->
                path.startsWith(pattern.replace("*", "")) ||
                        path.matches(pattern.replace("*", ".*")));
    }

    private boolean isValidToken(String token) {
        return jwtProcessor.validateAccessToken(token);
    }

    public static String removeBearerPrefix(String token) {
        if (token == null) {
            return null;
        }

        // Pattern to match "Bearer" followed by one or more whitespace characters
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^Bearer\\s+");
        java.util.regex.Matcher matcher = pattern.matcher(token);

        if (matcher.find()) {
            return token.substring(matcher.end());
        }
        return token;
    }

}


