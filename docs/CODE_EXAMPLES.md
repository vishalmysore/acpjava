# Code Examples and Use Cases

## Basic Agent Implementation

### 1. Simple Conversational Agent

```java
@Component
public class ConversationalAgent {
    
    @AIAction(description = "Handles general conversation")
    public Message chat(Message userMessage) {
        Message response = new Message();
        response.setRole("agent/chat");
        
        MessagePart part = new MessagePart();
        part.setContentType("text/plain");
        part.setContent("Hello! I'm a conversational agent.");
        
        response.setParts(Collections.singletonList(part));
        return response;
    }
}
```

### 2. File Processing Agent

```java
@Component
public class FileProcessingAgent {
    
    @AIAction(description = "Processes file content")
    public Message processFile(Message input) {
        // Extract file content from input
        String fileContent = input.getParts().stream()
            .filter(p -> p.getContentType().startsWith("text/"))
            .map(MessagePart::getContent)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No text content found"));
            
        // Process the content
        String processed = processContent(fileContent);
        
        // Create response
        Message response = new Message();
        response.setRole("agent/processor");
        
        MessagePart part = new MessagePart();
        part.setContentType("text/plain");
        part.setContent(processed);
        
        response.setParts(Collections.singletonList(part));
        return response;
    }
    
    private String processContent(String content) {
        // Implementation
        return "Processed: " + content;
    }
}
```

## Advanced Use Cases

### 1. Multi-Agent Collaboration

```java
@Component
public class OrchestratorAgent {
    
    @Autowired
    private ConversationalAgent chatAgent;
    
    @Autowired
    private FileProcessingAgent fileAgent;
    
    @AIAction(description = "Orchestrates multiple agents")
    public Message orchestrate(Message input) {
        // First, process any files
        Message fileResult = fileAgent.processFile(input);
        
        // Then, discuss the results
        Message chatInput = new Message();
        chatInput.setRole("user");
        MessagePart part = new MessagePart();
        part.setContentType("text/plain");
        part.setContent("Discuss the processing results: " + 
            fileResult.getParts().get(0).getContent());
        chatInput.setParts(Collections.singletonList(part));
        
        return chatAgent.chat(chatInput);
    }
}
```

### 2. Stateful Agent with Session Management

```java
@Component
public class StatefulAgent {
    
    private final Map<UUID, AgentState> states = new ConcurrentHashMap<>();
    
    @AIAction(description = "Handles stateful conversation")
    public Message interact(Message input, UUID sessionId) {
        // Get or create state
        AgentState state = states.computeIfAbsent(sessionId, 
            id -> new AgentState());
            
        // Update state based on input
        state.addToHistory(input);
        
        // Generate response based on state
        Message response = generateResponse(state);
        
        // Update state with response
        state.addToHistory(response);
        
        return response;
    }
    
    @Data
    private static class AgentState {
        private List<Message> history = new ArrayList<>();
        
        public void addToHistory(Message message) {
            history.add(message);
        }
    }
}
```

### 3. Event-Driven Agent

```java
@Component
public class EventDrivenAgent {
    
    private final ApplicationEventPublisher eventPublisher;
    
    @AIAction(description = "Handles long-running tasks")
    public void processAsync(Message input, UUID runId) {
        // Emit start event
        eventPublisher.publishEvent(new RunCreatedEvent(runId));
        
        // Begin processing
        CompletableFuture.runAsync(() -> {
            try {
                // Emit progress events
                eventPublisher.publishEvent(
                    new RunInProgressEvent(runId));
                
                // Do work
                Thread.sleep(5000);
                
                // Emit completion event
                eventPublisher.publishEvent(
                    new RunCompletedEvent(runId));
                    
            } catch (Exception e) {
                // Emit failure event
                eventPublisher.publishEvent(
                    new RunFailedEvent(runId, e));
            }
        });
    }
}
```

## Integration Examples

### 1. External API Integration

```java
@Component
public class WeatherAgent {
    
    @Value("${weather.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    
    @AIAction(description = "Gets weather information")
    public Message getWeather(Message input) {
        // Extract location from input
        String location = extractLocation(input);
        
        // Call weather API
        WeatherResponse weather = restTemplate.getForObject(
            "https://api.weather.com?location={location}&key={key}",
            WeatherResponse.class,
            location,
            apiKey
        );
        
        // Create response
        return createWeatherMessage(weather);
    }
}
```

### 2. Database Integration

```java
@Component
public class DatabaseAgent {
    
    @Autowired
    private JdbcTemplate jdbc;
    
    @AIAction(description = "Queries database")
    public Message queryDatabase(Message input) {
        // Extract query parameters
        String query = extractQuery(input);
        
        // Execute query
        List<Map<String, Object>> results = 
            jdbc.queryForList(query);
            
        // Format results
        return createResultMessage(results);
    }
}
```

### 3. File System Integration

```java
@Component
public class FileSystemAgent {
    
    @Value("${file.storage.path}")
    private String storagePath;
    
    @AIAction(description = "Manages files")
    public Message handleFile(Message input) {
        for (MessagePart part : input.getParts()) {
            if (part.getContentType().startsWith("application/")) {
                // Save file
                Path filePath = Paths.get(storagePath, 
                    UUID.randomUUID().toString());
                Files.write(filePath, 
                    Base64.getDecoder().decode(part.getContent()));
                    
                // Return file info
                return createFileInfoMessage(filePath);
            }
        }
        throw new IllegalArgumentException("No file content found");
    }
}
```

## Testing Examples

### 1. Unit Testing

```java
@SpringBootTest
public class ConversationalAgentTest {
    
    @Autowired
    private ConversationalAgent agent;
    
    @Test
    public void testChat() {
        Message input = new Message();
        input.setRole("user");
        MessagePart part = new MessagePart();
        part.setContentType("text/plain");
        part.setContent("Hello");
        input.setParts(Collections.singletonList(part));
        
        Message response = agent.chat(input);
        
        assertNotNull(response);
        assertEquals("agent/chat", response.getRole());
        assertFalse(response.getParts().isEmpty());
    }
}
```

### 2. Integration Testing

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ACPControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testCreateRun() {
        RunCreateRequest request = new RunCreateRequest();
        request.setAgentName("chat");
        request.setMode("sync");
        
        Message input = new Message();
        input.setRole("user");
        MessagePart part = new MessagePart();
        part.setContentType("text/plain");
        part.setContent("Test message");
        input.setParts(Collections.singletonList(part));
        
        request.setInput(Collections.singletonList(input));
        
        ResponseEntity<Run> response = restTemplate
            .postForEntity("/runs", request, Run.class);
            
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getRunId());
    }
}
```
