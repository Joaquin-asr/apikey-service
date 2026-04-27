package com.example.apikeyservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
    title = "API Key Service",
    version = "1.0",
    description = "Servicio para generación, validación y renovación segura de API Keys con cifrado AES-GCM y control de expiración."
))
public class ApiKeyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiKeyServiceApplication.class, args);
    }
}
