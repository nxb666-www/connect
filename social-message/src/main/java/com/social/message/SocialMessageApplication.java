package com.social.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.social.message.mapper")
@ComponentScan(basePackages = {"com.social.message", "com.social.common"})
public class SocialMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialMessageApplication.class, args);
    }
}
