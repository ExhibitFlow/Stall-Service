package com.exhibitflow.stall.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Security configuration for Stall Service integrating with custom Identity Service.
 * 
 * <p>This configuration:
 * <ul>
 *   <li>Validates JWT tokens using HS512 shared secret</li>
 *   <li>Extracts roles, permissions, and authorities from JWT claims</li>
 *   <li>Enables method-level security with @PreAuthorize annotations</li>
 *   <li>Configures public endpoints (actuator, swagger)</li>
 * </ul>
 * 
 * <p>Identity Service Integration:
 * <ul>
 *   <li>JWT tokens issued by Identity Service at /api/v1/auth/login</li>
 *   <li>Token contains: roles, permissions, authorities claims</li>
 *   <li>Tokens validated using shared JWT_SECRET (HS512)</li>
 *   <li>Optional introspection endpoint: /api/v1/oauth/introspect</li>
 * </ul>
 * 
 * @see JwtAuthenticationConverter for custom claim extraction
 * @see com.exhibitflow.stall.controller.StallController for endpoint security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, @Secured annotations
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final IdentityServiceProperties identityServiceProperties;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Configures RestClient for calling Identity Service endpoints.
     * Used for token introspection and validation.
     */
    @Bean
    public org.springframework.web.client.RestClient identityServiceRestClient() {
        return org.springframework.web.client.RestClient.builder()
                .baseUrl(identityServiceProperties.getBaseUrl())
                .build();
    }

    /**
     * Configures JWT decoder to validate tokens using HS512 algorithm.
     * The secret must match the one used by the Identity Service.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Create a secret key for HMAC-SHA512 validation
        // This must match the secret used by the Identity Service
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(secretBytes, "HmacSHA512");
        
        // Build JWT decoder with explicit HS512 algorithm
        // IMPORTANT: Must explicitly specify HS512 - default is HS256
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS512)
                .build();
    }

    /**
     * Configures HTTP security with JWT authentication and role-based authorization.
     * 
     * <p>Public endpoints (no authentication required):
     * <ul>
     *   <li>/actuator/** - Health checks and monitoring</li>
     *   <li>/swagger-ui/** - API documentation UI</li>
     *   <li>/api-docs/** - OpenAPI specification</li>
     *   <li>/v3/api-docs/** - OpenAPI v3 documentation</li>
     * </ul>
     * 
     * <p>Protected endpoints (require authentication):
     * <ul>
     *   <li>All /api/** endpoints require valid JWT token</li>
     *   <li>Specific role requirements defined at method level with @PreAuthorize</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            // CSRF protection is disabled for stateless REST API using JWT authentication
            // This is safe because JWT tokens are not stored in cookies and are immune to CSRF attacks
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                // All other endpoints require authentication
                // Specific role/permission requirements are enforced at method level
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    // Use custom converter to extract roles, permissions, and authorities
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )
            .sessionManagement(session -> session
                // Stateless - no session creation
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}
