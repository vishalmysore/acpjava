package io.github.vishalmysore.domain;

import lombok.Data;
import java.util.List;

@Data
public class AgentManifest {
    private String name;
    private String description;
    private List<String> inputContentTypes;
    private List<String> outputContentTypes;
    private Metadata metadata;
    private Status status;
}
