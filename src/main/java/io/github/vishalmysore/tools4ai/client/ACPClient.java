package io.github.vishalmysore.tools4ai.client;


import io.github.vishalmysore.tools4ai.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ACPClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public ACPClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Check if the ACP server is available
     * @return true if server responds with OK status
     */
    public boolean ping() {
        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(baseUrl + "/ping", Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * List available agents
     * @param limit maximum number of agents to return
     * @param offset number of agents to skip
     * @return list of agent manifests
     */
    public List<AgentManifest> listAgents(int limit, int offset) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/agents")
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .toUriString();

        AgentsListResponse response = restTemplate.getForObject(url, AgentsListResponse.class);
        return response != null ? response.getAgents() : List.of();
    }

    /**
     * Get details of a specific agent
     * @param name agent name
     * @return agent manifest
     */
    public AgentManifest getAgent(String name) {
        return restTemplate.getForObject(baseUrl + "/agents/" + name, AgentManifest.class);
    }

    /**
     * Create and execute a run synchronously
     * @param agentName name of the agent to run
     * @param input list of input messages
     * @return run result
     */
    public Run executeSync(String agentName, List<Message> input) {
        RunCreateRequest request = new RunCreateRequest();
        request.setAgentName(agentName);
        request.setMode(RunRequestMode.SYNC);
        request.setInput(input);

        return restTemplate.postForObject(baseUrl + "/runs", request, Run.class);
    }

    /**
     * Create and execute a run asynchronously
     * @param agentName name of the agent to run
     * @param input list of input messages
     * @return CompletableFuture that completes when the run is finished
     */
    public CompletableFuture<Run> executeAsync(String agentName, List<Message> input) {
        RunCreateRequest request = new RunCreateRequest();
        request.setAgentName(agentName);
        request.setMode(RunRequestMode.ASYNC);
        request.setInput(input);

        Run initialRun = restTemplate.postForObject(baseUrl + "/runs", request, Run.class);
        
        return CompletableFuture.supplyAsync(() -> {
            Run run;
            do {
                try {
                    Thread.sleep(1000); // Poll every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for run completion", e);
                }
                
                run = getRun(initialRun.getRunId());
            } while (run != null && 
                    (RunStatus.CREATED.toString().equals(run.getStatus()) || 
                     RunStatus.IN_PROGRESS.toString().equals(run.getStatus())));
            
            return run;
        });
    }

    /**
     * Get the current status of a run
     * @param runId ID of the run
     * @return current run status and results
     */
    public Run getRun(UUID runId) {
        return restTemplate.getForObject(baseUrl + "/runs/" + runId, Run.class);
    }

    /**
     * Cancel a running execution
     * @param runId ID of the run to cancel
     * @return updated run status
     */
    public Run cancelRun(UUID runId) {
        return restTemplate.postForObject(baseUrl + "/runs/" + runId + "/cancel", null, Run.class);
    }
}
