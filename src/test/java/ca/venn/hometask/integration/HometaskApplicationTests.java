package ca.venn.hometask.integration;

import ca.venn.hometask.adapter.in.file.dto.LoadResultJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HometaskApplicationTests {

    private static final Path OUTPUT_PATH = Path.of("build/integration-test-output.txt");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<LoadResultJson> results;

    @BeforeAll
    void readProcessedResults() throws IOException {
        results = new ArrayList<>();
        for (String line : Files.readAllLines(OUTPUT_PATH)) {
            results.add(objectMapper.readValue(line, LoadResultJson.class));
        }
    }

    @AfterAll
    void deleteOutputFile() throws IOException {
        Files.deleteIfExists(OUTPUT_PATH);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void writesAcceptedResultLineForValidLoadRequest() {
        // given: the shared pipeline has processed integration-test-input.jsonl, including
        // a single, well-within-limits load for "accept-1"

        // when
        Optional<LoadResultJson> result = findResult("accept-1");

        // then
        assertThat(result).contains(new LoadResultJson("accept-1", "cust-accept", true));
    }

    @Test
    void writesDeclinedResultLineWhenDailyAmountLimitExceeded() {
        // given: "cust-daily" has an accepted $4,950.00 balance followed by a $100.00 deposit
        // on the same day, pushing the daily total to $5,050.00 (exceeding the limit)

        // when
        Optional<LoadResultJson> firstResult = findResult("daily-1");
        Optional<LoadResultJson> secondResult = findResult("daily-2");

        // then
        assertThat(firstResult).contains(new LoadResultJson("daily-1", "cust-daily", true));
        assertThat(secondResult).contains(new LoadResultJson("daily-2", "cust-daily", false));
    }

    @Test
    void omitsOutputLineForDuplicateLoadId() {
        // given: "cust-dup" has two input lines sharing the same id "dup-1"

        // when
        long resultCountForDuplicateId = results.stream()
                .filter(result -> result.id().equals("dup-1"))
                .count();

        // then
        assertThat(resultCountForDuplicateId).isEqualTo(1);
        assertThat(findResult("dup-1")).contains(new LoadResultJson("dup-1", "cust-dup", true));
    }

    @Test
    void skipsBlankLinesWithoutError() {
        // given: the input file contains a blank line immediately before the after-blank entry

        // when
        Optional<LoadResultJson> result = findResult("after-blank");

        // then
        assertThat(result).contains(new LoadResultJson("after-blank", "cust-blank", true));
    }

    @Test
    void continuesProcessingAfterMalformedLine() {
        // given: the input file contains a malformed JSON line immediately before the after-malformed" entry

        // when
        Optional<LoadResultJson> result = findResult("after-malformed");

        // then
        assertThat(result).contains(new LoadResultJson("after-malformed", "cust-malformed", true));
    }

    private Optional<LoadResultJson> findResult(String id) {
        return results.stream()
                .filter(result -> result.id().equals(id))
                .findFirst();
    }
}
