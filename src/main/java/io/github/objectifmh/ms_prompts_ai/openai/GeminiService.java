package io.github.objectifmh.ms_prompts_ai.openai;

import io.github.objectifmh.ms_prompts_ai.endpoints.DefinePrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GeminiService {

    private final ChatClient chatClient;
    private final Resource defineSystem;
    private final Resource defineQuery;

    public GeminiService(ChatClient.Builder builder,
                         @Value("classpath:/prompts/define-system.st") Resource defineSystem,
                         @Value("classpath:/prompts/define-query.st") Resource defineQuery) {
        this.chatClient = builder.build();
        this.defineSystem = defineSystem;
        this.defineQuery = defineQuery;
    }

    public String define(DefinePrompt definePrompt){

        Message systemMessage = this.generateMessage(defineSystem, new HashMap<>());
        Message userMessage = this.generateMessage(defineQuery, Map.of("query", definePrompt.query()));

        return this.chatClient
                .prompt()
                        .system(systemMessage.getText())
                                .user(userMessage.getText())
                                        .call()
                                                .content();
    }

    private Message generateMessage(Resource resource, Map<String, Object> params) {
        PromptTemplate promptTemplate = PromptTemplate
                .builder()
                .resource(resource)
                .build();

        return promptTemplate.createMessage(params);
    }
}
