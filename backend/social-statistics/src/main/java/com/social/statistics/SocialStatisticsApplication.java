package com.social.statistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("com.social.statistics.mapper")
@ComponentScan(basePackages = {"com.social.statistics", "com.social.common"})
public class SocialStatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialStatisticsApplication.class, args);
    }
}
