package ca.venn.hometask.domain.model;

import java.math.BigDecimal;

public final class VelocityLimits {

    public static final BigDecimal MAX_DAILY_LOAD_AMOUNT = new BigDecimal("5000.00");

    public static final BigDecimal MAX_WEEKLY_LOAD_AMOUNT = new BigDecimal("20000.00");

    public static final int MAX_DAILY_LOAD_COUNT = 3;

    private VelocityLimits() { }
}
