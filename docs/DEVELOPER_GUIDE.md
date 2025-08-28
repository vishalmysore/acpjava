# Developer Guide

## Overview

This guide explains how to use and extend the Agent Communication Protocol (ACP) implementation in Java.

## Architecture

### Components

1. **Controller Layer**
   - `ACPController`: Main REST controller handling all ACP endpoints
   - Extends `RealTimeAgentCardController` from the Tools4AI framework
   - Manages agent manifests and run execution

2. **Domain Layer**
   - `AgentManifest`: Describes agent capabilities
   - `Run`: Represents agent execution instance
   - `Session`: Manages execution state
   - `Message`: Communication container
   - `Event`: Run-time events

3. **Service Layer**
   - Agent discovery and management
   - Run execution and control
   - Event handling and streaming
   - Session management

### Flow Diagram

```
User Request → ACPController → Agent Discovery → Run Execution → Event Stream → Response
```

## Implementation Guide

### Creating a New Agent

1. Create a Java class with your agent implementation:

```java
@Component
public class MyAgent {
    
    @AIAction(description = "Performs specific task")
    public String performTask(String input) {
        // Implementation
        return result;
    }
}
```

2. The agent will be automatically discovered and registered during initialization.

### Managing Agent State

1. Use sessions for maintaining state:

```java
@Component
public class SessionManager {
    
    public Session createSession() {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        return session;
    }
    
    public void updateSessionState(UUID sessionId, String state) {
        // Update session state
    }
}
```

### Handling Events

1. Create event listeners:

```java
@Component
public class RunEventListener {
    
    public void onRunEvent(Event event) {
        switch (event.getType()) {
            case "message.created":
                handleMessageCreated(event);
                break;
            case "run.completed":
                handleRunCompleted(event);
                break;
            // Handle other events
        }
    }
}
```

### Error Handling

1. Use custom exceptions:

```java
public class ACPException extends RuntimeException {
    private final String code;
    
    public ACPException(String code, String message) {
        super(message);
        this.code = code;
    }
}
```

2. Implement global error handling:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ACPException.class)
    public ResponseEntity<Error> handleACPException(ACPException ex) {
        Error error = new Error();
        error.setCode(ex.getCode());
        error.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

## Testing

### Unit Tests

1. Test agent implementation:

```java
@SpringBootTest
public class MyAgentTest {
    
    @Autowired
    private MyAgent agent;
    
    @Test
    public void testPerformTask() {
        String result = agent.performTask("test input");
        assertNotNull(result);
    }
}
```

### Integration Tests

1. Test API endpoints:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ACPControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testCreateRun() {
        RunCreateRequest request = new RunCreateRequest();
        // Set up request
        
        ResponseEntity<Run> response = restTemplate.postForEntity("/runs", request, Run.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

## Deployment

### Configuration

1. Create application.properties:

```properties
server.port=8000
spring.application.name=acp-service

# Logging
logging.level.root=INFO
logging.level.io.github.vishalmysore=DEBUG

# Custom properties
acp.agent.scan-packages=io.github.vishalmysore.agents
```

### Running the Application

1. Using Maven:

```bash
mvn spring-boot:run
```

2. Using Java:

```bash
java -jar target/acpjava-0.0.1.jar
```

## Monitoring

### Metrics

1. Add metrics collection:

```java
@Component
public class ACPMetrics {
    
    private final Counter runCounter;
    private final Timer runTimer;
    
    public ACPMetrics(MeterRegistry registry) {
        this.runCounter = registry.counter("acp.runs.total");
        this.runTimer = registry.timer("acp.runs.duration");
    }
}
```

### Logging

1. Configure logging:

```java
@Slf4j
@Component
public class ACPLogger {
    
    public void logRunCreated(Run run) {
        log.info("Run created: {}", run.getRunId());
    }
    
    public void logRunCompleted(Run run) {
        log.info("Run completed: {}", run.getRunId());
    }
}
```

## Security

### Authentication

1. Implement security configuration:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
            .requestMatchers("/ping").permitAll()
            .anyRequest().authenticated();
        return http.build();
    }
}
```

### Authorization

1. Implement role-based access:

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/runs/{runId}/cancel")
public ResponseEntity<Run> cancelRun(@PathVariable UUID runId) {
    // Implementation
}
```

## Extending the Framework

### Custom Actions

1. Create custom action annotations:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomAction {
    String description() default "";
    String[] tags() default {};
}
```

### Custom Event Types

1. Create custom event types:

```java
public class CustomEvent extends Event {
    private final String customData;
    
    public CustomEvent(String type, String customData) {
        super(type);
        this.customData = customData;
    }
}
```

## Troubleshooting

### Common Issues

1. Agent Discovery Issues
   - Check component scanning configuration
   - Verify annotations are properly set
   - Check package structure

2. Run Execution Issues
   - Verify agent implementation
   - Check error logs
   - Validate input parameters

### Debugging

1. Enable debug logging:

```properties
logging.level.io.github.vishalmysore=DEBUG
```

2. Use debug endpoints:

```java
@GetMapping("/debug/agents")
@Profile("dev")
public ResponseEntity<Map<String, Object>> debugAgents() {
    // Return debug information
}
```
