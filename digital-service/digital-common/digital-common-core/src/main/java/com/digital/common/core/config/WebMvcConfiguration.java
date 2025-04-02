package com.digital.common.core.config;

import cn.hutool.core.date.DatePattern;
import com.digital.common.core.jackson.PigJavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * @author lengleng
 * @date 2019-06-24
 * <p>
 * 注入自自定义SQL 过滤
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = SERVLET)
public class WebMvcConfiguration implements WebMvcConfigurer {

	@Value("${spring.controller.path.prefix:/api}")
	public String prefix;

	/**
	 * 增加GET请求参数中时间类型转换 {@link PigJavaTimeModule}
	 * <ul>
	 * <li>HH:mm:ss -> LocalTime</li>
	 * <li>yyyy-MM-dd -> LocalDate</li>
	 * <li>yyyy-MM-dd HH:mm:ss -> LocalDateTime</li>
	 * </ul>
	 * @param registry
	 */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
		registrar.setTimeFormatter(DatePattern.NORM_TIME_FORMATTER);
		registrar.setDateFormatter(DatePattern.NORM_DATE_FORMATTER);
		registrar.setDateTimeFormatter(DatePattern.NORM_DATETIME_FORMATTER);
		registrar.registerFormatters(registry);
	}

	/**
	 * 系统国际化文件配置
	 * @return MessageSource
	 */
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:i18n/messages");
		return messageSource;
	}

	/**
	 * 设置统一前缀
	 *
	 * @param configurer
	 */
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix(prefix, c -> c.isAnnotationPresent(RestController.class));
		WebMvcConfigurer.super.configurePathMatch(configurer);
	}
}
