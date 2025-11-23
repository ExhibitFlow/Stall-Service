package com.exhibitflow.stall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Identity Service integration.
 * 
 * <p>Loads configuration from application.yml under "identity-service" prefix.
 * Used to configure REST client for token introspection and validation.
 */
@Configuration
@ConfigurationProperties(prefix = "identity-service")
@Data
public class IdentityServiceProperties {
    
    /**
     * Base URL of the Identity Service (e.g., http://localhost:8080/api/v1)
     */
    private String baseUrl;
    
    /**
     * OAuth-related endpoints
     */
    private OAuthProperties oauth = new OAuthProperties();
    
    /**
     * Authentication-related endpoints (for reference)
     */
    private AuthProperties auth = new AuthProperties();
    
    @Data
    public static class OAuthProperties {
        /**
         * Token introspection endpoint (RFC 7662)
         * Default: {base-url}/oauth/introspect
         */
        private String introspectUrl;
        
        /**
         * Simple token validation endpoint
         * Default: {base-url}/oauth/validate
         */
        private String validateUrl;
    }
    
    @Data
    public static class AuthProperties {
        /**
         * Login endpoint to obtain access tokens
         * Default: {base-url}/auth/login
         */
        private String loginUrl;
        
        /**
         * User registration endpoint
         * Default: {base-url}/auth/register
         */
        private String registerUrl;
        
        /**
         * Token refresh endpoint
         * Default: {base-url}/auth/refresh
         */
        private String refreshUrl;
    }
}
