package com.digital.boot.bootstrap;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

public class DecryptingPropertySourceLoader implements EnvironmentPostProcessor {

    private final static String SECURITY = "Security:";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String datasourcePassword = environment.getProperty("spring.datasource.password");
        String secretKey = environment.getProperty("security.encodeKey");
        if (StringUtils.isBlank(datasourcePassword) || StringUtils.isBlank(secretKey) || !datasourcePassword.startsWith(SECURITY)) {
            return;
        }
        AES aes = new AES(Mode.CFB, Padding.NoPadding,
                new SecretKeySpec(secretKey.getBytes(), "AES"),
                new IvParameterSpec(secretKey.getBytes()));
        Map<String, Object> map = new HashMap<>();
        map.put("spring.datasource.password", aes.decryptStr(datasourcePassword.substring(SECURITY.length())));
        PropertySource<?> propertySource = new MapPropertySource("passwordPropertySource", map);
        environment.getPropertySources().addAfter("systemProperties", propertySource);
    }
}
