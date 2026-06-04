package com.social.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.social.user.mapper")
@ComponentScan(basePackages = {"com.social.user", "com.social.common"})
public class SocialUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialUserApplication.class, args);
    }
}
