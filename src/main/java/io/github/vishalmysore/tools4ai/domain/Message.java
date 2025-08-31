package io.github.vishalmysore.tools4ai.domain;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class Message {
    private MessageRole role;
    private List<MessagePart> parts;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;

    public void addPart(MessagePart part) {
        if(this.parts == null) {
            this.parts = new java.util.ArrayList<>();
        }
        this.parts.add(part);
    }
}
