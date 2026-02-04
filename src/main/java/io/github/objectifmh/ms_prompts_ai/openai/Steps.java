package io.github.objectifmh.ms_prompts_ai.openai;

import java.util.List;

public record Steps(int etape,
                    String libelle,
                    String description,
                    List<String> competences) {

}
