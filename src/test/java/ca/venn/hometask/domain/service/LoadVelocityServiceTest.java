package ca.venn.hometask.domain.service;

import ca.venn.hometask.domain.port.out.LoadRecordRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link LoadVelocityService}.
 *
 * <p>Scenarios to cover once the business logic is implemented:
 * <ul>
 *   <li>Accepts a load within all limits</li>
 *   <li>Declines a load that would exceed the daily amount limit ($5,000/day)</li>
 *   <li>Declines a load that would exceed the weekly amount limit ($20,000/week,
 *       Mon-Sun UTC)</li>
 *   <li>Declines a 4th load attempt in a single UTC day, regardless of amount</li>
 *   <li>Ignores (returns {@code Optional.empty()}) a load whose id has already
 *       been seen for the same customer</li>
 *   <li>Correctly resets daily/weekly counters across day and week boundaries
 *       (UTC midnight, Monday week start)</li>
 *   <li>Persists a {@link ca.venn.hometask.domain.model.LoadRecord} reflecting
 *       the outcome (accepted or declined) via {@link LoadRecordRepository}</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class LoadVelocityServiceTest {

    @Mock
    private LoadRecordRepository loadRecordRepository;

    @Test
    @Disabled("TODO: implement once LoadVelocityService business logic is written")
    void acceptsLoadWithinAllLimits() {
        // TODO
    }

    @Test
    @Disabled("TODO: implement once LoadVelocityService business logic is written")
    void declinesLoadExceedingDailyAmountLimit() {
        // TODO
    }

    @Test
    @Disabled("TODO: implement once LoadVelocityService business logic is written")
    void declinesLoadExceedingWeeklyAmountLimit() {
        // TODO
    }

    @Test
    @Disabled("TODO: implement once LoadVelocityService business logic is written")
    void declinesFourthLoadAttemptInSingleDay() {
        // TODO
    }

    @Test
    @Disabled("TODO: implement once LoadVelocityService business logic is written")
    void ignoresDuplicateLoadIdForSameCustomer() {
        // TODO
    }
}
