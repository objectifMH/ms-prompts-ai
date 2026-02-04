package io.github.objectifmh.ms_prompts_ai.endpoints;

import io.github.objectifmh.ms_prompts_ai.openai.Steps;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(path = "/prompts")
public class PromptsController {

    private final PromptsService promptsService;

    public PromptsController(PromptsService promptsService) {
        this.promptsService = promptsService;
        System.out.println(" Initialisation du constructeur : ");
    }

    @RequestMapping
    public String hello() {
        LocalTime maintenant = LocalTime.now();
        LocalDate aujourdhui = LocalDate.now();
        // Le pattern "dd/MM/yyyy" est le standard français

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("Hello, il est %02dh%02d; %s", maintenant.getHour(), maintenant.getMinute(), aujourdhui.format(formatter));
    }

    @PostMapping(path = "define")
    public String define(@RequestBody DefinePrompt definePrompt){
        System.out.println("Dans define");
        return this.promptsService.define(definePrompt);
    }

    @PostMapping(path = "roadmap")
    public List<Steps> roadmap(@RequestBody DefinePrompt definePrompt) {
        return this.promptsService.roadmap(definePrompt);
    }

}
