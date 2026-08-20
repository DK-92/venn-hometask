package ca.venn.hometask.domain.service;

import ca.venn.hometask.domain.model.LoadRecord;
import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import ca.venn.hometask.domain.model.VelocityLimitsProperties;
import ca.venn.hometask.domain.port.out.LoadRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadVelocityServiceTest {

    @Mock
    private LoadRecordRepository loadRecordRepository;

    private LoadVelocityService service;

    @BeforeEach
    void setUp() {
        service = new LoadVelocityService(defaultVelocityLimits(), loadRecordRepository);
    }

    @Test
    void acceptsLoadWithinAllLimits() {
        // given
        stubLimits("100", 0, "0.00", "0.00");
        LoadRequest request = loadRequest("1", "100", "100.00", Instant.parse("2023-06-14T15:30:00Z"));

        // when
        Optional<LoadResult> result = service.process(request);

        // then
        assertThat(result).contains(new LoadResult("1", "100", true));
        verify(loadRecordRepository).save(new LoadRecord("1", "100", new BigDecimal("100.00"), request.time(), true));
    }

    @Test
    void acceptsLoadWhenExactlyAtDailyAmountLimit() {
        // given
        stubLimits("100", 0, "4900.00", "0.00");
        LoadRequest request = loadRequest("1", "100", "100.00", Instant.parse("2023-06-14T15:30:00Z"));

        // when
        Optional<LoadResult> result = service.process(request);

        // then
        assertThat(result).contains(new LoadResult("1", "100", true));
    }

    @Test
    void declinesLoadExceedingDailyAmountLimit() {
        // given
        stubLimits("100", 0, "4901.00", "0.00");
        LoadRequest request = loadRequest("1", "100", "100.00", Instant.parse("2023-06-14T15:30:00Z"));

        // when
        Optional<LoadResult> result = service.process(request);

        // then
        assertThat(result).contains(new LoadResult("1", "100", false));
        verify(loadRecordRepository).save(new LoadRecord("1", "100", new BigDecimal("100.00"), request.time(), false));
    }

    @Test
    void declinesLoadExceedingWeeklyAmountLimit() {
        // given
        stubLimits("100", 0, "0.00", "19901.00");
        LoadRequest request = loadRequest("1", "100", "100.00", Instant.parse("2023-06-14T15:30:00Z"));

        // when
        Optional<LoadResult> result = service.process(request);

        // then
        assertThat(result).contains(new LoadResult("1", "100", false));
    }

    @Test
    void declinesFourthLoadAttemptInSingleDay() {
        // given
        stubLimits("100", 3, "0.00", "0.00");
        LoadRequest request = loadRequest("4", "100", "10.00", Instant.parse("2023-06-14T15:30:00Z"));

        // when
        Optional<LoadResult> result = service.process(request);

        // then
        assertThat(result).contains(new LoadResult("4", "100", false));
    }

    @Test
    void ignoresDuplicateLoadIdForSameCustomer() {
        // given
        when(loadRecordRepository.existsByIdAndCustomerId("1", "100")).thenReturn(true);
        LoadRequest request = loadRequest("1", "100", "10.00", Instant.parse("2023-06-14T15:30:00Z"));

        // when
        Optional<LoadResult> result = service.process(request);

        // then
        assertThat(result).isEmpty();
        verify(loadRecordRepository, never()).save(any());
        verify(loadRecordRepository, never()).countAccepted(any(), any(), any());
        verify(loadRecordRepository, never()).sumAcceptedAmount(any(), any(), any());
    }

    @Test
    void queriesDailyAndWeeklyWindowsUsingUtcDayAndMondayWeekBoundaries() {
        // given
        stubLimits("100", 0, "0.00", "0.00");
        LoadRequest request = loadRequest("1", "100", "10.00", Instant.parse("2023-06-14T15:30:00Z"));
        Instant expectedStartOfDay = Instant.parse("2023-06-14T00:00:00Z");
        Instant expectedEndOfDay = Instant.parse("2023-06-15T00:00:00Z");
        Instant expectedStartOfWeek = Instant.parse("2023-06-12T00:00:00Z");
        Instant expectedEndOfWeek = Instant.parse("2023-06-19T00:00:00Z");

        // when
        service.process(request);

        // then
        verify(loadRecordRepository).countAccepted("100", expectedStartOfDay, expectedEndOfDay);

        ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(loadRecordRepository, times(2))
                .sumAcceptedAmount(eq("100"), startCaptor.capture(), endCaptor.capture());

        assertThat(startCaptor.getAllValues()).containsExactly(expectedStartOfDay, expectedStartOfWeek);
        assertThat(endCaptor.getAllValues()).containsExactly(expectedEndOfDay, expectedEndOfWeek);
    }

    private void stubLimits(String customerId, long dailyCount, String dailyAmount, String weeklyAmount) {
        when(loadRecordRepository.countAccepted(eq(customerId), any(), any())).thenReturn(dailyCount);
        when(loadRecordRepository.sumAcceptedAmount(eq(customerId), any(), any())).thenAnswer(invocation -> {
            Instant start = invocation.getArgument(1);
            Instant end = invocation.getArgument(2);
            boolean isDailyWindow = start.until(end, ChronoUnit.DAYS) == 1;
            return isDailyWindow ? new BigDecimal(dailyAmount) : new BigDecimal(weeklyAmount);
        });
    }

    private VelocityLimitsProperties defaultVelocityLimits() {
        return new VelocityLimitsProperties(new BigDecimal("5000.00"), new BigDecimal("20000.00"), 3);
    }

    private LoadRequest loadRequest(String id, String customerId, String amount, Instant time) {
        return new LoadRequest(id, customerId, new BigDecimal(amount), time);
    }
}
