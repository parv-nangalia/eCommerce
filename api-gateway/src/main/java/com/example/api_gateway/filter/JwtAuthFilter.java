package com.example.api_gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import javax.crypto.SecretKey;
import java.security.Key;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final String SHARED_SECRET_KEY;
    private final Key signingKey; // Declare the Key once for efficiency

    public JwtAuthFilter(@Value("${jwt.secret}") String sharedSecretKey) {
        this.SHARED_SECRET_KEY = sharedSecretKey;
        // Initialize the Key in the constructor for the HMAC-SHA algorithm
        this.signingKey = Keys.hmacShaKeyFor(this.SHARED_SECRET_KEY.getBytes());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Bypass authentication for the login endpoint
        String path = exchange.getRequest().getURI().getPath();
        System.out.println("path: " + path);
        if (path.startsWith("/api/auth/")) {
            System.out.println("Bypassing authentication for path: " + path); // Added for logging
            return chain.filter(exchange);
        }

        // 1. Get the Authorization header from the request
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 2. Check if the header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            System.err.println("Authorization header is missing or malformed.");
            return exchange.getResponse().setComplete();
        }

        // Extract the JWT token (remove "Bearer " prefix)
        String token = authHeader.substring(7);

        try {
            // 3. Validate the token locally using the shared secret key
            // Correct usage for JJWT 0.12.0+ 🚀
            Jwts.parser() // Start building the parser (this method now returns JwtParserBuilder in 0.12.x)
                    .verifyWith((SecretKey) signingKey) // Use the new recommended method to set the signing key
                    .build() // Build the JwtParser instance
                    .parseSignedClaims(token); // Attempt to parse and validate the token

            // If no exception is thrown, the token is valid.
            // You can optionally extract claims (e.g., user ID, roles) here if needed for further processing.
            // Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            // System.out.println("JWT Validated. User: " + claims.getSubject());

            // Token is valid, proceed with the request to the downstream service
            return chain.filter(exchange);

        } catch (SignatureException | MalformedJwtException e) {
            // Catches invalid signature or malformed JWT token
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            System.err.println("Invalid JWT signature or token format: " + e.getMessage());
            return exchange.getResponse().setComplete();
        } catch (ExpiredJwtException e) {
            // Catches tokens that have expired
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // 403 Forbidden is appropriate for expired token
            System.err.println("JWT token is expired: " + e.getMessage());
            return exchange.getResponse().setComplete();
        } catch (UnsupportedJwtException e) {
            // Catches unsupported JWTs (e.g., wrong algorithm)
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            System.err.println("Unsupported JWT token: " + e.getMessage());
            return exchange.getResponse().setComplete();
        } catch (IllegalArgumentException e) {
            // Catches empty or null JWT
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            System.err.println("JWT claims string is empty: " + e.getMessage());
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            // Catch any other unexpected exceptions during validation
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            System.err.println("JWT validation failed due to an unexpected error: " + e.getMessage());
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        // This sets the order of the filter. A lower value means higher precedence.
        // -1 ensures this JWT filter runs very early in the filter chain.
        return -1;
    }
}