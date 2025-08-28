# Quick Start Guide

## Introduction

This guide will help you quickly get started with the Agent Communication Protocol (ACP) Java implementation.

## Prerequisites

- Java 18 or higher
- Maven 3.x
- Spring Boot knowledge
- Basic understanding of RESTful APIs

## Installation

1. Add the required dependencies to your `pom.xml`:

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

2. Create a Spring Boot application class:

```java
@SpringBootApplication
public class ACPApplication {
    public static void main(String[] args) {
        SpringApplication.run(ACPApplication.class, args);
    }
}
```

## Creating Your First Agent

1. Create a controller:

```java
@RestController
@RequestMapping("/")
public class ACPController extends RealTimeAgentCardController {
    
    public ACPController(ApplicationContext context) {
        super(context);
    }
}
```

2. Create an agent:

```java
@Component
public class SimpleAgent {
    
    @AIAction(description = "Greets the user")
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

## Basic Usage

1. Start the application:

```bash
mvn spring-boot:run
```

2. Check if the service is running:

```bash
curl http://localhost:8000/ping
```

3. List available agents:

```bash
curl http://localhost:8000/agents
```

4. Create a run:

```bash
curl -X POST http://localhost:8000/runs \
  -H "Content-Type: application/json" \
  -d '{
    "agent_name": "simple-agent",
    "input": [{
      "role": "user",
      "parts": [{
        "content_type": "text/plain",
        "content": "John"
      }]
    }],
    "mode": "sync"
  }'
```

## Next Steps

1. Review the [API Documentation](API_DOCUMENTATION.md) for detailed endpoint information
2. Check the [Developer Guide](DEVELOPER_GUIDE.md) for advanced topics
3. Explore more complex agent implementations
4. Implement error handling and monitoring
5. Add authentication and authorization

## Support

For issues and questions:
- Create an issue on GitHub
- Contact the maintainer at visrow@gmail.com

## Contributing

1. Fork the repository
2. Create your feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.
