package com.example.thefootballeventsstatistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Setter
@Getter
public class EventMessage {
    private Type type;
    @JsonProperty("get_statistics")
    private GetStatistics getStatistics;
    private Result result;
}
