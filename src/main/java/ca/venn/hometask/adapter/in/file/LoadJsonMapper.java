package ca.venn.hometask.adapter.in.file;

import ca.venn.hometask.adapter.in.file.dto.LoadRequestJson;
import ca.venn.hometask.adapter.in.file.dto.LoadResultJson;
import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class LoadJsonMapper {

    public LoadRequest toDomain(LoadRequestJson json) {
        // TODO: parse currency string and ISO-8601 timestamp
        var loadAmount = new BigDecimal(json.loadAmount().replace("$", ""));

        return new LoadRequest(
                json.id(),
                json.customerId(),
                loadAmount,
                Instant.parse(json.time())
        );
    }

    public LoadResultJson toJson(LoadResult result) {
        return new LoadResultJson(result.id(), result.customerId(), result.accepted());
    }
}
