package io.github.vishalmysore.domain;

import lombok.Data;
import java.util.UUID;
import java.util.List;

@Data
public class Session {
    private UUID id;
    private List<String> history;
    private String state;
}
