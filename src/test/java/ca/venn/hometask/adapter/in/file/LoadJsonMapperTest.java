package ca.venn.hometask.adapter.in.file;

import ca.venn.hometask.adapter.in.file.dto.LoadResultJson;
import ca.venn.hometask.domain.model.LoadResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoadJsonMapper}.
 */
class LoadJsonMapperTest {

    private final LoadJsonMapper mapper = new LoadJsonMapper();

    @Test
    @Disabled("TODO: implement once LoadJsonMapper#toDomain parses currency and timestamp")
    void parsesCurrencyStringAndTimestampIntoDomainRequest() {
        // e.g. new LoadRequestJson("1234", "1234", "$123.45", "2018-01-01T00:00:00Z")
        // should map to a LoadRequest with loadAmount = 123.45 and the corresponding Instant
    }

    @Test
    void mapsDomainResultToJsonDto() {
        LoadResult result = new LoadResult("1234", "5678", true);

        LoadResultJson json = mapper.toJson(result);

        assertThat(json.id()).isEqualTo("1234");
        assertThat(json.customerId()).isEqualTo("5678");
        assertThat(json.accepted()).isTrue();
    }
}
