package com.digital.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@ConditionalOnProperty(name = "digital.cors.enabled", havingValue = "true", matchIfMissing = false)
public class DigitalCorsConfiguration {
	@Bean
	public CorsFilter corsFilter(DigitalCorsConfig corsConfig) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(corsConfig.getAllowedOrigins());
		config.setAllowedHeaders(corsConfig.getAllowedHeaders());
		config.setMaxAge(corsConfig.getMaxAge());
		config.setAllowedMethods(corsConfig.getAllowedMethods());
		config.setExposedHeaders(corsConfig.getExposedHeaders());
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(corsConfig.getPattern(), config);
		return new CorsFilter(source);
	}
}
