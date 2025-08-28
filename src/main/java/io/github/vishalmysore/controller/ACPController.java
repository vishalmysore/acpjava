package io.github.vishalmysore.controller;

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
    
    private PromptTransformer promptTransformer;
    private List<AgentManifest> agentManifests = new ArrayList<>();
    @Value("${server.port:8080}")
    private String serverPort;

    public ACPController(ApplicationContext context) {
        super(context);
    }

    @PostConstruct
    public void init() {
        promptTransformer = PredictionLoader.getInstance().createOrGetPromptTransformer();
        Map<GroupInfo, String> groupActions = PredictionLoader.getInstance().getActionGroupList().getGroupActions();
        Map<String, AIAction> predictions = PredictionLoader.getInstance().getPredictions();
        StringBuilder realTimeDescription = new StringBuilder("This agent provides the following capabilities: ");

        // Group capabilities for AgentManifest
        Map<String, List<Metadata.Capability>> groupedCapabilities = new HashMap<>();

        for (Map.Entry<GroupInfo, String> entry : groupActions.entrySet()) {
            GroupInfo group = entry.getKey();
            String[] actionNames = entry.getValue().split(",");
            StringBuilder methodNames = new StringBuilder();
            List<Metadata.Capability> capabilities = new ArrayList<>();

            for (String actionName : actionNames) {
                AIAction action = predictions.get(actionName.trim());
                if (action instanceof GenericJavaMethodAction methodAction) {
                    Method m = methodAction.getActionMethod();
                    if (isMethodAllowed(m)) {
                        methodNames.append(",");
                        methodNames.append(actionName.trim());
                        
                        Metadata.Capability capability = new Metadata.Capability();
                        capability.setName(actionName.trim());
                        capability.setDescription(methodAction.getDescription());
                        capabilities.add(capability);
                    }
                }
            }
            groupedCapabilities.put(group.getGroupName(), capabilities);
            realTimeDescription.append(group.getGroupName())
                    .append(" (")
                    .append(group.getGroupDescription())
                    .append("), with actions: ")
                    .append(methodNames)
                    .append("; ");
        }

        if (realTimeDescription.length() > 2) {
            realTimeDescription.setLength(realTimeDescription.length() - 2);
        }

        String finalDescription = realTimeDescription.toString();

        try {
            AgentCard card;
            if(groupActions.isEmpty()) {
                log.warning("No actions found for the agent card");
                card = new AgentCard();
                storeCard(card);
            } else {
                card = (AgentCard) promptTransformer.transformIntoPojo(
                        "use this description and also populate skills in detail " + finalDescription,
                        AgentCard.class);
                storeCard(card);
            }
            
            String hostName = InetAddress.getLocalHost().getHostName();
            getCachedAgentCard().setUrl("http://" + hostName + ":" + serverPort);
            poplateCardFromProperties(getCachedAgentCard());

            // Convert AgentCard to AgentManifest
            AgentManifest manifest = convertToAgentManifest(card, groupedCapabilities);
            agentManifests.clear();
            agentManifests.add(manifest);

        } catch (AIProcessingException e) {
            log.severe("The skills are not populate in the agent card as actions are more in number \n you can either try with different processor \n" +
                    " or you can populate skills individually and add to agent card , or it could be that AI key is not initialized "+e.getMessage());
        } catch (UnknownHostException e) {
            log.warning("host not known set the url manually card.setUrl");
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
        // Implementation
        return ResponseEntity.ok(new Run());
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
