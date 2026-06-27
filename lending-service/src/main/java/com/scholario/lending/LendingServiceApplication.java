package com.scholario.lending;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.scholario.lending.client")
public class LendingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LendingServiceApplication.class, args);
    }
}
