package ca.venn.hometask.domain.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "velocity.limits")
public record VelocityLimitsProperties(
        BigDecimal maxDailyLoadAmount,
        BigDecimal maxWeeklyLoadAmount,
        int maxDailyLoadCount
) { }
