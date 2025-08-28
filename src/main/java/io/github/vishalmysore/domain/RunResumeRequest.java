package io.github.vishalmysore.domain;

import lombok.Data;

@Data
public class RunResumeRequest {
    private String runId;
    private Object awaitResume;  // AwaitResume type
    private String mode;  // RunMode enum values
}
