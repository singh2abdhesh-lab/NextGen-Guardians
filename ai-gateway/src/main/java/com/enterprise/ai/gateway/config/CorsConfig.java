package com.enterprise.ai.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Enterprise Cross-Origin Resource Sharing (CORS) security configuration.
 * Strictly manages permitted client origins, exposed governance headers, and preflight max-age caching.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins for development and production environments
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:[*]",
                "http://127.0.0.1:[*]",
                "https://*.enterprise.ai"
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Permitted headers
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Correlation-Id",
                "X-User-Role",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Exposed headers for browser clients
        config.setExposedHeaders(List.of(
                "X-Correlation-Id",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset",
                "Retry-After"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // 1 hour preflight cache

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
