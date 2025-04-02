package com.digital.boot;

import com.digital.common.security.annotation.EnablePigResourceServer;
import com.digital.common.swagger.annotation.EnablePigDoc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author lengleng 单体版本启动器，只需要运行此模块则整个系统启动
 */
@EnablePigDoc(value = "admin", isMicro = false)
@EnablePigResourceServer
@SpringBootApplication
@ComponentScan(basePackages = {"com.digital"})
@MapperScan({"com.digital.**.mapper"})
public class DigitalBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalBootApplication.class, args);
	}
}
