package com.example.demo.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    @Autowired
    private OpenAIClient openAIClient;

    @Value("${azure.openai.chat-deployment}")
    private String chatDeployment;

    private static final String SYSTEM_PROMPT = """
            You are an expert backend debugging assistant.
            Analyze the given log and return ONLY a valid JSON object.

            Format:
            {
              "rootCause": "...",
              "explanation": "...",
              "suggestedFix": "...",
              "confidence": 0.0
            }

            Rules:
            - Return ONLY the JSON — no markdown, no explanation outside it
            - Keep values concise and technical
            - confidence must be a decimal between 0.0 and 1.0
            """;

    public String call(String prompt) {
        log.info("Calling Azure OpenAI ({})...", chatDeployment);
        try {
            ChatCompletionsOptions options = new ChatCompletionsOptions(List.of(
                    new ChatRequestSystemMessage(SYSTEM_PROMPT),
                    new ChatRequestUserMessage(prompt)
            ));
            options.setMaxTokens(500);
            options.setTemperature(0.2);

            ChatCompletions completions = openAIClient.getChatCompletions(chatDeployment, options);
            String content = completions.getChoices().get(0).getMessage().getContent();
            log.info("Azure OpenAI response received.");
            return content;

        } catch (Exception e) {
            log.error("Azure OpenAI call failed: {}", e.getMessage());
            return """
                    {
                      "rootCause": "LLM call failed",
                      "explanation": "%s",
                      "suggestedFix": "Check Azure OpenAI deployment name and API key.",
                      "confidence": 0.0
                    }
                    """.formatted(e.getMessage().replace("\"", "'"));
        }
    }
}