package ca.venn.hometask.adapter.out.persistence;

import ca.venn.hometask.domain.model.LoadRecord;
import ca.venn.hometask.domain.port.out.LoadRecordRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class LoadRepositoryAdapter implements LoadRecordRepository {

    private final SpringDataLoadJpaRepository jpaRepository;

    public LoadRepositoryAdapter(SpringDataLoadJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByIdAndCustomerId(String id, String customerId) {
        return jpaRepository.existsByLoadIdAndCustomerId(id, customerId);
    }

    @Override
    public void save(LoadRecord record) {
        jpaRepository.save(new LoadJpaEntity(
                record.id(),
                record.customerId(),
                record.loadAmount(),
                record.time(),
                record.accepted()));
    }

    @Override
    public BigDecimal sumAcceptedAmount(String customerId, Instant periodStart, Instant periodEnd) {
        return jpaRepository.sumAcceptedAmount(customerId, periodStart, periodEnd);
    }

    @Override
    public long countAccepted(String customerId, Instant periodStart, Instant periodEnd) {
        return jpaRepository.countAccepted(customerId, periodStart, periodEnd);
    }
}
