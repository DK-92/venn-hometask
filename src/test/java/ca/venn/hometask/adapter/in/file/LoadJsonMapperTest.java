package ca.venn.hometask.adapter.in.file;

import ca.venn.hometask.adapter.in.file.dto.LoadRequestJson;
import ca.venn.hometask.adapter.in.file.dto.LoadResultJson;
import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoadJsonMapperTest {

    private final LoadJsonMapper mapper = new LoadJsonMapper();

    @Test
    void mapsIdAndCustomerIdUnchanged() {
        // given
        LoadRequestJson json = aLoadRequestJson("1234", "5678", "$123.45", "2018-01-01T00:00:00Z");

        // when
        LoadRequest request = mapper.toDomain(json);

        // then
        assertThat(request.id()).isEqualTo("1234");
        assertThat(request.customerId()).isEqualTo("5678");
    }

    @Test
    void parsesDollarPrefixedAmountToBigDecimal() {
        // given
        LoadRequestJson json = aLoadRequestJson("1234", "5678", "$123.45", "2018-01-01T00:00:00Z");

        // when
        LoadRequest request = mapper.toDomain(json);

        // then
        assertThat(request.loadAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void invalidDollarAmountThrowsException() {
        // given
        LoadRequestJson json = aLoadRequestJson("1234", "5678", "123.4567", "2018-01-01T00:00:00Z");

        // when / then
        assertThatThrownBy(() -> mapper.toDomain(json))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Rounding necessary");
    }

    @Test
    void parsesIsoTimestampToInstant() {
        // given
        LoadRequestJson json = aLoadRequestJson("1234", "5678", "$123.45", "2018-01-01T00:00:00Z");

        // when
        LoadRequest request = mapper.toDomain(json);

        // then
        assertThat(request.time()).isEqualTo(Instant.parse("2018-01-01T00:00:00Z"));
    }

    @Test
    void throwsExceptionForNonNumericLoadAmount() {
        // given
        LoadRequestJson json = aLoadRequestJson("1234", "5678", "$abc", "2018-01-01T00:00:00Z");

        // when / then
        assertThatThrownBy(() -> mapper.toDomain(json))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void throwsExceptionForInvalidTimestampFormat() {
        // given
        LoadRequestJson json = aLoadRequestJson("1234", "5678", "$123.45", "not-a-date");

        // when / then
        assertThatThrownBy(() -> mapper.toDomain(json))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void mapsAcceptedResultToJson() {
        // given
        LoadResult result = new LoadResult("1234", "5678", true);

        // when
        LoadResultJson json = mapper.toJson(result);

        // then
        assertThat(json.id()).isEqualTo("1234");
        assertThat(json.customerId()).isEqualTo("5678");
        assertThat(json.accepted()).isTrue();
    }

    @Test
    void mapsDeclinedResultToJson() {
        // given
        LoadResult result = new LoadResult("1234", "5678", false);

        // when
        LoadResultJson json = mapper.toJson(result);

        // then
        assertThat(json.accepted()).isFalse();
    }

    private static LoadRequestJson aLoadRequestJson(String id, String customerId, String loadAmount, String time) {
        return new LoadRequestJson(id, customerId, loadAmount, time);
    }
}