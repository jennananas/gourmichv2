package com.gourmich.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourmich.dto.AuthResponse;
import com.gourmich.dto.LoginRequest;
import com.gourmich.models.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
class RecipeE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String bearerToken;

    @BeforeEach
    void setUp() throws Exception {
        registerUser("e2euser", "password123", "e2euser@example.com");
        bearerToken = loginAndGetToken("e2euser", "password123");
    }

    // ------------------- Success -------------------
    @Test
    void createRecipe_success() throws Exception {
        JsonNode createdRecipe = createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));

        assertThat(createdRecipe.get("title").asText()).isEqualTo("Pancakes");
        assertThat(createdRecipe.get("ingredients").isArray()).isTrue();
        assertThat(createdRecipe.get("ingredients").get(0).get("name").asText()).isEqualTo("Flour");
        assertThat(createdRecipe.get("category").asText()).isEqualTo("Dessert");
    }

    @Test
    void getAllRecipes_success() throws Exception {
        createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();

        JsonNode recipes = objectMapper.readTree(response.getBody());
        assertThat(recipes.isArray()).isTrue();

        boolean found = false;
        for (JsonNode r : recipes) {
            if ("Pancakes".equals(r.get("title").asText())) {
                found = true;
                assertThat(r.get("category").asText()).isEqualTo("Dessert");
                assertThat(r.get("ingredients").isArray()).isTrue();
                assertThat(r.get("ingredients").get(0).get("name").asText()).isEqualTo("Flour");
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    void getRecipeById_success() throws Exception {
        JsonNode createdRecipe = createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));
        long recipeId = createdRecipe.get("id").asLong();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();

        JsonNode recipe = objectMapper.readTree(response.getBody());
        assertThat(recipe.get("id").asLong()).isEqualTo(recipeId);
        assertThat(recipe.get("title").asText()).isEqualTo("Pancakes");
        assertThat(recipe.get("category").asText().toLowerCase()).isEqualTo("dessert");
        assertThat(recipe.get("description").asText()).isEqualTo("Fluffy pancakes");
        assertThat(recipe.get("ingredients").isArray()).isTrue();
        assertThat(recipe.get("ingredients").get(0).get("name").asText()).isEqualTo("Flour");
    }

    @Test
    void updateRecipe_success() throws Exception {
        JsonNode createdRecipe = createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));
        long recipeId = createdRecipe.get("id").asLong();

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Blueberry Pancakes");
        updatePayload.put("description", "Fluffy pancakes with blueberries");
        updatePayload.put("category", "dessert");
        updatePayload.put("difficulty", 1);
        updatePayload.put("cookingTime", 15);
        updatePayload.put("instructions", "Mix, add blueberries, cook, eat.");
        updatePayload.put("ingredients", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g"),
                Map.of("name", "Blueberries", "quantity", 50.0, "unit", "g")
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updatePayload, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();

        JsonNode updatedRecipe = objectMapper.readTree(response.getBody());
        assertThat(updatedRecipe.get("id").asLong()).isEqualTo(recipeId);
        assertThat(updatedRecipe.get("title").asText()).isEqualTo("Blueberry Pancakes");
        assertThat(updatedRecipe.get("description").asText()).isEqualTo("Fluffy pancakes with blueberries");
        assertThat(updatedRecipe.get("category").asText().toLowerCase()).isEqualTo("dessert");
        assertThat(updatedRecipe.get("ingredients").size()).isEqualTo(2);
        assertThat(updatedRecipe.get("ingredients").get(1).get("name").asText()).isEqualTo("Blueberries");
    }

    @Test
    void deleteRecipe_success() throws Exception {
        JsonNode createdRecipe = createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));
        long recipeId = createdRecipe.get("id").asLong();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getByIdResponse = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.GET,
                entity,
                String.class
        );
        assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getAllResponse = restTemplate.exchange(
                "/api/recipes",
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode recipes = objectMapper.readTree(getAllResponse.getBody());
        for (JsonNode r : recipes) {
            assertThat(r.get("id").asLong()).isNotEqualTo(recipeId);
        }
    }

    // ------------------- Errors -------------------
    @Test
    void createRecipe_unauthorized_noToken() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "Unauthorized Recipe");
        payload.put("description", "This should fail");
        payload.put("category", "dessert");
        payload.put("instructions", "Do nothing");
        payload.put("ingredients", List.of(Map.of("name", "Sugar", "quantity", 50.0, "unit", "g")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/recipes", new HttpEntity<>(payload, headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateRecipe_unauthorized_noToken() throws Exception {
        JsonNode createdRecipe = createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));
        long recipeId = createdRecipe.get("id").asLong();

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Unauthorized Update");
        updatePayload.put("description", "This should fail");
        updatePayload.put("category", "dessert");
        updatePayload.put("difficulty", 1);
        updatePayload.put("cookingTime", 10);
        updatePayload.put("instructions", "Do nothing");
        updatePayload.put("ingredients", List.of(Map.of("name", "Sugar", "quantity", 20.0, "unit", "g")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.PUT,
                new HttpEntity<>(updatePayload, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void deleteRecipe_unauthorized_noToken() throws Exception {
        JsonNode createdRecipe = createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));
        long recipeId = createdRecipe.get("id").asLong();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    void createRecipe_badRequest_invalidData() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", ""); // titre vide
        payload.put("description", "");
        payload.put("category", "dessert");
        payload.put("instructions", "");
        payload.put("ingredients", List.of());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/recipes", new HttpEntity<>(payload, headers), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateRecipe_badRequest_invalidData() throws Exception {
        JsonNode createdRecipe = createRecipe("Pancakes", "Fluffy pancakes", "DESSERT", List.of(
                Map.of("name", "Flour", "quantity", 100.0, "unit", "g")
        ));
        long recipeId = createdRecipe.get("id").asLong();

        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("title", "");
        invalidPayload.put("description", "Invalid recipe");
        invalidPayload.put("category", "dessert");
        invalidPayload.put("difficulty", 1);
        invalidPayload.put("cookingTime", 10L);
        invalidPayload.put("instructions", "Hack instructions");
        invalidPayload.put("ingredients", List.of(Map.of("name", "Sugar", "quantity", 20.0, "unit", "g")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.PUT,
                new HttpEntity<>(invalidPayload, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateRecipe_forbidden_otherUser() throws Exception {
        JsonNode createdRecipe = createRecipe("User1 Recipe", "Desc", "dessert", List.of(Map.of("name", "Flour", "quantity", 50.0, "unit", "g")));
        long recipeId = createdRecipe.get("id").asLong();

        registerUser("otherUser", "password123", "other@example.com");
        String otherToken = loginAndGetToken("otherUser", "password123");

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Hacked Recipe");
        updatePayload.put("description", "Trying to edit");
        updatePayload.put("category", "dessert");
        updatePayload.put("difficulty", 1);
        updatePayload.put("cookingTime", 10L);
        updatePayload.put("instructions", "Hack instructions");
        updatePayload.put("ingredients", List.of(Map.of("name", "Sugar", "quantity", 20.0, "unit", "g")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", otherToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.PUT,
                new HttpEntity<>(updatePayload, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteRecipe_forbidden_otherUser() throws Exception {
        JsonNode createdRecipe = createRecipe("User1 Recipe", "Desc", "dessert", List.of(Map.of("name", "Flour", "quantity", 50.0, "unit", "g")));
        long recipeId = createdRecipe.get("id").asLong();

        registerUser("otherUser", "password123", "other@example.com");
        String otherToken = loginAndGetToken("otherUser", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", otherToken);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/recipes/by-id/" + recipeId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getRecipeById_notFound() throws Exception {
        long nonExistentId = 9999L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes/by-id/" + nonExistentId,
                HttpMethod.GET,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateRecipe_notFound() throws Exception {
        long nonExistentId = 9999L;

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Updated Title");
        updatePayload.put("description", "Updated Description");
        updatePayload.put("category", "dessert");
        updatePayload.put("difficulty", 1);
        updatePayload.put("cookingTime", 10);
        updatePayload.put("instructions", "Updated instructions");
        updatePayload.put("ingredients", List.of(
                Map.of("name", "Sugar", "quantity", 50.0, "unit", "g")
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/recipes/by-id/" + nonExistentId,
                HttpMethod.PUT,
                new HttpEntity<>(updatePayload, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteRecipe_notFound() throws Exception {
        long nonExistentId = 9999L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/recipes/by-id/" + nonExistentId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
        assertThat(response.getBody().getToken()).isNotBlank();

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