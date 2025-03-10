package com.happymarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.happymarket", "mongoDB", "controlador", "servicio"})
public class HappyMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(HappyMarketApplication.class, args);
    }
    
}
