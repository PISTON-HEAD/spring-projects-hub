package com.ragapp.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragapp.config.OnnxEmbeddingModel;
import com.ragapp.dto.LoginRequest;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 *
 * Uses @SpringBootTest to start a real application context (but without the
 * OpenAI network calls — we override the api-key to "test" so the app starts).
 * These tests prove the full Security→Auth→JWT chain works end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test-key-does-not-call-openai",
        "app.rag.seed.enabled=false",
        "app.rag.persistence.enabled=false"
})
class AuthControllerIntegrationTest {

    // Mock AI beans so the context loads without any network calls
    @MockBean EmbeddingModel embeddingModel;
    @MockBean ChatModel chatModel;
    // Prevents the real ONNX model download during context startup
    @MockBean OnnxEmbeddingModel onnxEmbeddingModel;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Successful login ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login with valid admin credentials returns JWT token")
    void login_withValidAdminCredentials_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest("admin", "admin123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("token");
    }

    @Test
    @DisplayName("POST /auth/login with valid organization credentials returns JWT token + org info")
    void login_withValidOrgCredentials_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest("acme", "acme123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("acme"))
                .andExpect(jsonPath("$.mode").value("ORGANIZATION"))
                .andExpect(jsonPath("$.orgId").value("acme"))
                .andExpect(jsonPath("$.orgName").value("Acme Corporation"));
    }

    // ── Invalid credentials ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login with wrong password returns 401")
    void login_withWrongPassword_returns401() throws Exception {
        LoginRequest req = new LoginRequest("admin", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login with unknown username returns 401")
    void login_withUnknownUsername_returns401() throws Exception {
        LoginRequest req = new LoginRequest("ghost", "password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login with blank username returns 400")
    void login_withBlankUsername_returns400() throws Exception {
        LoginRequest req = new LoginRequest("", "password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login with blank password returns 400")
    void login_withBlankPassword_returns400() throws Exception {
        LoginRequest req = new LoginRequest("admin", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── Protected endpoint without token ─────────────────────────────────────

    @Test
    @DisplayName("POST /api/documents/{id}/query without Authorization header returns 401/403")
    void protectedEndpoint_withoutToken_returns401or403() throws Exception {
        mockMvc.perform(post("/api/documents/some-id/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is this?\"}"))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus())
                                .isIn(401, 403));
    }
}
