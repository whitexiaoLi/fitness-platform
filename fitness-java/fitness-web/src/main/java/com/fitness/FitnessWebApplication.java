package com.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fitness.mapper")
public class FitnessWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(FitnessWebApplication.class, args);
    }
}
