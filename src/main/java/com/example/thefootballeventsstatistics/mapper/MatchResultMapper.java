package com.example.thefootballeventsstatistics.mapper;

import com.example.thefootballeventsstatistics.dto.Result;
import com.example.thefootballeventsstatistics.entity.MatchResult;
import org.springframework.stereotype.Component;

@Component
public class MatchResultMapper {
    public MatchResult mapToMatchResult(Result result) {
        MatchResult matchResult = new MatchResult();
        matchResult.setHomeTeam(result.getHomeTeam());
        matchResult.setAwayTeam(result.getAwayTeam());
        matchResult.setHomeScore(result.getHomeScore());
        matchResult.setAwayScore(result.getAwayScore());

        return matchResult;
    }
}
