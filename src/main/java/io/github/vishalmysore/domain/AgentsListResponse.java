package io.github.vishalmysore.domain;

import lombok.Data;
import java.util.List;

@Data
public class AgentsListResponse {
    private List<AgentManifest> agents;
}
