package de.htw_berlin.KinderCareConnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for frontend-backend communication
 * Allows requests from Vue.js frontend deployed on Render
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",           // Local frontend development
                    "http://localhost:5173",           // Vite dev server
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:5173",
                    "https://*.onrender.com",           // Render deployment
                    "https://kindercare-connect.onrender.com" // Your actual frontend URL
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // Allow static files to be served without CORS restrictions
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .maxAge(3600);
    }
}

