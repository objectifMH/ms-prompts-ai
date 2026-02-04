package io.github.objectifmh.ms_prompts_ai.endpoints;

import io.github.objectifmh.ms_prompts_ai.openai.AiService;
import io.github.objectifmh.ms_prompts_ai.openai.Steps;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptsService {

    private final AiService aiService;

    public PromptsService(AiService aiService) {
        this.aiService = aiService;
    }

    public String define(DefinePrompt definePrompt){
        return this.aiService.define(definePrompt);
    }

    public List<Steps> roadmap(DefinePrompt definePrompt) {
        return this.aiService.roadmap(definePrompt);
    }

}
