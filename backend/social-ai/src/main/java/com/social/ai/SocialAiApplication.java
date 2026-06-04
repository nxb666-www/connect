package com.social.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.social.ai.mapper")
@ComponentScan(basePackages = {"com.social.ai", "com.social.common"})
public class SocialAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialAiApplication.class, args);
    }
}
