package com.bankhub.hubia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HubIaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubIaServiceApplication.class, args);
    }
}
