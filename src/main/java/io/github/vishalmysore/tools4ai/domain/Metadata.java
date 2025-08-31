package io.github.vishalmysore.tools4ai.domain;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class Metadata {
    private Object annotations;
    private String documentation;
    private String license;
    private String programmingLanguage;
    private List<String> naturalLanguages;
    private String framework;
    private List<Capability> capabilities;
    private List<String> domains;
    private List<String> tags;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Person author;
    private List<Person> contributors;
    private List<Link> links;
    private List<AgentDependency> dependencies;
    private List<String> recommendedModels;

    @Data
    public static class Capability {
        private String name;
        private String description;
    }
}
