package com.example.thefootballeventsstatistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
        "spring.rabbitmq.template.reply-timeout=5000",
        "spring.rabbitmq.listener.simple.acknowledge-mode=manual"
})
public class TheFootballEventsStatisticsApplicationTests {

    @Container
    public static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.8-management-alpine");

    @LocalServerPort
    private int port;

    private String url;
    private HttpHeaders headers;


    private final TestRestTemplate restTemplate;

    @Autowired
    public TheFootballEventsStatisticsApplicationTests(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @DynamicPropertySource
    static void configureRabbitProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    }

    @BeforeEach
    void setUp() {
        url = "http://localhost:" + port + "/api/v1/event";
        headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
    }

    @Test
    @DirtiesContext
    public void runExampleTest() throws IOException {
        String pathToEventPayloadFile = "src/main/resources/files/event_payloads.txt";
        String pathToExpectedMessages = "src/main/resources/files/expected_messages.txt";
        String pathToOutput = "src/main/resources/files/output_example.txt";
        runEventEndpointTest(pathToEventPayloadFile, pathToExpectedMessages, pathToOutput);
    }

    @Test
    @DirtiesContext
    public void runFinalTest() throws IOException {
        String pathToEventPayloadFile = "src/main/resources/files/messages.txt";
        String pathToExpectedMessages = "src/main/resources/files/result.txt";
        String pathToOutput = "src/main/resources/files/output_final.txt";
        runEventEndpointTest(pathToEventPayloadFile, pathToExpectedMessages, pathToOutput);
    }

    void runEventEndpointTest(String pathToEventPayloadFile, String pathToExpectedMessages, String pathToOutput) throws IOException {
        List<String> eventPayloads = Files.readAllLines(Paths.get(pathToEventPayloadFile));
        List<String> expectedMessages = Files.readAllLines(Paths.get(pathToExpectedMessages));
        assertThat(eventPayloads.size()).isEqualTo(expectedMessages.size());
        Map<String, String> eventToExpectedMessageMap = initMap(eventPayloads, expectedMessages);
        List<String> outputs = new ArrayList<>();
        eventToExpectedMessageMap.forEach((key, value) -> outputs.add(sendRequestAndValidateResponse(key, value)));
        Files.write(Path.of(pathToOutput), outputs);
    }

    private String sendRequestAndValidateResponse(String eventPayload, String expectedMessage) {
        HttpEntity<String> request = new HttpEntity<>(eventPayload, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        String body = removeSpaces(Objects.requireNonNull(response.getBody()));
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(body).contains(removeSpaces(expectedMessage));
        return body;
    }

    private Map<String, String> initMap(List<String> eventPayloads, List<String> expectedMessages) {
        return IntStream.range(0, eventPayloads.size())
                .boxed()
                .collect(Collectors.toMap(eventPayloads::get, expectedMessages::get, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String removeSpaces(String text) {
        return text.replaceAll("\\s", "");
    }
}