package base.api.favorites;

import base.api.entity.ProductModel;
import base.api.entity.UserModel;
import base.api.enums.ProductType;
import base.api.enums.UserRole;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import base.api.service.IProductFavoriteService;
import base.api.service.impl.ProductFavoriteServiceImpl;
import net.jqwik.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: product-favorites, Property 8: Ghi log và thống kê**
 * **Validates: Requirements 6.1, 6.2**
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductFavoriteStatisticsPropertyTest {

    @Autowired
    private IProductFavoriteService favoriteService;

    @Autowired
    private ProductFavoriteServiceImpl favoriteServiceImpl; // For accessing helper methods

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Property(tries = 100)
    void whenFavoritesAreCreated_thenStatisticsShouldReflectActualCounts(@ForAll("validUsers") List<UserModel> users,
                                                                         @ForAll("validProducts") List<ProductModel> products) {
        Assume.that(users.size() >= 2 && products.size() >= 2);
        
        // Given: Save users and products
        List<UserModel> savedUsers = userRepository.saveAll(users);
        List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        // Create favorites with known pattern
        Map<Long, Integer> expectedCounts = new java.util.HashMap<>();
        
        for (int i = 0; i < savedProducts.size(); i++) {
            ProductModel product = savedProducts.get(i);
            int favoriteCount = 0;
            
            // Each product gets favorited by (i+1) users
            for (int j = 0; j <= i && j < savedUsers.size(); j++) {
                UserModel user = savedUsers.get(j);
                favoriteService.toggleFavorite(user.getId(), product.getId());
                favoriteCount++;
            }
            
            expectedCounts.put(product.getId(), favoriteCount);
        }
        
        // When: Get statistics
        List<Long> productIds = savedProducts.stream()
            .map(ProductModel::getId)
            .collect(Collectors.toList());
        
        Map<Long, Long> statistics = favoriteServiceImpl.getFavoriteStatistics(productIds);
        
        // Then: Statistics should match expected counts
        assertThat(statistics).hasSize(savedProducts.size());
        
        for (ProductModel product : savedProducts) {
            Long productId = product.getId();
            Long actualCount = statistics.get(productId);
            Integer expectedCount = expectedCounts.get(productId);
            
            assertThat(actualCount).isEqualTo(expectedCount.longValue());
        }
    }

    @Property(tries = 100)
    void whenProductHasNoFavorites_thenStatisticsShouldShowZero(@ForAll("validProduct") ProductModel product) {
        // Given: Save product with no favorites
        ProductModel savedProduct = productRepository.save(product);
        
        // When: Get statistics
        List<Long> productIds = List.of(savedProduct.getId());
        Map<Long, Long> statistics = favoriteServiceImpl.getFavoriteStatistics(productIds);
        
        // Then: Should show zero count
        assertThat(statistics).containsEntry(savedProduct.getId(), 0L);
        
        // Also verify direct count method
        long directCount = favoriteService.getFavoriteCount(savedProduct.getId());
        assertThat(directCount).isZero();
    }

    @Property(tries = 50)
    void whenFavoritesAreAddedAndRemoved_thenStatisticsShouldUpdateAccurately(@ForAll("validUsers") List<UserModel> users,
                                                                              @ForAll("validProduct") ProductModel product) {
        Assume.that(users.size() >= 3);
        
        // Given: Save users and product
        List<UserModel> savedUsers = userRepository.saveAll(users);
        ProductModel savedProduct = productRepository.save(product);
        
        // Initially no favorites
        assertThat(favoriteService.getFavoriteCount(savedProduct.getId())).isZero();
        
        // When: Add favorites
        int addedCount = 0;
        for (int i = 0; i < savedUsers.size(); i += 2) { // Every other user
            UserModel user = savedUsers.get(i);
            favoriteService.toggleFavorite(user.getId(), savedProduct.getId());
            addedCount++;
        }
        
        // Then: Count should reflect additions
        assertThat(favoriteService.getFavoriteCount(savedProduct.getId())).isEqualTo(addedCount);
        
        // When: Remove some favorites
        int removedCount = 0;
        for (int i = 0; i < savedUsers.size(); i += 4) { // Every fourth user (subset)
            UserModel user = savedUsers.get(i);
            if (favoriteService.isFavorited(user.getId(), savedProduct.getId())) {
                favoriteService.removeFavorite(user.getId(), savedProduct.getId());
                removedCount++;
            }
        }
        
        // Then: Count should reflect removals
        long finalCount = favoriteService.getFavoriteCount(savedProduct.getId());
        assertThat(finalCount).isEqualTo(addedCount - removedCount);
        
        // Verify statistics method gives same result
        Map<Long, Long> statistics = favoriteServiceImpl.getFavoriteStatistics(List.of(savedProduct.getId()));
        assertThat(statistics.get(savedProduct.getId())).isEqualTo(finalCount);
    }

    @Property(tries = 50)
    void whenGettingRecentFavorites_thenShouldBeOrderedByCreationTime(@ForAll("validUser") UserModel user,
                                                                      @ForAll("validProducts") List<ProductModel> products) {
        Assume.that(products.size() >= 3);
        
        // Given: Save user and products
        UserModel savedUser = userRepository.save(user);
        List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        // Add favorites with small delays to ensure different timestamps
        for (ProductModel product : savedProducts) {
            favoriteService.toggleFavorite(savedUser.getId(), product.getId());
            try {
                Thread.sleep(1); // Small delay to ensure different timestamps
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // When: Get recent favorites
        int limit = Math.min(savedProducts.size(), 3);
        var recentFavorites = favoriteServiceImpl.getRecentFavorites(savedUser.getId(), limit);
        
        // Then: Should be ordered by creation time (most recent first)
        assertThat(recentFavorites).hasSize(limit);
        
        // The most recently added products should appear first
        // Since we added products in order, the last ones added should be first in results
        for (int i = 0; i < limit - 1; i++) {
            var current = recentFavorites.get(i);
            var next = recentFavorites.get(i + 1);
            
            // Find the products in our saved list to compare their addition order
            int currentIndex = findProductIndex(savedProducts, current.getId());
            int nextIndex = findProductIndex(savedProducts, next.getId());
            
            // More recently added (higher index) should come first
            assertThat(currentIndex).isGreaterThanOrEqualTo(nextIndex);
        }
    }

    @Property(tries = 100)
    void whenMultipleProductsHaveFavorites_thenBatchStatisticsShouldBeConsistent(@ForAll("validUsers") List<UserModel> users,
                                                                                 @ForAll("validProducts") List<ProductModel> products) {
        Assume.that(users.size() >= 2 && products.size() >= 2);
        
        // Given: Save users and products
        List<UserModel> savedUsers = userRepository.saveAll(users);
        List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        // Create favorites
        for (int i = 0; i < savedProducts.size(); i++) {
            ProductModel product = savedProducts.get(i);
            // Each product gets favorited by first (i+1) users
            for (int j = 0; j <= i && j < savedUsers.size(); j++) {
                UserModel user = savedUsers.get(j);
                favoriteService.toggleFavorite(user.getId(), product.getId());
            }
        }
        
        // When: Get batch statistics
        List<Long> productIds = savedProducts.stream()
            .map(ProductModel::getId)
            .collect(Collectors.toList());
        
        Map<Long, Long> batchStatistics = favoriteServiceImpl.getFavoriteStatistics(productIds);
        
        // Then: Batch statistics should match individual counts
        for (int i = 0; i < savedProducts.size(); i++) {
            ProductModel product = savedProducts.get(i);
            Long batchCount = batchStatistics.get(product.getId());
            long individualCount = favoriteService.getFavoriteCount(product.getId());
            
            assertThat(batchCount).isEqualTo(individualCount);
            assertThat(batchCount).isEqualTo(Math.min(i + 1, savedUsers.size()));
        }
    }

    private int findProductIndex(List<ProductModel> products, Long productId) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(productId)) {
                return i;
            }
        }
        return -1;
    }

    @Provide
    Arbitrary<UserModel> validUser() {
        return Arbitraries.create(() -> {
            UserModel user = new UserModel();
            user.setUserName("user_" + System.nanoTime());
            user.setEmail("user_" + System.nanoTime() + "@test.com");
            user.setPassword("password123");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole(UserRole.USER);
            user.setActive(true);
            user.setVerified(true);
            return user;
        });
    }

    @Provide
    Arbitrary<List<UserModel>> validUsers() {
        return validUser().list().ofMinSize(2).ofMaxSize(5);
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
    Arbitrary<List<ProductModel>> validProducts() {
        return validProduct().list().ofMinSize(2).ofMaxSize(5);
    }
}