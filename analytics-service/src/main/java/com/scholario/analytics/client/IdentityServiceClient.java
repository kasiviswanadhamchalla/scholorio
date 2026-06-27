package com.scholario.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service", path = "/internal/users")
public interface IdentityServiceClient {

    @GetMapping("/by-id/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}
