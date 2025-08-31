package io.github.vishalmysore.client;

import io.github.vishalmysore.domain.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ACPClientExample {
    public static void main(String[] args) {
        // Create client instance
        ACPClient client = new ACPClient("http://localhost:8080");

        // Check server availability
        if (!client.ping()) {
            System.out.println("Server is not available");
            return;
        }

        try {
            // List available agents
            System.out.println("Available Agents:");
            List<AgentManifest> agents = client.listAgents(10, 0);
            for (AgentManifest agent : agents) {
                System.out.println("- " + agent.getName() + ": " + agent.getDescription());
                System.out.println("  Capabilities:");
                for (Metadata.Capability capability : agent.getMetadata().getCapabilities()) {
                    System.out.println("    * " + capability.getName() + ": " + capability.getDescription());
                }
            }

            if (!agents.isEmpty()) {
                // Create a test message
                Message message = new Message();
                message.setRole(MessageRole.USER);
                
                MessagePart part = new MessagePart();
                part.setContentType("text/plain");
                part.setContent("Hello, agent!");
                message.addPart(part);

                // Example of synchronous execution
                System.out.println("\nTesting Synchronous Execution:");
                Run syncRun = client.executeSync(agents.get(0).getName(), List.of(message));
                System.out.println("Sync Result: " + syncRun.getStatus());
                if (!syncRun.getOutput().isEmpty()) {
                    System.out.println("Response: " + syncRun.getOutput().get(0).getParts().get(0).getContent());
                }

                // Example of asynchronous execution
                System.out.println("\nTesting Asynchronous Execution:");
                CompletableFuture<Run> futureRun = client.executeAsync(agents.get(0).getName(), List.of(message));
                
                System.out.println("Waiting for async result...");
                Run asyncRun = futureRun.join();
                System.out.println("Async Result: " + asyncRun.getStatus());
                if (!asyncRun.getOutput().isEmpty()) {
                    System.out.println("Response: " + asyncRun.getOutput().get(0).getParts().get(0).getContent());
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
