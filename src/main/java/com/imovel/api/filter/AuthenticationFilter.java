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
 * and handling CORS
 */
@Component
public class AuthenticationFilter implements Filter {

    private List<String> protectedEndpoints = Collections.emptyList();
    private List<String> excludedEndpoints = Collections.emptyList();
    private List<String> allowedOrigins = Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:300"
            // Add other allowed origins as needed
    );
    
    private boolean initialized = false;

    @Autowired
    JWTProvider jwtProcessor;
    @Autowired
    private ConfigurationService configurationService;

    /**
     * Initializes the filter by reading endpoint configurations only
     * JWT initialization is deferred to first use to avoid Spring context timing issues
     *
     * @param filterConfig The filter configuration object.
     */
    @Override
    public void init( FilterConfig filterConfig )
    {
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
    
    /**
     * Lazy initialization of JWT components to ensure Spring context is fully loaded
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    try {
                        configurationService.setDefaultConfigurations();
                        jwtProcessor.initialize();
                        initialized = true;
                    } catch (Exception e) {
                        System.err.println("Failed to initialize JWT components: " + e.getMessage());
                        throw new RuntimeException("JWT initialization failed", e);
                    }
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Add CORS headers to all responses
        addCorsHeaders(httpRequest, httpResponse);

        // Handle preflight requests first
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Check if path is excluded
        if (isPathMatch(path, excludedEndpoints)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if path is protected
        if (isPathMatch(path, protectedEndpoints)) {
            // Ensure JWT components are initialized before use
            ensureInitialized();
            
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

    /**
     * Adds CORS headers to the response
     */
    private void addCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");

        // Check if origin is allowed
        if (origin != null && allowedOrigins.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else if (allowedOrigins.contains("*")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }

        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Authorization, Content-Length");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Disposition");
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