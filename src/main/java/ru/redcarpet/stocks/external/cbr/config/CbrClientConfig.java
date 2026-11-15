package ru.redcarpet.stocks.external.cbr.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableConfigurationProperties(CbrClientProperties.class)
public class CbrClientConfig {

    @Bean
    public RestClient cbrClient(CbrClientProperties properties) throws Exception {
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();
        byteArrayConverter.setSupportedMediaTypes(List.of(MediaType.ALL));

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = new ClassPathResource("cbr-truststore.p12").getInputStream()) {
            trustStore.load(is, "changeit".toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        HttpClient httpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(properties.connectionDurationSec()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory(httpClient);
        rf.setReadTimeout(Duration.ofSeconds(properties.readDurationSec()));

        return RestClient.builder()
                .requestFactory(rf)
                .baseUrl(properties.url())
                .defaultHeader(HttpHeaders.USER_AGENT, "SpringBoot-cbr-client")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .configureMessageConverters(converters -> converters.addCustomConverter(byteArrayConverter))
                .build();
    }
}