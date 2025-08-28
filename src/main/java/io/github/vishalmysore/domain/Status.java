package io.github.vishalmysore.domain;

import lombok.Data;

@Data
public class Status {
    private Double avgRunTokens;
    private Double avgRunTimeSeconds;
    private Double successRate;
}
