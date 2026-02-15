package io.github.objectifmh.ms_prompts_ai.endpoints;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class PromptsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private Environment env;

    @Autowired
    private WireMockServer wiremockServer;

    @Test
    void hello() {
        this.webTestClient.get()
                .uri("/prompts") // On cible le controller
                .exchange()      // On envoie
                .expectStatus().isOk() // On vérifie que c'est 200 OK
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();

                    // On garde ton print pour le plaisir de voir le log
                    System.out.println("Vérification de la réponse : " + body);

                    // 1. Vérification que le body n'est pas vide
                    assertNotNull(body, "La réponse ne doit pas être nulle");

                    // 2. Vérification du texte fixe
                    assertTrue(body.contains("Hello, il est"), "Le message doit contenir le texte de hello");

                    // 3. Vérification de la date dynamique
                    String dateDuJour = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    assertTrue(body.contains(dateDuJour), "La réponse doit contenir la date du jour : " + dateDuJour);
                });
    }


    @Test
    void define_shouldReturnAiResponse_whenQueryIsProvided() {

        // Given
        stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                 {
                                                    "choices": [{
                                                        "message": {
                                                            "role": "assistant",
                                                            "content": "Une String est une chaîne de caractères en programmation."
                                                        }
                                                    }]
                                                }
                                """)));

        // When & Then
        this.webTestClient.post()
                .uri("/prompts/define")

                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt("String"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String response = result.getResponseBody();
                    //System.out.println("RÉPONSE : " + response);
                    assertThat(response).isEqualTo("Une String est une chaîne de caractères en programmation.");
                });

        // Then - Vérifie explicitement l'appel une fois :
        verify(exactly(1), postRequestedFor(urlPathEqualTo("/v1/chat/completions")));
    }


    @Test
    void roadmap() {
    }

    @Test
    void findTheUrl() {
        // Un record se crée uniquement comme ça, sans setters
        DefinePrompt monPrompt = new DefinePrompt("Java");

        System.out.println("🚀 Envoi du record DefinePrompt : " + monPrompt);

        this.webTestClient.post()
                .uri("/prompts/define")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(monPrompt)
                .exchange()
                .expectStatus().is5xxServerError();
        // On force l'erreur pour que WireMock nous affiche l'URL non trouvée
    }
}