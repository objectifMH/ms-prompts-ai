package io.github.objectifmh.ms_prompts_ai.endpoints;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
class PromptsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

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
    void define() {

//        stubFor(
//                post(urlEqualTo("/"))
//        )

        this.webTestClient.post()
                .uri("/prompts/define")

                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt("docker"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class);
    }

    @Test
    void roadmap() {
    }
}