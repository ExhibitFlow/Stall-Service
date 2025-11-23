package com.exhibitflow.stall.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom JWT authentication converter that extracts roles, permissions, and authorities
 * from the Identity Service JWT tokens.
 * 
 * The Identity Service JWT contains:
 * - "roles": ["MANAGER", "VIEWER"] - Role names without ROLE_ prefix
 * - "permissions": ["content:read", "content:write", "user:read"] - Permission strings
 * - "authorities": ["ROLE_MANAGER", "ROLE_VIEWER", "content:read", ...] - Combined list
 * 
 * This converter:
 * 1. Extracts roles and adds "ROLE_" prefix for Spring Security
 * 2. Extracts permissions as-is
 * 3. Falls back to "authorities" claim if available
 * 4. Combines all into GrantedAuthority collection
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extract authorities from JWT claims in the following order:
     * 1. Extract "roles" claim and prefix with "ROLE_"
     * 2. Extract "permissions" claim as-is
     * 3. Extract "authorities" claim as-is (fallback/additional)
     * 4. Combine all unique authorities
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract roles from "roles" claim (e.g., ["MANAGER", "VIEWER"])
        // Add "ROLE_" prefix for Spring Security
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && !roles.isEmpty()) {
            Set<GrantedAuthority> roleAuthorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
            authorities.addAll(roleAuthorities);
        }

        // Extract permissions from "permissions" claim (e.g., ["content:read", "content:write"])
        // Use as-is without prefix
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null && !permissions.isEmpty()) {
            Set<GrantedAuthority> permissionAuthorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
            authorities.addAll(permissionAuthorities);
        }

        // Extract authorities claim (contains both roles with ROLE_ prefix and permissions)
        // This is a fallback/additional source
        List<String> authoritiesClaim = jwt.getClaimAsStringList("authorities");
        if (authoritiesClaim != null && !authoritiesClaim.isEmpty()) {
            Set<GrantedAuthority> claimAuthorities = authoritiesClaim.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
            authorities.addAll(claimAuthorities);
        }

        // If no custom claims found, fallback to default converter (uses "scope" and "scp" claims)
        if (authorities.isEmpty()) {
            authorities.addAll(defaultGrantedAuthoritiesConverter.convert(jwt));
        }

        return authorities;
    }
}
