package com.example.thefootballeventsstatistics.controller;

import com.example.thefootballeventsstatistics.dto.EventMessage;
import com.example.thefootballeventsstatistics.dto.GetStatistics;
import com.example.thefootballeventsstatistics.dto.Result;
import com.example.thefootballeventsstatistics.dto.Type;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.thefootballeventsstatistics.strings.UtilityStrings.*;

@RestController
@RequestMapping("/api/v1")
public class StatisticsController {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public StatisticsController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/event")
    public String handleEvent(@RequestBody EventMessage eventMessage) {
        Type type = eventMessage.getType();
        return switch (type) {
            case GET_STATISTICS -> handleGetStatistics(eventMessage.getGetStatistics());
            case RESULT -> handleResult(eventMessage.getResult());
        };
    }

    private String handleGetStatistics(GetStatistics getStatistics) {
        return sendAndReceiveMessage(STATISTICS, getStatistics);
    }

    private String handleResult(Result result) {
        return sendAndReceiveMessage(RESULT, result);
    }

    private String sendAndReceiveMessage(String routingKey, Object messagePayload) {
        String response = (String) rabbitTemplate.convertSendAndReceive(EVENT_EXCHANGE, routingKey, messagePayload, message -> {
            message.getMessageProperties().setReplyTo(REPLY_QUEUE);
            return message;
        });
        return response != null ? response : NO_RESPONSE_RECEIVED;
    }
}