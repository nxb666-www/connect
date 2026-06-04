package com.social.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SocialGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialGatewayApplication.class, args);
    }
}
