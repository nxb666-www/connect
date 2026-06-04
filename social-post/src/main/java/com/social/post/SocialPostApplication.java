package com.social.post;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.social.post.mapper")
@ComponentScan(basePackages = {"com.social.post", "com.social.common"})
public class SocialPostApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialPostApplication.class, args);
    }
}
