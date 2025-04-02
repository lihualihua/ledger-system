package com.digital.upms;

import com.digital.common.security.annotation.EnablePigResourceServer;
import com.digital.common.swagger.annotation.EnablePigDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author lengleng
 * @date 2018年06月21日
 * <p>
 * 用户统一管理系统
 */
@EnablePigDoc("admin")
@EnablePigResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class PigAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(PigAdminApplication.class, args);
	}

}
