package ca.venn.hometask.adapter.in.file;

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
import static org.mockito.Mockito.when;

/**
 * Tests the file reading/writing wiring of {@link FileLoadProcessor}, using a
 * mocked {@link ProcessLoadUseCase} so the test is independent of the (not yet
 * implemented) velocity limit business logic.
 */
class FileLoadProcessorTest {

    @TempDir
    Path tempDir;

    private static final String INPUT = """
            {"id":"1","customer_id":"100","load_amount":"$100.00","time":"2018-01-01T00:00:00Z"}
            {"id":"2","customer_id":"100","load_amount":"$200.00","time":"2018-01-01T00:01:00Z"}
            """;

    @Test
    void readsEachInputLineEvaluatesItAndWritesResultsSkippingEmptyOptionals() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource resource = new ByteArrayResource(INPUT.getBytes(StandardCharsets.UTF_8));
        when(resourceLoader.getResource(any())).thenReturn(resource);

        ProcessLoadUseCase useCase = mock(ProcessLoadUseCase.class);
        when(useCase.process(any(LoadRequest.class)))
                .thenReturn(Optional.of(new LoadResult("1", "100", true)))
                .thenReturn(Optional.empty());

        LoadJsonMapper loadJsonMapper = mock(LoadJsonMapper.class);
        when(loadJsonMapper.toDomain(any())).thenReturn(
                new LoadRequest("1", "100", BigDecimal.TEN, Instant.now()));
        when(loadJsonMapper.toJson(any(LoadResult.class)))
                .thenAnswer(invocation -> {
                    LoadResult r = invocation.getArgument(0);
                    return new ca.venn.hometask.adapter.in.file.dto.LoadResultJson(
                            r.id(), r.customerId(), r.accepted());
                });

        Path outputFile = tempDir.resolve("output.txt");

        FileLoadProcessor processor = new FileLoadProcessor(
                useCase, loadJsonMapper, objectMapper, resourceLoader,
                "classpath:input.txt", outputFile.toString());

        processor.run();

        List<String> lines = Files.readAllLines(outputFile);
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0)).contains("\"id\":\"1\"").contains("\"accepted\":true");
    }
}
