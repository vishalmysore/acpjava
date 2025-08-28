package io.github.vishalmysore.domain;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class Message {
    private String role;
    private List<MessagePart> parts;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
}
