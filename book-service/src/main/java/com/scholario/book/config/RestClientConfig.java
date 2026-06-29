package com.scholario.book.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .filter((request, next) -> {
                    try {
                        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                        if (attributes != null) {
                            String authHeader = attributes.getRequest().getHeader("Authorization");
                            if (authHeader != null) {
                                return next.exchange(
                                        ClientRequest.from(request)
                                                .header("Authorization", authHeader)
                                                .build()
                                );
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    return next.exchange(request);
                });
    }
}
