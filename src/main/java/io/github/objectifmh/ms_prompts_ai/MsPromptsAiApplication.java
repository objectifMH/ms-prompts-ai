package io.github.objectifmh.ms_prompts_ai;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;

@SpringBootApplication
public class MsPromptsAiApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(MsPromptsAiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Démarrage de Ms prompts ai :" + new Date());
	}
}
