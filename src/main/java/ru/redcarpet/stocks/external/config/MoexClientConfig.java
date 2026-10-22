package ru.redcarpet.stocks.external.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(MoexClientProperties.class)
public class MoexClientConfig {

    @Bean
    public RestClient moexClient(MoexClientProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.connectionDurationSec()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.readDurationSec()));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(properties.url())
                .defaultHeader(HttpHeaders.USER_AGENT, "SpringBoot-moex-clietn")
                .build();
    }
}
