# Agent Communication Protocol (ACP) Documentation

## Overview

The Agent Communication Protocol (ACP) provides a standardized RESTful API for managing, orchestrating, and executing AI agents. This implementation in Java supports synchronous, asynchronous, and streamed agent interactions, with both stateless and stateful execution modes.

## API Reference

### Base URL

```
http://localhost:8000
```

### Authentication

Currently, the API doesn't require authentication for local development. For production deployments, appropriate authentication mechanisms should be implemented.

### Endpoints

#### Health Check

```http
GET /ping
```

Checks if the service is running.

**Response**
```json
{
    "status": "ok"
}
```

#### Agent Discovery

```http
GET /agents
```

Returns a list of available agents.

**Query Parameters**
- `limit` (optional): Maximum number of agents to return (default: 10, max: 1000)
- `offset` (optional): Number of agents to skip (default: 0)

**Response**
```json
{
    "agents": [
        {
            "name": "string",
            "description": "string",
            "inputContentTypes": ["text/plain"],
            "outputContentTypes": ["text/plain"],
            "metadata": {
                "documentation": "string",
                "framework": "string",
                "capabilities": [
                    {
                        "name": "string",
                        "description": "string"
                    }
                ]
            },
            "status": {
                "avgRunTokens": 0,
                "avgRunTimeSeconds": 0,
                "successRate": 100
            }
        }
    ]
}
```

#### Get Agent Manifest

```http
GET /agents/{name}
```

Returns detailed information about a specific agent.

**Path Parameters**
- `name`: The name of the agent (required)

**Response**
```json
{
    "name": "string",
    "description": "string",
    "inputContentTypes": ["text/plain"],
    "outputContentTypes": ["text/plain"],
    "metadata": {
        "documentation": "string",
        "framework": "string",
        "capabilities": [
            {
                "name": "string",
                "description": "string"
            }
        ]
    }
}
```

#### Create Run

```http
POST /runs
```

Creates and starts a new agent run.

**Request Body**
```json
{
    "agent_name": "string",
    "session_id": "uuid",
    "input": [
        {
            "role": "user",
            "parts": [
                {
                    "content_type": "text/plain",
                    "content": "string"
                }
            ]
        }
    ],
    "mode": "sync"
}
```

**Response**
```json
{
    "agent_name": "string",
    "run_id": "uuid",
    "status": "in-progress",
    "output": [],
    "created_at": "timestamp"
}
```

#### Get Run Status

```http
GET /runs/{run_id}
```

Returns the current status of a run.

**Path Parameters**
- `run_id`: UUID of the run (required)

**Response**
```json
{
    "agent_name": "string",
    "run_id": "uuid",
    "status": "completed",
    "output": [
        {
            "role": "agent",
            "parts": [
                {
                    "content_type": "text/plain",
                    "content": "string"
                }
            ]
        }
    ],
    "created_at": "timestamp",
    "finished_at": "timestamp"
}
```

#### Resume Run

```http
POST /runs/{run_id}
```

Resumes a paused or awaiting run.

**Path Parameters**
- `run_id`: UUID of the run (required)

**Request Body**
```json
{
    "run_id": "uuid",
    "await_resume": {},
    "mode": "sync"
}
```

#### Cancel Run

```http
POST /runs/{run_id}/cancel
```

Cancels an in-progress run.

**Path Parameters**
- `run_id`: UUID of the run (required)

**Response**
```json
{
    "agent_name": "string",
    "run_id": "uuid",
    "status": "cancelled",
    "created_at": "timestamp",
    "finished_at": "timestamp"
}
```

#### List Run Events

```http
GET /runs/{run_id}/events
```

Returns a list of events emitted by the run.

**Path Parameters**
- `run_id`: UUID of the run (required)

**Response**
```json
{
    "events": [
        {
            "type": "message.created",
            "message": {
                "role": "agent",
                "parts": [
                    {
                        "content_type": "text/plain",
                        "content": "string"
                    }
                ]
            }
        }
    ]
}
```

#### Get Session

```http
GET /session/{session_id}
```

Returns details of a specific session.

**Path Parameters**
- `session_id`: UUID of the session (required)

**Response**
```json
{
    "id": "uuid",
    "history": ["string"],
    "state": "string"
}
```

## Implementation Details

### Java Classes

The implementation uses the following main classes:

1. `ACPController`: Main REST controller implementing all endpoints
2. `AgentManifest`: Represents agent capabilities and metadata
3. `Run`: Represents a single execution of an agent
4. `Session`: Manages state across multiple runs
5. `Message`: Represents communication between users and agents
6. `Event`: Represents various events during agent execution

### Integration with Tools4AI

The implementation leverages Tools4AI framework for:
1. Automatic discovery of agent capabilities
2. Method-to-action transformation
3. Real-time event handling
4. State management

### Error Handling

All endpoints may return the following error responses:

```json
{
    "code": "string",
    "message": "string",
    "data": {}
}
```

Error codes:
- `server_error`: Internal server error
- `invalid_input`: Invalid request parameters
- `not_found`: Requested resource not found

## Configuration

### Maven Dependencies

```xml
<dependency>
    <groupId>io.github.vishalmysore</groupId>
    <artifactId>tools4ai</artifactId>
    <version>1.1.5</version>
</dependency>

<dependency>
    <groupId>io.github.vishalmysore</groupId>
    <artifactId>a2ajava</artifactId>
    <version>0.1.9.6</version>
</dependency>
```

### Spring Configuration

Configure the following properties in `application.properties`:

```properties
server.port=8000
# Add other configuration properties as needed
```

## Best Practices

1. **Error Handling**
   - Always include appropriate error responses
   - Use specific error codes
   - Provide helpful error messages

2. **State Management**
   - Use sessions for maintaining state
   - Clean up expired sessions
   - Handle session conflicts

3. **Performance**
   - Use pagination for list endpoints
   - Implement caching where appropriate
   - Monitor resource usage

4. **Security**
   - Implement authentication for production
   - Validate input parameters
   - Sanitize responses

## Examples

### Creating an Agent Run

```java
RunCreateRequest request = new RunCreateRequest();
request.setAgentName("example-agent");
request.setMode("sync");

Message message = new Message();
message.setRole("user");
MessagePart part = new MessagePart();
part.setContentType("text/plain");
part.setContent("Hello, agent!");
message.setParts(Collections.singletonList(part));

request.setInput(Collections.singletonList(message));

ResponseEntity<Run> response = acpController.createRun(request);
```

### Handling Events

```java
@GetMapping("/runs/{runId}/events")
public ResponseEntity<RunEventsListResponse> listRunEvents(@PathVariable UUID runId) {
    List<Event> events = eventService.getEventsForRun(runId);
    RunEventsListResponse response = new RunEventsListResponse();
    response.setEvents(events);
    return ResponseEntity.ok(response);
}
```
