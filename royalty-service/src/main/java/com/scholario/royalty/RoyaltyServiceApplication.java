package com.scholario.royalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.scholario.royalty.client")
public class RoyaltyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoyaltyServiceApplication.class, args);
    }
}
