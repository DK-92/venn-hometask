package ca.venn.hometask.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record LoadRecord(
        String id,
        String customerId,
        BigDecimal loadAmount,
        Instant time,
        boolean accepted) {
}
