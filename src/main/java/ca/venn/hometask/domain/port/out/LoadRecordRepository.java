package ca.venn.hometask.domain.port.out;

import ca.venn.hometask.domain.model.LoadRecord;

import java.math.BigDecimal;
import java.time.Instant;

public interface LoadRecordRepository {

    boolean existsByIdAndCustomerId(String id, String customerId);

    void save(LoadRecord record);

    BigDecimal sumAcceptedAmount(String customerId, Instant periodStart, Instant periodEnd);

    long countAccepted(String customerId, Instant periodStart, Instant periodEnd);
}
