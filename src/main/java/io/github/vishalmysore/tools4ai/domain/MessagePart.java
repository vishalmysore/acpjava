package io.github.vishalmysore.tools4ai.domain;

import lombok.Data;

@Data
public class MessagePart {
    private String name;
    private String contentType;
    private String content;
    private String contentEncoding;
    private String contentUrl;
    private Object metadata;  // Can be CitationMetadata or TrajectoryMetadata
}
