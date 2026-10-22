package ru.redcarpet.stocks.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "moex")
public record MoexClientProperties(
        String url,
        long connectionDurationSec,
        long readDurationSec
) {}
