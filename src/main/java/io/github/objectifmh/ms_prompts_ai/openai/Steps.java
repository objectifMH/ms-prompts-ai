package io.github.objectifmh.ms_prompts_ai.openai;

import java.util.List;

public record Steps(int etape,
                    String libelle,
                    String description,
                    String estimatedDuration,
                    String difficulty,  // Ex: "Facile", "Moyen", "Difficile"
                    List<String> competences) {

}
