package io.github.objectifmh.ms_prompts_ai.openai;

import io.github.objectifmh.ms_prompts_ai.endpoints.DefinePrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiService {

    private final ChatClient chatClient;
    private final Resource defineSystem;
    private final Resource defineQuery;

    private final Resource roadmapSystem;
    private final Resource roadmapQuery;

    private final SimpleLoggerAdvisor simpleLoggerAdvisor;


    public AiService(ChatClient.Builder builder,
                     @Value("classpath:/prompts/define-system.st") Resource defineSystem,
                     @Value("classpath:/prompts/define-query.st") Resource defineQuery,

                     @Value("classpath:/prompts/roadmap-system.st") Resource roadmapSystem,
                     @Value("classpath:/prompts/roadmap-query.st") Resource roadmapQuery,
                     SimpleLoggerAdvisor simpleLoggerAdvisor
    ) {
        this.chatClient = builder.build();
        this.defineSystem = defineSystem;
        this.defineQuery = defineQuery;
        this.roadmapSystem = roadmapSystem;
        this.roadmapQuery = roadmapQuery;
        this.simpleLoggerAdvisor = simpleLoggerAdvisor;
    }

    public String define(DefinePrompt definePrompt) {

        Message systemMessage = this.generateMessage(defineSystem, new HashMap<>());
        Message userMessage = this.generateMessage(defineQuery, Map.of("query", definePrompt.query()));

        return this.chatClient
                .prompt()
                .system(systemMessage.getText())
                .user(userMessage.getText())
                .advisors(simpleLoggerAdvisor)
                .call()
                .content();
    }

    public List<Steps> roadmap(DefinePrompt definePrompt) {
        BeanOutputConverter<List<Steps>> stepsOutputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<Steps>>() {
        });
        String format = stepsOutputConverter.getFormat();

        Message systemMessage = this.generateMessage(roadmapSystem, Map.of("query", definePrompt.query()));
        Message userMessage = this.generateMessage(roadmapQuery, Map.of("format", format));
        return this.chatClient
                .prompt()
                .system(systemMessage.getText())
                .user(userMessage.getText())
                .advisors(simpleLoggerAdvisor)
                .call()
                .entity(new ParameterizedTypeReference<List<Steps>>() {
                });


    }

    private Message generateMessage(Resource resource, Map<String, Object> params) {
        PromptTemplate promptTemplate = PromptTemplate
                .builder()
                .resource(resource)
                .build();

        return promptTemplate.createMessage(params);
    }
}
