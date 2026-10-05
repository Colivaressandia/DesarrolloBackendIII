package com.bancoxyz.banco_microservicio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class BancoMicroservicioApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoMicroservicioApplication.class, args);
    }
}