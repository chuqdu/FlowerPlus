package base.api.favorites;

import base.api.entity.ProductModel;
import base.api.entity.UserModel;
import base.api.enums.ProductType;
import base.api.enums.UserRole;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * **Feature: product-favorites, Property 7: Xử lý lỗi graceful**
 * **Validates: Requirements 5.3, 5.5**
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductFavoriteControllerPropertyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    private UserModel testUser;

    @BeforeEach
    void setUp() {
        // Create a test user for authentication context
        testUser = new UserModel();
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.USER);
        testUser.setActive(true);
        testUser.setVerified(true);
        testUser = userRepository.save(testUser);
    }

    @Property(tries = 50)
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenToggleFavoriteWithValidProduct_thenShouldReturnSuccessResponse(@ForAll("validProduct") ProductModel product) throws Exception {
        // Given: Save product
        ProductModel savedProduct = productRepository.save(product);
        
        // When: Toggle favorite
        String requestBody = String.format("{\"productId\": %d}", savedProduct.getId());
        
        MvcResult result = mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.data.isFavorited").value(true))
                .andReturn();
        
        // Then: Response should indicate success
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("\"success\":true");
        assertThat(responseContent).contains("\"isFavorited\":true");
    }

    @Property(tries = 30)
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenToggleFavoriteWithInvalidProduct_thenShouldReturnErrorResponse(@ForAll("invalidProductId") Long invalidProductId) throws Exception {
        // When: Toggle favorite with invalid product ID
        String requestBody = String.format("{\"productId\": %d}", invalidProductId);
        
        mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Property(tries = 50)
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenCheckFavoriteStatus_thenShouldReturnConsistentResults(@ForAll("validProduct") ProductModel product) throws Exception {
        // Given: Save product
        ProductModel savedProduct = productRepository.save(product);
        
        // Initially should not be favorited
        mockMvc.perform(get("/api/favorites/check/{productId}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.data.isFavorited").value(false));
        
        // When: Add to favorites
        String toggleRequest = String.format("{\"productId\": %d}", savedProduct.getId());
        mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toggleRequest))
                .andExpect(status().isOk());
        
        // Then: Should be favorited
        mockMvc.perform(get("/api/favorites/check/{productId}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.data.isFavorited").value(true));
    }

    @Property(tries = 30)
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenGetUserFavorites_thenShouldReturnPaginatedResults(@ForAll("validProducts") java.util.List<ProductModel> products) throws Exception {
        Assume.that(products.size() >= 2);
        
        // Given: Save products and add to favorites
        java.util.List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        for (ProductModel product : savedProducts) {
            String toggleRequest = String.format("{\"productId\": %d}", product.getId());
            mockMvc.perform(post("/api/favorites/toggle")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toggleRequest))
                    .andExpect(status().isOk());
        }
        
        // When: Get favorites with pagination
        int pageSize = Math.max(1, savedProducts.size() / 2);
        
        mockMvc.perform(get("/api/favorites")
                .param("pageNumber", "0")
                .param("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(savedProducts.size()))
                .andExpect(jsonPath("$.data.size").value(pageSize));
    }

    @Property(tries = 50)
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenRemoveFavorite_thenShouldUpdateStatusCorrectly(@ForAll("validProduct") ProductModel product) throws Exception {
        // Given: Save product and add to favorites
        ProductModel savedProduct = productRepository.save(product);
        
        String toggleRequest = String.format("{\"productId\": %d}", savedProduct.getId());
        mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toggleRequest))
                .andExpect(status().isOk());
        
        // Verify it's favorited
        mockMvc.perform(get("/api/favorites/check/{productId}", savedProduct.getId()))
                .andExpect(jsonPath("$.data.isFavorited").value(true));
        
        // When: Remove favorite
        mockMvc.perform(delete("/api/favorites/{productId}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        // Then: Should not be favorited
        mockMvc.perform(get("/api/favorites/check/{productId}", savedProduct.getId()))
                .andExpect(jsonPath("$.data.isFavorited").value(false));
    }

    @Property(tries = 30)
    void whenAccessWithoutAuthentication_thenShouldReturnUnauthorized(@ForAll("validProduct") ProductModel product) throws Exception {
        // Given: Save product
        ProductModel savedProduct = productRepository.save(product);
        
        // When: Try to access protected endpoints without authentication
        String toggleRequest = String.format("{\"productId\": %d}", savedProduct.getId());
        
        mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toggleRequest))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/favorites/check/{productId}", savedProduct.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Property(tries = 50)
    void whenGetFavoriteCount_thenShouldReturnAccurateCount(@ForAll("validProduct") ProductModel product) throws Exception {
        // Given: Save product
        ProductModel savedProduct = productRepository.save(product);
        
        // Initially should have zero favorites
        mockMvc.perform(get("/api/favorites/count/{productId}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(0));
    }

    @Property(tries = 30)
    void whenServerError_thenShouldReturnGracefulErrorResponse(@ForAll("validProduct") ProductModel product) throws Exception {
        // This test simulates server errors by using invalid data that might cause exceptions
        
        // When: Send malformed request
        mockMvc.perform(post("/api/favorites/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": \"json\"}"))
                .andExpect(status().is4xxClientError());
        
        // When: Send request with missing content type
        mockMvc.perform(post("/api/favorites/toggle")
                .content("{\"productId\": 1}"))
                .andExpect(status().is4xxClientError());
    }

    @Provide
    Arbitrary<ProductModel> validProduct() {
        return Arbitraries.create(() -> {
            ProductModel product = new ProductModel();
            product.setName("Product_" + System.nanoTime());
            product.setDescription("Test product description");
            product.setPrice(100.0 + Math.random() * 900.0);
            product.setStock(10);
            product.setProductType(ProductType.PRODUCT);
            product.setIsActive(true);
            product.setImages("test-image.jpg");
            return product;
        });
    }

    @Provide
    Arbitrary<java.util.List<ProductModel>> validProducts() {
        return validProduct().list().ofMinSize(2).ofMaxSize(5);
    }

    @Provide
    Arbitrary<Long> invalidProductId() {
        return Arbitraries.longs().between(999999L, 9999999L); // IDs that don't exist
    }
}