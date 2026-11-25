package com.capgemini.test.code.clients;

import feign.Client;
import feign.Logger;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Client feignClient() {
        return new feign.okhttp.OkHttpClient(new OkHttpClient());
    }

    @Bean(name = "customLogger")
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Puedes ajustar el nivel de los logs según tu necesidad
    }
}