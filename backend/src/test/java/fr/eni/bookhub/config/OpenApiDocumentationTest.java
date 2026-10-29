package fr.eni.bookhub.config;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void swaggerUiIsPublic() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
  }

  @Test
  void allEndpointsAreDocumentedWithTheirStatusCodes() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("BookHub API"))
        .andExpect(jsonPath("$.paths['/api/auth/register'].post.responses", hasKey("201")))
        .andExpect(jsonPath("$.paths['/api/auth/register'].post.responses", hasKey("409")))
        .andExpect(jsonPath("$.paths['/api/auth/login'].post.responses", hasKey("401")))
        .andExpect(jsonPath("$.paths['/api/users/me'].get.responses", hasKey("401")))
        .andExpect(jsonPath("$.paths['/api/users/me'].put.responses", hasKey("400")))
        .andExpect(jsonPath("$.paths['/api/users/me/password'].put.responses", hasKey("204")))
        .andExpect(jsonPath("$.paths['/api/users/me'].delete.responses", hasKey("409")))
        .andExpect(jsonPath("$.paths['/api/books'].get.responses", hasKey("200")))
        .andExpect(jsonPath("$.paths['/api/books'].get.responses", hasKey("400")));
  }

  @Test
  void catalogueShowsPaginationParametersAndIsPublic() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(jsonPath("$.paths['/api/books'].get.description").isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/books'].get.parameters[*].name").value(
            containsInAnyOrder("page", "size", "sort")))
        .andExpect(jsonPath("$.paths['/api/books'].get.security").doesNotExist())
        .andExpect(jsonPath("$.components.schemas", hasKey("BookSummaryResponse")));
  }

  @Test
  void searchQueryParametersAreDocumented() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(jsonPath("$.paths['/api/books/search'].get.description").isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/books/search'].get.parameters[*].name").value(
            containsInAnyOrder("q", "categoryId", "available", "sort", "page", "size")))
        .andExpect(jsonPath("$.paths['/api/books/search'].get.parameters[?(@.name == 'q')].description")
            .isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/books/search'].get.responses", hasKey("400")))
        .andExpect(jsonPath("$.paths['/api/categories'].get.description").isNotEmpty())
        .andExpect(jsonPath("$.components.schemas", hasKey("CategoryResponse")));
  }

  @Test
  void everyEndpointExplainsWhatItIsFor() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(jsonPath("$.paths['/api/auth/register'].post.description").isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/auth/login'].post.description").isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/users/me'].get.description").isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/users/me'].put.description").isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/users/me/password'].put.description").isNotEmpty())
        .andExpect(jsonPath("$.paths['/api/users/me'].delete.description").isNotEmpty())
        .andExpect(jsonPath("$.components.schemas.RegisterRequest.description").isNotEmpty())
        .andExpect(jsonPath("$.components.schemas.RegisterRequest.properties.consent.description").isNotEmpty());
  }

  @Test
  void protectedEndpointsRequireJwt() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
        .andExpect(jsonPath("$.paths['/api/users/me'].get.security[0]", hasKey("bearerAuth")))
        .andExpect(jsonPath("$.paths['/api/users/me'].get.parameters").doesNotExist());
  }

  @Test
  void dtoSchemasAreVisible() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(jsonPath("$.components.schemas", hasKey("RegisterRequest")))
        .andExpect(jsonPath("$.components.schemas", hasKey("LoginRequest")))
        .andExpect(jsonPath("$.components.schemas", hasKey("LoginResponse")))
        .andExpect(jsonPath("$.components.schemas", hasKey("UserResponse")))
        .andExpect(jsonPath("$.components.schemas", hasKey("ProfileResponse")))
        .andExpect(jsonPath("$.components.schemas", hasKey("UpdateProfileRequest")))
        .andExpect(jsonPath("$.components.schemas", hasKey("ChangePasswordRequest")))
        .andExpect(jsonPath("$.components.schemas", hasKey("ErrorResponse")))
        .andExpect(jsonPath("$.components.schemas.UserResponse.properties.passwordHash").doesNotExist());
  }

}
