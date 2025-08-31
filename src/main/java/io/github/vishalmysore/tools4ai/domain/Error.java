package io.github.vishalmysore.tools4ai.domain;

import lombok.Data;

@Data
public class Error {
    private String code;
    private String message;
    private Object data;
}
