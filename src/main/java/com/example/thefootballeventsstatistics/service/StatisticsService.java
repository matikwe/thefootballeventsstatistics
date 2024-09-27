package com.example.thefootballeventsstatistics.service;

import com.example.thefootballeventsstatistics.dto.GetStatistics;
import com.example.thefootballeventsstatistics.entity.MatchResult;
import com.example.thefootballeventsstatistics.repository.MatchResultRepository;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.thefootballeventsstatistics.strings.UtilityStrings.*;

@Service
public class StatisticsService extends AbstractEventService {

    public StatisticsService(RabbitTemplate rabbitTemplate, MatchResultRepository matchResultRepository) {
        super(rabbitTemplate, matchResultRepository);
    }

    @RabbitListener(queues = STATISTICS_QUEUE, ackMode = MANUAL)
    @Transactional
    public String getStatistics(GetStatistics getStatistics, Channel channel, Message message) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        return getStatisticsBuilder(getStatistics);
    }

    private String getStatisticsBuilder(GetStatistics getStatistics) {
        StringBuilder sb = new StringBuilder();
        List<MatchResult> matchResults = matchResultRepository.findAll();
        getStatistics.getTeams().forEach(team -> sb.append(buildStatistic(filterTop3LatestMatchResultsByTeam(matchResults, team), team)));
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private StringBuilder buildStatistic(List<MatchResult> matchResults, String team) {
        return new StringBuilder().append(team).append(SPACE)
                .append(getTeamForm(matchResults, team)).append(SPACE)
                .append(getAverageAmountOfGoals(matchResults, team)).append(SPACE)
                .append(getNumberOfPlayedEvents(matchResults)).append(SPACE)
                .append(getSumOfGainedPoints(matchResults, team)).append(SPACE)
                .append(getSumOfGoalsScored(matchResults, team)).append(SPACE)
                .append(getSumOfGoalsConceded(matchResults, team)).append(NEW_LINE);
    }

    private String getTeamForm(List<MatchResult> matchResults, String team) {
        return matchResults.stream()
                .map(match -> determineMatchOutcome(match, team))
                .collect(Collectors.joining());
    }

    private String getAverageAmountOfGoals(List<MatchResult> matchResults, String team) {
        float average = 0;
        if (!matchResults.isEmpty()) {
            average = (float) (getSumOfGoalsScored(matchResults, team) + getSumOfGoalsConceded(matchResults, team)) / getNumberOfPlayedEvents(matchResults);
        }
        return String.format("%.2f", average);
    }


    private List<MatchResult> filterTop3LatestMatchResultsByTeam(List<MatchResult> matchResults, String team) {
        return matchResults
                .stream()
                .filter(matchResult -> isTeamInvolved(matchResult, team))
                .sorted(Comparator.comparing(MatchResult::getId).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    private String determineMatchOutcome(MatchResult match, String team) {
        if (team.equals(match.getHomeTeam())) {
            if (match.getHomeScore() > match.getAwayScore()) {
                return W;
            } else if (match.getHomeScore() < match.getAwayScore()) {
                return L;
            } else {
                return D;
            }
        } else if (team.equals(match.getAwayTeam())) {
            if (match.getAwayScore() > match.getHomeScore()) {
                return W;
            } else if (match.getAwayScore() < match.getHomeScore()) {
                return L;
            } else {
                return D;
            }
        }
        return EMPTY;
    }
}
