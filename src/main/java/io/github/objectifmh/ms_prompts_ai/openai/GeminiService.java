package io.github.objectifmh.ms_prompts_ai.openai;

import io.github.objectifmh.ms_prompts_ai.endpoints.DefinePrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class GeminiService {

    private ChatClient chatClient;

    public GeminiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String define(DefinePrompt definePrompt){
        return this.chatClient.


                prompt(definePrompt.query()).call().content();
    }
}
