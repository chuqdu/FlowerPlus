package base.api.favorites;

import base.api.controller.ProductFavoriteController;
import base.api.dto.request.paging.PageableRequestDTO;
import base.api.dto.response.ProductResponse;
import base.api.service.IProductFavoriteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductFavoriteController.class)
class ProductFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IProductFavoriteService favoriteService;

    private ProductResponse sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new ProductResponse();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setDescription("Test Description");
        sampleProduct.setPrice(100.0);
        sampleProduct.setStock(10);
        sampleProduct.setIsActive(true);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void toggleFavorite_WithValidProduct_ShouldReturnSuccess() throws Exception {
        // Given
        Long productId = 1L;
        when(favoriteService.toggleFavorite(anyLong(), eq(productId))).thenReturn(true);

        String requestBody = String.format("{\"productId\": %d}", productId);

        // When & Then
        mockMvc.perform(post("/api/favorites/toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.isFavorited").value(true))
                .andExpect(jsonPath("$.message").value("Đã thêm vào yêu thích"));

        verify(favoriteService).toggleFavorite(anyLong(), eq(productId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void toggleFavorite_WithInvalidProduct_ShouldReturnBadRequest() throws Exception {
        // Given
        Long productId = 999L;
        when(favoriteService.toggleFavorite(anyLong(), eq(productId)))
                .thenThrow(new IllegalArgumentException("Product not found"));

        String requestBody = String.format("{\"productId\": %d}", productId);

        // When & Then
        mockMvc.perform(post("/api/favorites/toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sản phẩm không tồn tại hoặc không khả dụng"));

        verify(favoriteService).toggleFavorite(anyLong(), eq(productId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getUserFavorites_ShouldReturnPaginatedResults() throws Exception {
        // Given
        List<ProductResponse> products = Arrays.asList(sampleProduct);
        Page<ProductResponse> page = new PageImpl<>(products);
        
        when(favoriteService.getUserFavorites(anyLong(), any(PageableRequestDTO.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/favorites")
                .param("pageNumber", "0")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(favoriteService).getUserFavorites(anyLong(), any(PageableRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void checkFavoriteStatus_ShouldReturnStatus() throws Exception {
        // Given
        Long productId = 1L;
        when(favoriteService.isFavorited(anyLong(), eq(productId))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/favorites/check/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.isFavorited").value(true));

        verify(favoriteService).isFavorited(anyLong(), eq(productId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void checkMultipleFavoriteStatus_ShouldReturnStatusMap() throws Exception {
        // Given
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        Map<Long, Boolean> statusMap = Map.of(1L, true, 2L, false, 3L, true);
        
        when(favoriteService.getFavoriteStatusMap(anyLong(), eq(productIds)))
                .thenReturn(statusMap);

        String requestBody = objectMapper.writeValueAsString(productIds);

        // When & Then
        mockMvc.perform(post("/api/favorites/check-multiple")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.1").value(true))
                .andExpect(jsonPath("$.data.2").value(false))
                .andExpect(jsonPath("$.data.3").value(true));

        verify(favoriteService).getFavoriteStatusMap(anyLong(), eq(productIds));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void removeFavorite_ShouldReturnSuccess() throws Exception {
        // Given
        Long productId = 1L;
        doNothing().when(favoriteService).removeFavorite(anyLong(), eq(productId));

        // When & Then
        mockMvc.perform(delete("/api/favorites/{productId}", productId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Đã xóa khỏi danh sách yêu thích"));

        verify(favoriteService).removeFavorite(anyLong(), eq(productId));
    }

    @Test
    void getFavoriteCount_ShouldReturnCount() throws Exception {
        // Given
        Long productId = 1L;
        when(favoriteService.getFavoriteCount(eq(productId))).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/favorites/count/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));

        verify(favoriteService).getFavoriteCount(eq(productId));
    }

    @Test
    void toggleFavorite_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Given
        String requestBody = "{\"productId\": 1}";

        // When & Then
        mockMvc.perform(post("/api/favorites/toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(favoriteService, never()).toggleFavorite(anyLong(), anyLong());
    }

    @Test
    void getUserFavorites_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized());

        verify(favoriteService, never()).getUserFavorites(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void toggleFavorite_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        Long productId = 1L;
        when(favoriteService.toggleFavorite(anyLong(), eq(productId)))
                .thenThrow(new RuntimeException("Database error"));

        String requestBody = String.format("{\"productId\": %d}", productId);

        // When & Then
        mockMvc.perform(post("/api/favorites/toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Có lỗi xảy ra khi cập nhật yêu thích"));

        verify(favoriteService).toggleFavorite(anyLong(), eq(productId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void toggleFavorite_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/favorites/toggle")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": json}"))
                .andExpect(status().isBadRequest());

        verify(favoriteService, never()).toggleFavorite(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        // Given
        List<Long> productIds = Arrays.asList(1L, 2L);
        
        // When & Then
        mockMvc.perform(get("/api/favorites/admin/statistics")
                .param("productIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void adminEndpoints_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/favorites/admin/statistics")
                .param("productIds", "1,2"))
                .andExpect(status().isForbidden());
    }
}