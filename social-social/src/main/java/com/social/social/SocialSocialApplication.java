package com.social.social;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.social.social.mapper")
@ComponentScan(basePackages = {"com.social.social", "com.social.common"})
public class SocialSocialApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialSocialApplication.class, args);
    }
}
