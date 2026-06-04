package com.wxai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wxai.mapper")
public class WxAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WxAiApplication.class, args);
	}

}
