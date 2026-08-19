package ca.venn.hometask.adapter.in.file.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoadResultJson(
        @JsonProperty("id") String id,
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("accepted") boolean accepted) {
}
