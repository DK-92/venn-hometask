package ca.venn.hometask.domain.port.in;

import ca.venn.hometask.domain.model.LoadRequest;
import ca.venn.hometask.domain.model.LoadResult;

import java.util.Optional;

public interface ProcessLoadUseCase {

    Optional<LoadResult> process(LoadRequest request);
}
