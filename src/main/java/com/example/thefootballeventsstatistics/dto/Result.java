package com.example.thefootballeventsstatistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Setter
@Getter
public class Result {

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    @JsonProperty("home_score")
    private int homeScore;

    @JsonProperty("away_score")
    private int awayScore;
}
