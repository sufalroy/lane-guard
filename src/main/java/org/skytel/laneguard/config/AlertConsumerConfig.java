package org.skytel.laneguard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AlertConsumerConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
