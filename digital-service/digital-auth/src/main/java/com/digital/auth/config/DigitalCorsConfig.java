package com.digital.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@RefreshScope
@ConfigurationProperties("digital.cors")
public class DigitalCorsConfig {
	private boolean allowCredentials;

	private long maxAge;

	private String pattern;

	private List<String> allowedOrigins;

	private List<String> allowedHeaders;

	private List<String> allowedMethods;

	private List<String> exposedHeaders;
}
