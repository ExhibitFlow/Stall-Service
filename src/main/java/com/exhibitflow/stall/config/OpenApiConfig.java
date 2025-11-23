package com.exhibitflow.stall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stall Service API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing exhibition stalls
                                
                                ## Authentication
                                This API uses JWT Bearer tokens issued by the Identity Service.
                                
                                ### Get Access Token
                                ```
                                POST http://localhost:8080/api/v1/auth/login
                                Content-Type: application/json
                                
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                ```
                                
                                ### Use Token
                                Add the token to the Authorization header:
                                ```
                                Authorization: Bearer {your-access-token}
                                ```
                                
                                ## Authorization Roles
                                - **VIEWER**: Read-only access (GET operations)
                                - **MANAGER**: Manage stalls (hold, release, reserve, update)
                                - **ADMIN**: Full access including create operations
                                
                                ## Identity Service
                                Base URL: http://localhost:8080/api/v1
                                
                                For complete documentation, see:
                                - [Identity Integration Guide](../IDENTITY_INTEGRATION.md)
                                - [Identity Service API Reference](../Identity_Service_API_QUICK_REFERENCE.md)
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                JWT Bearer token from Identity Service
                                                
                                                To obtain a token:
                                                1. Login at: POST http://localhost:8080/api/v1/auth/login
                                                2. Copy the 'accessToken' from response
                                                3. Click 'Authorize' button above
                                                4. Enter token (without 'Bearer' prefix)
                                                5. Click 'Authorize' to save
                                                
                                                Token includes:
                                                - User identity (username, email)
                                                - Roles (VIEWER, MANAGER, ADMIN)
                                                - Permissions (resource:action format)
                                                - Expiration (typically 24 hours)
                                                """)));
    }
}
