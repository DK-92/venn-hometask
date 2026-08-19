package ca.venn.hometask.domain.service;

import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import ca.venn.hometask.domain.port.in.ProcessLoadUseCase;
import ca.venn.hometask.domain.port.out.LoadRecordRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Core domain service implementing {@link ProcessLoadUseCase}.
 *
 * <p>Evaluates each incoming {@link LoadRequest} against the customer's daily
 * amount limit, weekly amount limit, and daily count limit (see
 * {@link ca.venn.hometask.domain.model.VelocityLimits}), then persists the
 * outcome via {@link LoadRecordRepository} so it is accounted for in
 * subsequent evaluations.
 *
 * <p>Business logic to be implemented:
 * <ul>
 *   <li>Ignore (return {@link Optional#empty()}) requests whose id has already
 *       been seen for the given customer</li>
 *   <li>Compute the current UTC day and ISO week (Mon-Sun) boundaries for the
 *       request's timestamp</li>
 *   <li>Determine acceptance based on
 *       {@link ca.venn.hometask.domain.model.VelocityLimits}</li>
 *   <li>Persist a {@link ca.venn.hometask.domain.model.LoadRecord} reflecting
 *       the outcome (accepted or declined)</li>
 * </ul>
 */
@Service
public class LoadVelocityService implements ProcessLoadUseCase {

    private final LoadRecordRepository loadRecordRepository;

    public LoadVelocityService(LoadRecordRepository loadRecordRepository) {
        this.loadRecordRepository = loadRecordRepository;
    }

    @Override
    public Optional<LoadResult> process(LoadRequest request) {
        // TODO: implement velocity limit evaluation logic
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
