package com.pdd.pddai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponsePrompt;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentService {

    @Value("${yandex.api.key}")
    private String apiKey;

    @Value("${yandex.folder.id}")
    private String folderId;

    @Value("${yandex.agent.id}")
    private String agentId;

    private OpenAIClient createClient() {
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl("https://ai.api.cloud.yandex.net/v1")
                .organization(folderId)
                .build();
    }

    public String askAgent(String prompt) {
        try {
            OpenAIClient client = createClient();
            ResponseCreateParams params = ResponseCreateParams.builder()
                    .prompt(ResponsePrompt.builder()
                            .id(agentId)
                            .build())
                    .input(prompt)
                    .build();

            var response = client.responses().create(params);

            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writeValueAsString(response);
            JsonNode root = mapper.readTree(jsonStr);
            String text = root.path("output").path(0).path("content").path(0).path("text").asText();
            return text;

        } catch (Exception e) {
            log.error("Ошибка при вызове агента: {}", e.getMessage(), e);
            return "Не удалось получить ответ от агента. Попробуйте позже.";
        }
    }
}

