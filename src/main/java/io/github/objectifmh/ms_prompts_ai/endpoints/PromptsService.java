package io.github.objectifmh.ms_prompts_ai.endpoints;

import io.github.objectifmh.ms_prompts_ai.openai.GeminiService;
import org.springframework.stereotype.Service;

@Service
public class PromptsService {

    private final GeminiService geminiService;

    public PromptsService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String define(DefinePrompt definePrompt){
        return this.geminiService.define(definePrompt);
    }
}
