package io.github.vishalmysore.tools4ai.domain;

import lombok.Data;
import java.util.List;

@Data
public class RunEventsListResponse {
    private List<Event> events;
}

@Data
class Event {
    private String type;
    private Object data;  // Can be Message, Run, Error, or Generic event data
}
