package io.github.vishalmysore.domain;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class RunCreateRequest {
    private String agentName;
    private String sessionId;
    private Session session;
    private List<Message> input;
    private String mode;  // RunMode enum values
}
