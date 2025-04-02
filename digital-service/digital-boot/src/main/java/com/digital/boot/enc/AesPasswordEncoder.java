package com.digital.boot.enc;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.List;

/**
 * 前端加密
 */
public class AesPasswordEncoder {
	public static void main(String[] args) {
		String password = "";
		String passwordStr = "";
		aesEncAndDec(password, passwordStr);
	}

	private static void aesEncAndDec(String password, String passwordStr) {
		try {
			String secretKey = getSecretKey();
			AES aes = new AES(Mode.CFB, Padding.NoPadding,
					new SecretKeySpec(secretKey.getBytes(), "AES"),
					new IvParameterSpec(secretKey.getBytes()));
			System.out.println("***************  The encrypt password is : " + aes.encryptHex(password));
			System.out.println("***************  The decrypt password is : " + aes.decryptStr(passwordStr));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getSecretKey() throws IOException {
		ClassPathResource classPathResource = new ClassPathResource("application-dev.yml");
		// 使用Spring内置的YamlPropertySourceLoader来加载配置文件
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		List<PropertySource<?>> propertySources = loader.load("application-dev", classPathResource);
		return String.valueOf(propertySources.get(0).getProperty("security.encodeKey"));
	}
}
