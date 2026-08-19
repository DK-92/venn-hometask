package ca.venn.hometask.adapter.in.file;

import ca.venn.hometask.adapter.in.file.dto.LoadRequestJson;
import ca.venn.hometask.adapter.in.file.dto.LoadResultJson;
import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import org.springframework.stereotype.Component;

/**
 * Maps between the file adapter's raw JSON DTOs and domain models.
 *
 * <p>Logic to be implemented:
 * <ul>
 *   <li>Parse {@link LoadRequestJson#loadAmount()} (e.g. {@code "$123.45"}) into a
 *       {@link java.math.BigDecimal}</li>
 *   <li>Parse {@link LoadRequestJson#time()} (ISO-8601, e.g.
 *       {@code "2018-01-01T00:00:00Z"}) into an {@link java.time.Instant}</li>
 * </ul>
 */
@Component
public class LoadJsonMapper {

    public LoadRequest toDomain(LoadRequestJson json) {
        // TODO: parse currency string and ISO-8601 timestamp
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public LoadResultJson toJson(LoadResult result) {
        return new LoadResultJson(result.id(), result.customerId(), result.accepted());
    }
}
