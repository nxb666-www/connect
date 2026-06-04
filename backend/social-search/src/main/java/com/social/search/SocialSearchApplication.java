package com.social.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.social.search.mapper")
@ComponentScan(basePackages = {"com.social.search", "com.social.common"})
public class SocialSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialSearchApplication.class, args);
    }
}
