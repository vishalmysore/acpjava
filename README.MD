# ACP Java Implementation

ACP Java is a pure Java implementation of the Agent Communication Protocol (ACP). ACP is an open protocol for communication between AI agents, applications, and humans.

## Overview

Modern AI agents are often built in isolation, across different frameworks, teams, and infrastructures. This fragmentation slows innovation and makes it harder for agents to work together effectively. ACP solves this by enabling agents to communicate and coordinate using multimodal messages.

## Key Features

ACP enables agents to:
- Send and receive rich messages — like text, code, files, or media
- Respond in real time, in the background, or as a stream
- Let others discover what they can do
- Collaborate on long-running tasks
- Share state with each other when needed

ACP powers agent communication on the BeeAI Platform — a place where you can discover, run, and share agents.

## Quick Start

With just simple annotations, you can convert your Java classes and methods into agents and actions:

```java
@RestController
@RequestMapping("/")
public class ACPController extends RealTimeAgentCardController {
    
    public ACPController(ApplicationContext context) {
        super(context);
    }
}
```

## API Endpoints

### Agent Management
- `GET /ping` - Health check endpoint
- `GET /agents` - List available agents with pagination
- `GET /agents/{name}` - Get specific agent manifest

### Run Management
- `POST /runs` - Create and start a new run
- `GET /runs/{run_id}` - Get run status
- `POST /runs/{run_id}` - Resume a paused run
- `POST /runs/{run_id}/cancel` - Cancel a run
- `GET /runs/{run_id}/events` - List run events

### Session Management
- `GET /session/{session_id}` - Get session details

## Dependencies

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

## Agent Manifest

The agent manifest includes:
- Basic information (name, description)
- Input/Output content types
- Capabilities and actions
- Metadata (documentation, framework, author)
- Status metrics

## Integration with Tools4AI

This implementation integrates with the Tools4AI framework to:
- Auto-discover agent capabilities
- Transform Java methods into actions
- Generate agent manifests
- Handle real-time communication

## Reference

- [ACP Specification](https://agentcommunicationprotocol.dev)
- [Tools4AI Documentation](https://github.com/vishalmysore/Tools4AI)

## License

MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Vishal Mysore (visrow@gmail.com)