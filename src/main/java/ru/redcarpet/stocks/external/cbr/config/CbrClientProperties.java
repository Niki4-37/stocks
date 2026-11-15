package ru.redcarpet.stocks.external.cbr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cbr.client")
public record CbrClientProperties(
        String url,
        int connectionDurationSec,
        int readDurationSec
) {}