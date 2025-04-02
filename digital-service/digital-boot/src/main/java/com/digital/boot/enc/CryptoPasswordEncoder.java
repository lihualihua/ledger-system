package com.digital.boot.enc;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 数据库加密
 */
public class CryptoPasswordEncoder {
	public static void main(String[] args) {
		String password = "";
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		System.out.println("***************  The password encoder is : " + passwordEncoder.encode(password));
	}
}
