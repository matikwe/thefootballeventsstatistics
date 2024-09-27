package com.example.thefootballeventsstatistics.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@RequiredArgsConstructor
@Setter
@Getter
public class GetStatistics {
    private List<String> teams;
}
