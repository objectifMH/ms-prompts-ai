package io.github.objectifmh.ms_prompts_ai.endpoints;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.objectifmh.ms_prompts_ai.openai.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @BeforeEach
    void setUp() {
        // Reset WireMock pour éviter que les stubs et compteurs d'un test
        // n'interfèrent avec les tests suivants
        WireMock.reset();
    }

    @DisplayName("Retourne un message Hello avec la date du jour")
    @Test
    void hello() {
        webTestClient.get()
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
                    String dateDuJour = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    assertTrue(body.contains(dateDuJour), "La réponse doit contenir la date du jour : " + dateDuJour);
                });
    }

    @Test
    @DisplayName("Retourne une réponse quand une query valide est fournie")
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
        webTestClient.post()
                .uri("/prompts/define")

                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt("String"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String response = result.getResponseBody();
                    //System.out.println("RÉPONSE : " + response); chek
                    assertThat(response).isEqualTo("Une String est une chaîne de caractères en programmation.");
                });

        // Then - Vérifie explicitement l'appel une fois :
        verify(exactly(1), postRequestedFor(urlPathEqualTo("/v1/chat/completions"))
                .withHeader("Authorization", matching("Bearer .*")));
    }

    @Test
    @DisplayName("Retourne un message quand la query est vide")
    void define_shouldReturnMessage_whenQueryIsEmpty() {
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
                                            "content": "Veuillez fournir un terme à définir."
                                        }
                                    }]
                                }
                                """)));

        // When & Then
        webTestClient.post()
                .uri("/prompts/define")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt(""))  // ← Query VIDE
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Veuillez fournir un terme à définir.");

        // Then
        verify(exactly(1), postRequestedFor(urlPathEqualTo("/v1/chat/completions")));
    }

    @Test
    @DisplayName("Échoue quand l'API retourne une erreur de quota dépassé")
    void define_shouldFail_whenApiQuotaExceeded() {
        // Given
        stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(429)  // ← Too Many Requests
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "error": {
                                        "message": "You exceeded your current quota",
                                        "type": "insufficient_quota",
                                        "code": "insufficient_quota"
                                    }
                                }
                                """)));

        // When & Then
        webTestClient.post()
                .uri("/prompts/define")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt("test"))
                .exchange()
                .expectStatus().is5xxServerError();  // Spring AI propage l'erreur

        // more than car au moins 1 appel (retry possible en cas d'erreur)
        verify(moreThanOrExactly(1), postRequestedFor(urlPathEqualTo("/v1/chat/completions"))
                .withRequestBody(containing("test")));

    }

    @Test
    @DisplayName("Retourne une liste d'étapes quand une query valide est fournie")
    void roadmap_shouldReturnStepsList_whenQueryIsProvided() {

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
                                                                "content": "[{\\"etape\\":1,\\"libelle\\":\\"Définition des objectifs\\",\\"description\\":\\"Définir les objectifs et les compétences requises pour la formation\\",\\"estimatedDuration\\":\\"1 semaine\\",\\"difficulty\\":\\"Facile\\",\\"competences\\":[\\"Analyse de besoin\\",\\"Conception de parcours\\"]},{\\"etape\\":2,\\"libelle\\":\\"Création du contenu\\",\\"description\\":\\"Créer le contenu de formation et gérer les ressources nécessaires\\",\\"estimatedDuration\\":\\"3 semaines\\",\\"difficulty\\":\\"Moyen\\",\\"competences\\":[\\"Création de contenu\\",\\"Gestion de projet\\"]},{\\"etape\\":3,\\"libelle\\":\\"Évaluation et amélioration\\",\\"description\\":\\"Évaluer les résultats de la formation et améliorer le parcours\\",\\"estimatedDuration\\":\\"2 semaines\\",\\"difficulty\\":\\"Difficile\\",\\"competences\\":[\\"Évaluation des acquis\\",\\"Amélioration continue\\"]}]"
                                                            }
                                                        }]
                                                    }
                                """)));

        // When & Then
        webTestClient.post()
                .uri("/prompts/roadmap")

                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt("String"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Steps.class)
                .consumeWith(result -> {
                    List<Steps> steps = result.getResponseBody();
                    // Vérifie structure globale
                    assertThat(steps).isNotNull();
                    assertThat(steps.size()).isEqualTo(3);

                    // Vérifie que les étapes sont ordonnées
                    assertThat(steps.get(0).etape()).isEqualTo(1);
                    assertThat(steps.get(1).etape()).isEqualTo(2);
                    assertThat(steps.get(2).etape()).isEqualTo(3);


                    // Vérifie que chaque étape a tous ses champs remplis
                    for (Steps step : steps) {
                        assertThat(step.etape()).isPositive();
                        assertThat(step.libelle()).isNotBlank();
                        assertThat(step.description()).isNotBlank();
                        assertThat(step.estimatedDuration()).isNotBlank();
                        assertThat(step.difficulty()).isNotBlank();
                        assertThat(step.competences()).isNotNull();
                        assertThat(step.competences().size()).isGreaterThan(0);
                    }
                });

        // Then - Vérifie explicitement l'appel une fois :
        verify(exactly(1), postRequestedFor(urlPathEqualTo("/v1/chat/completions"))
                .withHeader("Authorization", matching("Bearer .*")));
    }

    @Test
    @DisplayName("Doit retourner une erreur 500 quand l'IA renvoie un JSON mal formé")
    void roadmap_shouldReturnError_whenAiReturnsInvalidJson() {
        stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"choices\":[{\"message\":{\"content\":\"[{'etape':1, 'libelle':...\"}}]}"))); // JSON tronqué

        webTestClient.post()
                .uri("/prompts/roadmap")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt("Java"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Doit gérer l'erreur de quota d'API")
    void roadmap_shouldHandleQuotaExceeded() {
        stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(429) // Too Many Requests
                        .withBody("{\"error\": {\"message\": \"Rate limit reached\"}}")));

        webTestClient.post()
                .uri("/prompts/roadmap")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DefinePrompt("Spring Boot"))
                .exchange()
                .expectStatus().is5xxServerError(); // Ou 429 si le controller le gère
    }
}