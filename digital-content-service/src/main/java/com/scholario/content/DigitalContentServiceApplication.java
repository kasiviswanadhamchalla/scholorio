package com.scholario.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.scholario.content.client")
public class DigitalContentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DigitalContentServiceApplication.class, args);
    }
}
