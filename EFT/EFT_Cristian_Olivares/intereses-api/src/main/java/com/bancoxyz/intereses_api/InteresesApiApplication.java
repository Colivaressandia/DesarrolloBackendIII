package com.bancoxyz.intereses_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InteresesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteresesApiApplication.class, args);
    }

}
