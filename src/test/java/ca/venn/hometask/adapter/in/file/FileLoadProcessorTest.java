package ca.venn.hometask.adapter.in.file;

import ca.venn.hometask.adapter.in.file.dto.LoadRequestJson;
import ca.venn.hometask.adapter.in.file.dto.LoadResultJson;
import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import ca.venn.hometask.domain.port.in.ProcessLoadUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FileLoadProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void writesOneResultLinePerProcessedLoad() throws Exception {
        // given
        String input = """
                {"id":"1","customer_id":"100","load_amount":"$100.00","time":"2018-01-01T00:00:00Z"}
                {"id":"2","customer_id":"100","load_amount":"$200.00","time":"2018-01-01T00:01:00Z"}
                """;
        ProcessLoadUseCase useCase = mock(ProcessLoadUseCase.class);
        when(useCase.process(any(LoadRequest.class)))
                .thenReturn(Optional.of(new LoadResult("1", "100", true)))
                .thenReturn(Optional.of(new LoadResult("2", "100", false)));
        FileLoadProcessor processor = aProcessor(input, useCase, aPassthroughMapper());
        Path outputFile = tempDir.resolve("output.txt");

        // when
        processor.run();

        // then
        List<String> lines = readOutputLines(outputFile);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("\"id\":\"1\"").contains("\"accepted\":true");
        assertThat(lines.get(1)).contains("\"id\":\"2\"").contains("\"accepted\":false");
    }

    @Test
    void skipsLineWhenUseCaseReturnsEmptyOptional() throws Exception {
        // given
        String input = """
                {"id":"1","customer_id":"100","load_amount":"$100.00","time":"2018-01-01T00:00:00Z"}
                {"id":"1","customer_id":"100","load_amount":"$100.00","time":"2018-01-01T00:02:00Z"}
                {"id":"2","customer_id":"100","load_amount":"$200.00","time":"2018-01-01T00:03:00Z"}
                """;
        ProcessLoadUseCase useCase = mock(ProcessLoadUseCase.class);
        when(useCase.process(any(LoadRequest.class)))
                .thenReturn(Optional.of(new LoadResult("1", "100", true)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new LoadResult("2", "100", true)));
        FileLoadProcessor processor = aProcessor(input, useCase, aPassthroughMapper());
        Path outputFile = tempDir.resolve("output.txt");

        // when
        processor.run();

        // then
        List<String> lines = readOutputLines(outputFile);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("\"id\":\"1\"");
        assertThat(lines.get(1)).contains("\"id\":\"2\"");
    }

    @Test
    void skipsBlankLines() throws Exception {
        // given
        String input = """
                {"id":"1","customer_id":"100","load_amount":"$100.00","time":"2018-01-01T00:00:00Z"}
                   \s
                {"id":"2","customer_id":"100","load_amount":"$200.00","time":"2018-01-01T00:01:00Z"}
                """;
        ProcessLoadUseCase useCase = mock(ProcessLoadUseCase.class);
        when(useCase.process(any(LoadRequest.class)))
                .thenReturn(Optional.of(new LoadResult("1", "100", true)))
                .thenReturn(Optional.of(new LoadResult("2", "100", true)));
        FileLoadProcessor processor = aProcessor(input, useCase, aPassthroughMapper());
        Path outputFile = tempDir.resolve("output.txt");

        // when
        processor.run();

        // then
        List<String> lines = readOutputLines(outputFile);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("\"id\":\"1\"");
        assertThat(lines.get(1)).contains("\"id\":\"2\"");
    }

    @Test
    void continuesProcessingAfterMalformedLine() throws Exception {
        // given
        String input = """
                {"id":"1","customer_id":"100","load_amount":"$100.00","time":"2018-01-01T00:00:00Z"}
                { this is not valid json
                {"id":"2","customer_id":"100","load_amount":"$200.00","time":"2018-01-01T00:01:00Z"}
                """;
        ProcessLoadUseCase useCase = mock(ProcessLoadUseCase.class);
        when(useCase.process(any(LoadRequest.class)))
                .thenReturn(Optional.of(new LoadResult("1", "100", true)))
                .thenReturn(Optional.of(new LoadResult("2", "100", true)));
        FileLoadProcessor processor = aProcessor(input, useCase, aPassthroughMapper());
        Path outputFile = tempDir.resolve("output.txt");

        // when
        processor.run();

        // then
        List<String> lines = readOutputLines(outputFile);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("\"id\":\"1\"");
        assertThat(lines.get(1)).contains("\"id\":\"2\"");
    }

    @Test
    void writesNoOutputWhenInputIsEmpty() throws Exception {
        // given
        String input = "";
        ProcessLoadUseCase useCase = mock(ProcessLoadUseCase.class);
        FileLoadProcessor processor = aProcessor(input, useCase, aPassthroughMapper());
        Path outputFile = tempDir.resolve("output.txt");

        // when
        processor.run();

        // then
        List<String> lines = readOutputLines(outputFile);
        assertThat(lines).isEmpty();
        verifyNoInteractions(useCase);
    }

    private FileLoadProcessor aProcessor(String input, ProcessLoadUseCase useCase, LoadJsonMapper loadJsonMapper) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource resource = new ByteArrayResource(input.getBytes(StandardCharsets.UTF_8));
        when(resourceLoader.getResource(any())).thenReturn(resource);

        return new FileLoadProcessor(
                useCase, loadJsonMapper, objectMapper, resourceLoader,
                "classpath:input.txt", tempDir.resolve("output.txt").toString());
    }

    /**
     * A {@link LoadJsonMapper} mock that behaves like the real mapper for the
     * purposes of these orchestration tests: it converts the raw parsed JSON id
     * into a domain {@link LoadRequest} carrying the same id, and converts a
     * {@link LoadResult} back into a {@link LoadResultJson} with matching fields.
     */
    private LoadJsonMapper aPassthroughMapper() {
        LoadJsonMapper loadJsonMapper = mock(LoadJsonMapper.class);
        when(loadJsonMapper.toDomain(any())).thenAnswer(invocation -> {
            LoadRequestJson json = invocation.getArgument(0);
            return new LoadRequest(json.id(), json.customerId(), BigDecimal.TEN, Instant.now());
        });
        when(loadJsonMapper.toJson(any(LoadResult.class))).thenAnswer(invocation -> {
            LoadResult result = invocation.getArgument(0);
            return new LoadResultJson(result.id(), result.customerId(), result.accepted());
        });
        return loadJsonMapper;
    }

    private List<String> readOutputLines(Path outputFile) throws Exception {
        return Files.readAllLines(outputFile);
    }
}
