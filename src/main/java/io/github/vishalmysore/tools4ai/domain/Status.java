package io.github.vishalmysore.tools4ai.domain;

import lombok.Data;

@Data
public class Status {
    private Double avgRunTokens;
    private Double avgRunTimeSeconds;
    private Double successRate;
}
