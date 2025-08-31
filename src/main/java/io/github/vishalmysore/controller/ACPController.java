package io.github.vishalmysore.controller;

import com.t4a.processor.AIProcessor;
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

    private AgentManifest convertToAgentManifest(AgentCard card, Map<String, List<Metadata.Capability>> groupedCapabilities) {
        AgentManifest manifest = new AgentManifest();
        manifest.setName(card.getName());
        manifest.setDescription(card.getDescription());
        
        // Set default content types
        manifest.setInputContentTypes(Arrays.asList("text/plain", "application/json"));
        manifest.setOutputContentTypes(Arrays.asList("text/plain", "application/json"));

        // Create metadata
        Metadata metadata = new Metadata();
        metadata.setDocumentation(card.getDescription());
        metadata.setFramework("Tools4AI");
        metadata.setCapabilities(new ArrayList<>());
        
        // Add all capabilities from grouped actions
        for (List<Metadata.Capability> capabilities : groupedCapabilities.values()) {
            metadata.getCapabilities().addAll(capabilities);
        }

        // Add author information if available
        Person author = new Person();
        author.setName(card.getProvider().getOrganization());
        metadata.setAuthor(author);

        // Set the creation timestamp
        metadata.setCreatedAt(OffsetDateTime.now());
        metadata.setUpdatedAt(OffsetDateTime.now());

        manifest.setMetadata(metadata);

        // Create status
        Status status = new Status();
        status.setSuccessRate(100.0); // Default value
        manifest.setStatus(status);

        return manifest;
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
        Run run = new Run();
        try {
            Object obj  = baseAIProcessor.processSingleAction(request.toString());

            MessagePart part = new MessagePart();
            part.setContent(obj.toString());
            Message message = new Message();
            message.setRole("assistant");

            message.addPart(part);

            run.addOutput(message);

        } catch (AIProcessingException e) {
            throw new RuntimeException(e);
        }
        // Implementation
        return ResponseEntity.ok(run);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<Run> getRun(@PathVariable UUID runId) {
        // Implementation
        return ResponseEntity.ok(new Run());
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
