package io.github.vishalmysore.controller;

import com.t4a.processor.AIProcessor;
import com.t4a.processor.LoggingHumanDecision;
import com.t4a.processor.LogginggExplainDecision;
import io.github.vishalmysore.a2a.server.RealTimeAgentCardController;
import io.github.vishalmysore.domain.*;
import com.t4a.api.AIAction;
import com.t4a.api.GenericJavaMethodAction;
import com.t4a.api.GroupInfo;
import com.t4a.predict.PredictionLoader;
import com.t4a.processor.AIProcessingException;
import com.t4a.transform.GeminiV2PromptTransformer;
import com.t4a.transform.PromptTransformer;
import io.github.vishalmysore.a2a.domain.AgentCard;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/")
public class ACPController extends RealTimeAgentCardController {
    private static final Logger log = Logger.getLogger(ACPController.class.getName());
    
    private AIProcessor baseAIProcessor = null;
    private List<AgentManifest> agentManifests = new ArrayList<>();
    private Map<UUID, Run> runQueue = new ConcurrentHashMap<>();
    private Map<UUID, CompletableFuture<Void>> runningTasks = new ConcurrentHashMap<>();
    
    @Value("${server.port:8080}")
    private String serverPort;

    public ACPController(ApplicationContext context) {
        super(context);
    }

