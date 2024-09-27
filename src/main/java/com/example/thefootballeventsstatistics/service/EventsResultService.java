package com.example.thefootballeventsstatistics.service;

import com.example.thefootballeventsstatistics.dto.Result;
import com.example.thefootballeventsstatistics.entity.MatchResult;
import com.example.thefootballeventsstatistics.mapper.MatchResultMapper;
import com.example.thefootballeventsstatistics.repository.MatchResultRepository;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.thefootballeventsstatistics.strings.UtilityStrings.*;

@Service
public class EventsResultService extends AbstractEventService {

    private final MatchResultMapper matchResultMapper;

    @Autowired
    public EventsResultService(MatchResultRepository matchResultRepository, MatchResultMapper matchResultMapper, RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate, matchResultRepository);
        this.matchResultMapper = matchResultMapper;
    }

    @RabbitListener(queues = RESULT_QUEUE, ackMode = MANUAL)
    @Transactional
    public String handleResult(Result result, Channel channel, Message message) throws IOException {
        MatchResult matchResult = matchResultMapper.mapToMatchResult(result);
        matchResultRepository.save(matchResult);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        return getResultBuilder(matchResult).toString();
    }

    private StringBuilder getResultBuilder(MatchResult matchResult) {
        List<MatchResult> matchResults = matchResultRepository.findAll();
        return new StringBuilder()
                .append(buildResult(matchResults, matchResult.getHomeTeam())).append(NEW_LINE)
                .append(buildResult(matchResults, matchResult.getAwayTeam()));
    }

    private StringBuilder buildResult(List<MatchResult> matchResults, String team) {
        List<MatchResult> filterMatchResultsByTeam = filterMatchResultsByTeam(matchResults, team);
        return new StringBuilder().append(team).append(SPACE)
                .append(getNumberOfPlayedEvents(filterMatchResultsByTeam)).append(SPACE)
                .append(getSumOfGainedPoints(filterMatchResultsByTeam, team)).append(SPACE)
                .append(getSumOfGoalsScored(filterMatchResultsByTeam, team)).append(SPACE)
                .append(getSumOfGoalsConceded(filterMatchResultsByTeam, team));
    }

    private List<MatchResult> filterMatchResultsByTeam(List<MatchResult> matchResults, String team) {
        return matchResults
                .stream()
                .filter(matchResult -> isTeamInvolved(matchResult, team))
                .collect(Collectors.toList());
    }
}
