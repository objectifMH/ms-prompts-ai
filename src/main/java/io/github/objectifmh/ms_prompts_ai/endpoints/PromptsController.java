package io.github.objectifmh.ms_prompts_ai.endpoints;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "prompts")
public class PromptsController {

    private final PromptsService promptsService;

    public PromptsController(PromptsService promptsService) {
        this.promptsService = promptsService;
        System.out.println(" Initialisation du constructeur : ");
    }

    @RequestMapping
    public String hello(){
        return "Hello";
    }

    @PostMapping
    public String define(@RequestBody DefinePrompt definePrompt){
        return this.promptsService.define(definePrompt);
    }
}
