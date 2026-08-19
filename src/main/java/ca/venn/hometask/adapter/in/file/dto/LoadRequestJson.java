package ca.venn.hometask.adapter.in.file.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoadRequestJson(
        @JsonProperty("id") String id,
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("load_amount") String loadAmount,
        @JsonProperty("time") String time) {
}
