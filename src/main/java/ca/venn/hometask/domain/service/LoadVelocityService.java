package ca.venn.hometask.domain.service;

import ca.venn.hometask.domain.model.LoadRecord;
import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import ca.venn.hometask.domain.model.VelocityLimitsProperties;
import ca.venn.hometask.domain.port.in.ProcessLoadUseCase;
import ca.venn.hometask.domain.port.out.LoadRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

@Service
public class LoadVelocityService implements ProcessLoadUseCase {

    private final VelocityLimitsProperties velocityLimitsProperties;
    private final LoadRecordRepository loadRecordRepository;

    public LoadVelocityService(
            VelocityLimitsProperties velocityLimitsProperties,
            LoadRecordRepository loadRecordRepository
    ) {
        this.velocityLimitsProperties = velocityLimitsProperties;
        this.loadRecordRepository = loadRecordRepository;
    }

    @Override
    public Optional<LoadResult> process(LoadRequest request) {
        if (loadRecordRepository.existsByIdAndCustomerId(request.id(), request.customerId())) {
            return Optional.empty();
        }

        var accepted = true;

        var startOfDay = request.time().truncatedTo(ChronoUnit.DAYS);
        var endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        var dailyCount = loadRecordRepository.countAccepted(request.customerId(), startOfDay, endOfDay);
        if (dailyCount + 1 > velocityLimitsProperties.maxDailyLoadCount()) {
            accepted = false;
        }

        var dailyAmount = loadRecordRepository.sumAcceptedAmount(request.customerId(), startOfDay, endOfDay);
        if (dailyAmount.add(request.loadAmount()).compareTo(velocityLimitsProperties.maxDailyLoadAmount()) > 0) {
            accepted = false;
        }

        ZonedDateTime zdt = ZonedDateTime.ofInstant(request.time(), ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS);

        var startOfWeek = zdt.toInstant();
        var endOfWeek = zdt.plusDays(7).toInstant();
        var weeklySum = loadRecordRepository.sumAcceptedAmount(request.customerId(), startOfWeek, endOfWeek);
        if (weeklySum.add(request.loadAmount()).compareTo(velocityLimitsProperties.maxWeeklyLoadAmount()) > 0) {
            accepted = false;
        }

        var newRecord = new LoadRecord(
                request.id(),
                request.customerId(),
                request.loadAmount(),
                request.time(),
                accepted
        );
        loadRecordRepository.save(newRecord);

        return Optional.of(new LoadResult(request.id(), request.customerId(), accepted));
    }
}
