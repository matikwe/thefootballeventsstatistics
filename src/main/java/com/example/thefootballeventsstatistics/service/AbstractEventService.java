package com.example.thefootballeventsstatistics.service;

import com.example.thefootballeventsstatistics.entity.MatchResult;
import com.example.thefootballeventsstatistics.repository.MatchResultRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
abstract public class AbstractEventService {

    protected final RabbitTemplate rabbitTemplate;
    protected final MatchResultRepository matchResultRepository;

    @Autowired
    public AbstractEventService(RabbitTemplate rabbitTemplate, MatchResultRepository matchResultRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.matchResultRepository = matchResultRepository;
    }

    protected int getNumberOfPlayedEvents(List<MatchResult> matchResults) {
        return matchResults.size();
    }

    protected String getSumOfGainedPoints(List<MatchResult> matchResults, String team) {
        int drawPoints = calculateDrawPoints(matchResults, team);
        int winPoints = calculateWinPoints(matchResults, team);

        return String.valueOf(drawPoints + winPoints);
    }

    protected int getSumOfGoalsScored(List<MatchResult> matchResults, String team) {
        return calculateGoals(matchResults, team, true);
    }

    protected int getSumOfGoalsConceded(List<MatchResult> matchResults, String team) {
        return calculateGoals(matchResults, team, false);
    }

    private int calculateGoals(List<MatchResult> matchResults, String team, boolean isScored) {
        int homeGoals = matchResults.stream()
                .filter(matchResult -> matchResult.getHomeTeam().equals(team))
                .mapToInt(matchResult -> isScored ? matchResult.getHomeScore() : matchResult.getAwayScore())
                .sum();

        int awayGoals = matchResults.stream()
                .filter(matchResult -> matchResult.getAwayTeam().equals(team))
                .mapToInt(matchResult -> isScored ? matchResult.getAwayScore() : matchResult.getHomeScore())
                .sum();

        return homeGoals + awayGoals;
    }

    private int calculateDrawPoints(List<MatchResult> currentTeamMatches, String team) {
        return currentTeamMatches.stream()
                .filter(matchResult -> isDraw(matchResult) && isTeamInvolved(matchResult, team))
                .mapToInt(matchResult -> 1)
                .sum();
    }

    private int calculateWinPoints(List<MatchResult> currentTeamMatches, String team) {
        return currentTeamMatches.stream()
                .filter(matchResult -> isWin(matchResult, team))
                .mapToInt(matchResult -> 3)
                .sum();
    }

    private boolean isDraw(MatchResult matchResult) {
        return matchResult.getHomeScore() == matchResult.getAwayScore();
    }

    private boolean isWin(MatchResult matchResult, String team) {
        return (matchResult.getHomeTeam().equals(team) && matchResult.getHomeScore() > matchResult.getAwayScore())
                || (matchResult.getAwayTeam().equals(team) && matchResult.getAwayScore() > matchResult.getHomeScore());
    }

    protected boolean isTeamInvolved(MatchResult matchResult, String team) {
        return (matchResult.getAwayTeam().equals(team) || matchResult.getHomeTeam().equals(team));
    }
}