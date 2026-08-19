package ca.venn.hometask.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity persisting a customer's fund load record for velocity limit tracking.
 *
 * <p>The primary key is the composite of {@code loadId} and {@code customerId},
 * since load ids are only guaranteed unique per customer.
 */
@Entity
@Table(name = "load_records", indexes = {
        @Index(name = "idx_load_records_customer_time", columnList = "customerId,time")
})
public class LoadJpaEntity {

    @Id
    @Column(nullable = false)
    private String loadId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal loadAmount;

    @Column(nullable = false)
    private Instant time;

    @Column(nullable = false)
    private boolean accepted;

    protected LoadJpaEntity() {
        // required by JPA
    }

    public LoadJpaEntity(String loadId, String customerId, BigDecimal loadAmount, Instant time, boolean accepted) {
        this.loadId = loadId;
        this.customerId = customerId;
        this.loadAmount = loadAmount;
        this.time = time;
        this.accepted = accepted;
    }

    public String getLoadId() {
        return loadId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getLoadAmount() {
        return loadAmount;
    }

    public Instant getTime() {
        return time;
    }

    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoadJpaEntity that)) {
            return false;
        }
        return Objects.equals(loadId, that.loadId) && Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loadId, customerId);
    }
}
