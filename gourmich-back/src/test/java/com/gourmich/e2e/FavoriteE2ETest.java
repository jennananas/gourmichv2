package com.gourmich.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourmich.dto.AuthResponse;
import com.gourmich.dto.LoginRequest;
import com.gourmich.models.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class FavoriteE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String bearerToken;
    private long recipeId;

    @BeforeEach
    void setUp() throws Exception {
        registerUser("e2euser", "password123", "e2euser@example.com");
        bearerToken = loginAndGetToken("e2euser", "password123");

        // Créer une recette pour les favoris
        JsonNode createdRecipe = createRecipe("Pancakes", "Delicious", "DESSERT",
                List.of(Map.of("name", "Flour", "quantity", 100.0, "unit", "g")));
        recipeId = createdRecipe.get("id").asLong();
    }

    @Test
    void addFavorite_success() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asLong()).isPositive();
        assertThat(body.get("recipeId").asLong()).isEqualTo(recipeId);
    }

    @Test
    void removeFavorite_success() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers), String.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Unfav recipe");
    }

    @Test
    void getFavorites_success() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/favorites",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode favorites = objectMapper.readTree(response.getBody());
        assertThat(favorites.isArray()).isTrue();
        assertThat(favorites.get(0).get("recipeId").asLong()).isEqualTo(recipeId);
    }

    @Test
    void isFavorite_success() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/favorites/is-favorite/" + recipeId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("true");
    }

    @Test
    void toggleFavorite_unauthorized_noToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody()).contains("Unauthorized");
    }

    @Test
    void toggleFavorite_nonExistentRecipe_shouldReturnNotFound() throws Exception {
        long nonExistentRecipeId = 9999L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/favorites/toggle?recipeId=" + nonExistentRecipeId,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Erreur interne lors du toggle du favori");
    }

    @Test
    void getFavorites_unauthorized_noToken() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/favorites",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody()).contains("Unauthorized");
    }

    @Test
    void isFavorite_unauthorized_noToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/favorites/is-favorite/" + recipeId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody()).contains("Unauthorized");
    }

    @Test
    void addFavorite_alreadyExists_toggleBehavior() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> firstResponse = restTemplate.postForEntity(
                "/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode firstBody = objectMapper.readTree(firstResponse.getBody());
        assertThat(firstBody.get("id").asLong()).isPositive();
        assertThat(firstBody.get("recipeId").asLong()).isEqualTo(recipeId);

        ResponseEntity<String> secondResponse = restTemplate.postForEntity(
                "/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isEqualTo("Unfav recipe");
    }

    @Test
    void removeFavorite_nonExistentFavorite_createsIt() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> checkBefore = restTemplate.exchange(
                "/api/favorites/is-favorite/" + recipeId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(checkBefore.getBody()).isEqualTo("false");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/favorites/toggle?recipeId=" + recipeId,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("id").asLong()).isPositive();
        assertThat(body.get("recipeId").asLong()).isEqualTo(recipeId);

        ResponseEntity<String> checkAfter = restTemplate.exchange(
                "/api/favorites/is-favorite/" + recipeId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertThat(checkAfter.getBody()).isEqualTo("true");
    }

    // ------------------- Tools -------------------

    private void registerUser(String username, String password, String email) {
        Users newUser = new Users();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", new HttpEntity<>(newUser, headers), String.class
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", new HttpEntity<>(login, headers), AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getToken()).isNotBlank();

        return "Bearer " + response.getBody().getToken();
    }

    private JsonNode createRecipe(String title, String description, String category, List<Map<String, Object>> ingredients) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("description", description);
        payload.put("category", category);
        payload.put("instructions", "Mix, cook, eat.");
        payload.put("ingredients", ingredients);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/recipes", new HttpEntity<>(payload, headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();

        return objectMapper.readTree(response.getBody());
    }
}