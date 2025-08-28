package io.github.vishalmysore.domain;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class Run {
    private String agentName;
    private UUID sessionId;
    private UUID runId;
    private String status;  // RunStatus enum values
    private Object awaitRequest;  // AwaitRequest type
    private List<Message> output;
    private Error error;
    private OffsetDateTime createdAt;
    private OffsetDateTime finishedAt;
}
