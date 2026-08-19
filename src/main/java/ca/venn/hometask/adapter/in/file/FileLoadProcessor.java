package ca.venn.hometask.adapter.in.file;

import ca.venn.hometask.adapter.in.file.dto.LoadRequestJson;
import ca.venn.hometask.adapter.in.file.dto.LoadResultJson;
import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;
import ca.venn.hometask.domain.port.in.ProcessLoadUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Inbound adapter driving the {@link ProcessLoadUseCase} from a line-delimited
 * JSON file.
 *
 * <p>Reads one {@link LoadRequestJson} per line from the configured input path,
 * evaluates it via the use case, and writes one {@link LoadResultJson} per line
 * to the configured output path (skipping lines for which the use case returns
 * no result, e.g. duplicate load ids).
 */
@Component
@ConditionalOnProperty(value = "hometask.runner.enabled", havingValue = "true", matchIfMissing = true)
public class FileLoadProcessor implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FileLoadProcessor.class);

    private final ProcessLoadUseCase processLoadUseCase;
    private final LoadJsonMapper loadJsonMapper;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String inputPath;
    private final String outputPath;

    public FileLoadProcessor(ProcessLoadUseCase processLoadUseCase,
                              LoadJsonMapper loadJsonMapper,
                              ObjectMapper objectMapper,
                              ResourceLoader resourceLoader,
                              @Value("${hometask.input-path}") String inputPath,
                              @Value("${hometask.output-path}") String outputPath) {
        this.processLoadUseCase = processLoadUseCase;
        this.loadJsonMapper = loadJsonMapper;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    @Override
    public void run(String... args) throws IOException {
        Resource input = resourceLoader.getResource(inputPath);
        log.info("Processing load attempts from {} to {}", inputPath, outputPath);

        int lineNumber = 0;
        int processed = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8));
             Writer writer = Files.newBufferedWriter(Path.of(outputPath), StandardCharsets.UTF_8)) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                try {
                    LoadRequestJson requestJson = objectMapper.readValue(line, LoadRequestJson.class);
                    LoadRequest request = loadJsonMapper.toDomain(requestJson);
                    Optional<LoadResult> result = processLoadUseCase.process(request);

                    if (result.isPresent()) {
                        LoadResultJson resultJson = loadJsonMapper.toJson(result.get());
                        writer.write(objectMapper.writeValueAsString(resultJson));
                        writer.write(System.lineSeparator());
                        processed++;
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    log.error("Failed to process line {}: {}", lineNumber, line, e);
                }
            }
        }

        log.info("Finished processing. Lines read: {}, results written: {}, skipped: {}",
                lineNumber, processed, skipped);
    }
}
