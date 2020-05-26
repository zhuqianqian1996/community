package com.nowcoder.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@MapperScan(basePackages = "com.nowcoder.community.dao")
@SpringBootApplication
@ComponentScan(basePackages = "com.nowcoder.community.*")
public class CommunityApplication {

	@PostConstruct
	public void init(){
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}
}

