package io.github.vishalmysore.tools4ai.domain;

import lombok.Data;
import java.util.List;

@Data
public class AgentsListResponse {
    private List<AgentManifest> agents;
}