    @PostConstruct
    public void init() {
        Map<GroupInfo, String> groupActions = PredictionLoader.getInstance().getActionGroupList().getGroupActions();
        Map<String, AIAction> predictions = PredictionLoader.getInstance().getPredictions();

        agentManifests.clear();
        
        try {
            baseAIProcessor = PredictionLoader.getInstance().createOrGetAIProcessor();
            String hostName = InetAddress.getLocalHost().getHostName();
            String baseUrl = "http://" + hostName + ":" + serverPort;

            // Create one AgentManifest per group
            for (Map.Entry<GroupInfo, String> entry : groupActions.entrySet()) {
                GroupInfo group = entry.getKey();
                String[] actionNames = entry.getValue().split(",");
                List<Metadata.Capability> capabilities = new ArrayList<>();

                // Collect all valid actions for this group
                for (String actionName : actionNames) {
                    AIAction action = predictions.get(actionName.trim());
                    if (action instanceof GenericJavaMethodAction methodAction) {
                        Method m = methodAction.getActionMethod();
                        if (isMethodAllowed(m)) {
                            Metadata.Capability capability = new Metadata.Capability();
                            capability.setName(actionName.trim());
                            capability.setDescription(methodAction.getDescription());
                            capabilities.add(capability);
                        }
                    }
                }

                if (!capabilities.isEmpty()) {
                    AgentManifest manifest = new AgentManifest();
                    
                    // Set basic info
                    manifest.setName(group.getGroupName().toLowerCase().replaceAll("\\s+", "-"));
                    manifest.setDescription(group.getGroupDescription());
                    
                    // Set default content types
                    manifest.setInputContentTypes(Arrays.asList("text/plain", "application/json"));
                    manifest.setOutputContentTypes(Arrays.asList("text/plain", "application/json"));

                    // Create metadata
                    Metadata metadata = new Metadata();
                    metadata.setDocumentation(group.getGroupDescription());
                    metadata.setFramework("Tools4AI");
                    metadata.setCapabilities(capabilities);
                    metadata.setCreatedAt(OffsetDateTime.now());
                    metadata.setUpdatedAt(OffsetDateTime.now());

                    // Add URLs and links
                    List<Link> links = new ArrayList<>();
                    Link apiLink = new Link();
                    apiLink.setType("api");
                    apiLink.setUrl(baseUrl + "/agents/" + manifest.getName());
                    links.add(apiLink);
                    metadata.setLinks(links);

                    manifest.setMetadata(metadata);

                    // Set status
                    Status status = new Status();
                    status.setSuccessRate(100.0);
                    manifest.setStatus(status);

                    agentManifests.add(manifest);
                }
            }

            if (agentManifests.isEmpty()) {
                log.warning("No agent manifests created - no valid actions found in any group");
            } else {
                log.info("Created " + agentManifests.size() + " agent manifests");
            }

        } catch (UnknownHostException e) {
            log.warning("Host not known, using default localhost for URLs: " + e.getMessage());
        }
    }



    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/agents")
    public ResponseEntity<AgentsListResponse> listAgents(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        AgentsListResponse response = new AgentsListResponse();
        int endIndex = Math.min(offset + limit, agentManifests.size());
        if (offset < agentManifests.size()) {
            response.setAgents(agentManifests.subList(offset, endIndex));
        } else {
            response.setAgents(Collections.emptyList());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agents/{name}")
    public ResponseEntity<AgentManifest> getAgent(@PathVariable String name) {
        return agentManifests.stream()
                .filter(agent -> agent.getName().equals(name))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/runs")
    public ResponseEntity<Run> createRun(@RequestBody RunCreateRequest request) {
        AIAction action = PredictionLoader.getInstance().getPredictions().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(request.getAgentName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        Run run = new Run();
        run.setRunId(UUID.randomUUID());
        run.setAgentName(request.getAgentName());
        run.setCreatedAt(OffsetDateTime.now());

        try {
            if (RunRequestMode.SYNC.equals(request.getMode())) {
                // Synchronous processing
                Object obj = baseAIProcessor.processSingleAction(request.toString(),action,new LoggingHumanDecision(), new LogginggExplainDecision());
                MessagePart part = new MessagePart();
                part.setContent(obj.toString());
                Message message = new Message();
                message.setRole(MessageRole.AGENT);
                message.addPart(part);
                run.addOutput(message);
                run.setStatus(RunStatus.COMPLETED);
                run.setFinishedAt(OffsetDateTime.now());
                
            } else if (RunRequestMode.ASYNC.equals(request.getMode())) {
                // Asynchronous processing
                run.setStatus(RunStatus.IN_PROGRESS);
                runQueue.put(run.getRunId(), run);
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // Update status to in-progress
                        run.setStatus(RunStatus.IN_PROGRESS);
                        
                        // Process the request
                        Object obj = baseAIProcessor.processSingleAction(request.toString(),action,new LoggingHumanDecision(), new LogginggExplainDecision());
                        
                        // Create response message
                        MessagePart part = new MessagePart();
                        part.setContent(obj.toString());
                        Message message = new Message();
                        message.setRole(MessageRole.AGENT);
                        message.addPart(part);
                        
                        // Update run with result
                        run.addOutput(message);
                        run.setStatus(RunStatus.COMPLETED);
                        run.setFinishedAt(OffsetDateTime.now());
                        
                    } catch (Exception e) {
                        log.severe("Error processing async run: " + e.getMessage());
                        run.setStatus(RunStatus.FAILED);
                        run.setError(createError("processing_error", e.getMessage()));
                        run.setFinishedAt(OffsetDateTime.now());
                    }
                });
                
                runningTasks.put(run.getRunId(), future);
                return ResponseEntity.accepted().body(run);
            }

        } catch (AIProcessingException e) {
            run.setStatus(RunStatus.FAILED);
            run.setError(createError("processing_error", e.getMessage()));
            run.setFinishedAt(OffsetDateTime.now());
        }
        
        return ResponseEntity.ok(run);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<Run> getRun(@PathVariable UUID runId) {
        Run run = runQueue.get(runId);
        if (run == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(run);
    }
    
    private io.github.vishalmysore.domain.Error createError(String code, String message) {
        io.github.vishalmysore.domain.Error error = new io.github.vishalmysore.domain.Error();
        error.setCode(code);
        error.setMessage(message);
        return error;
    }

    @PostMapping("/runs/{runId}")
    public ResponseEntity<Run> resumeRun(
            @PathVariable UUID runId,
            @RequestBody RunResumeRequest request) {
        // Implementation
        return ResponseEntity.ok(new Run());
    }

    @PostMapping("/runs/{runId}/cancel")
    public ResponseEntity<Run> cancelRun(@PathVariable UUID runId) {
        // Implementation
        return ResponseEntity.status(202).body(new Run());
    }

    @GetMapping("/runs/{runId}/events")
    public ResponseEntity<RunEventsListResponse> listRunEvents(@PathVariable UUID runId) {
        // Implementation
        return ResponseEntity.ok(new RunEventsListResponse());
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Session> getSession(@PathVariable UUID sessionId) {
        // Implementation
        return ResponseEntity.ok(new Session());
    }
}
