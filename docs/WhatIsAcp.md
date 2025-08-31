# Understanding the Agent Communication Protocol (ACP)

## What is ACP?

ACP (Agent Communication Protocol) is an open protocol designed for agent interoperability. It addresses the growing challenge of connecting:
- AI agents
- Applications
- Humans

## The Problem ACP Solves

Modern AI agents face several challenges:
- Built in isolation
- Developed across different frameworks
- Created by separate teams
- Deployed on various infrastructures

This fragmentation makes it difficult for agents to work together effectively.

## Technical Focus

While comprehensive documentation about ACP basics is available at [ACP Official Documentation](https://agentcommunicationprotocol.dev/introduction/welcome), this guide focuses on:
- Technical implementation details
- Building agents in Java using ACP
- Understanding REST endpoints and data flow

## Comparison with Other Protocols

ACP differs from other protocols in several ways:

| Protocol | Key Differences |
|----------|----------------|
| A2A | Agent-to-Agent specific |
| MCP | Model Context Protocol |
| Agent Connect | Connection-oriented |

## Core Components

### REST Endpoints

ACP provides several key endpoints:
1. Agent Discovery (`/agents`)
2. Run Management (`/runs`)
3. Session Management (`/sessions`)
4. Event Streaming

### Data Flow

Understanding ACP requires familiarity with:
- Input/Output formats
- Message structure
- Event handling
- Session management

## Next Steps

To learn more about implementing ACP in Java:
1. Review the [API Documentation](API_DOCUMENTATION.md)
2. Check the [Code Examples](CODE_EXAMPLES.md)
3. Follow the [Quick Start Guide](QUICK_START.md)

## Technical Implementation

For detailed implementation guidance:
- See [Developer Guide](DEVELOPER_GUIDE.md)
- Review [System Architecture](DIAGRAMS.md)
- Explore [Integration Examples](CODE_EXAMPLES.md#integration-examples)
