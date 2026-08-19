package ca.venn.hometask.adapter.out.persistence;

import ca.venn.hometask.domain.model.LoadRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link LoadRepositoryAdapter} against an in-memory H2 database.
 */
@DataJpaTest
class LoadRepositoryAdapterTest {

    @Autowired
    private SpringDataLoadJpaRepository springDataLoadJpaRepository;

    private LoadRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LoadRepositoryAdapter(springDataLoadJpaRepository);
    }

    @Test
    void savesAndDetectsExistingLoadRecordByIdAndCustomer() {
        Instant now = Instant.parse("2018-01-01T00:00:00Z");
        adapter.save(new LoadRecord("1", "100", new BigDecimal("50.00"), now, true));

        assertThat(adapter.existsByIdAndCustomerId("1", "100")).isTrue();
        assertThat(adapter.existsByIdAndCustomerId("2", "100")).isFalse();
        assertThat(adapter.existsByIdAndCustomerId("1", "999")).isFalse();
    }

    @Test
    void sumsAcceptedAmountsWithinTimeRangeOnly() {
        Instant dayStart = Instant.parse("2018-01-01T00:00:00Z");
        Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

        adapter.save(new LoadRecord("1", "100", new BigDecimal("100.00"), dayStart, true));
        adapter.save(new LoadRecord("2", "100", new BigDecimal("200.00"), dayStart.plusSeconds(60), true));
        adapter.save(new LoadRecord("3", "100", new BigDecimal("300.00"), dayStart, false));
        adapter.save(new LoadRecord("4", "100", new BigDecimal("999.00"), dayEnd, true));

        BigDecimal sum = adapter.sumAcceptedAmount("100", dayStart, dayEnd);

        assertThat(sum).isEqualByComparingTo("300.00");
    }

    @Test
    void countsAcceptedLoadsWithinTimeRangeOnly() {
        Instant dayStart = Instant.parse("2018-01-01T00:00:00Z");
        Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

        adapter.save(new LoadRecord("1", "100", new BigDecimal("100.00"), dayStart, true));
        adapter.save(new LoadRecord("2", "100", new BigDecimal("200.00"), dayStart.plusSeconds(60), true));
        adapter.save(new LoadRecord("3", "100", new BigDecimal("300.00"), dayStart, false));
        adapter.save(new LoadRecord("4", "100", new BigDecimal("999.00"), dayEnd, true));

        long count = adapter.countAccepted("100", dayStart, dayEnd);

        assertThat(count).isEqualTo(2);
    }
}
