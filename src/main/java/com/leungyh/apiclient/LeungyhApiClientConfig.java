package com.leungyh.apiclient;

import com.leungyh.apiclient.client.LeungyhApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leungyh
 */
@Configuration
@ConfigurationProperties("leungyh-api.client")
@Data
@ComponentScan
public class LeungyhApiClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public LeungyhApiClient leungyhApiClient() {
        return new LeungyhApiClient(accessKey, secretKey);
    }

}
