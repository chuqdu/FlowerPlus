package base.api.favorites;

import base.api.entity.ProductModel;
import base.api.entity.UserModel;
import base.api.enums.ProductType;
import base.api.enums.UserRole;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import base.api.service.IProductFavoriteService;
import net.jqwik.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: product-favorites, Property 1: Toggle yêu thích**
 * **Validates: Requirements 1.1, 1.3, 3.1**
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductFavoriteServicePropertyTest {

    @Autowired
    private IProductFavoriteService favoriteService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Property(tries = 100)
    void whenToggleFavoriteTwice_thenShouldReturnToOriginalState(@ForAll("validUser") UserModel user,
                                                                 @ForAll("validProduct") ProductModel product) {
        // Given: Save user and product
        UserModel savedUser = userRepository.save(user);
        ProductModel savedProduct = productRepository.save(product);
        
        // Get initial state (should be false - not favorited)
        boolean initialState = favoriteService.isFavorited(savedUser.getId(), savedProduct.getId());
        assertThat(initialState).isFalse();
        
        // When: Toggle favorite twice
        boolean firstToggle = favoriteService.toggleFavorite(savedUser.getId(), savedProduct.getId());
        boolean secondToggle = favoriteService.toggleFavorite(savedUser.getId(), savedProduct.getId());
        
        // Then: Should return to original state
        boolean finalState = favoriteService.isFavorited(savedUser.getId(), savedProduct.getId());
        
        assertThat(finalState).isEqualTo(initialState);
        assertThat(firstToggle).isTrue(); // First toggle adds to favorites
        assertThat(secondToggle).isFalse(); // Second toggle removes from favorites
    }

    @Property(tries = 100)
    void whenToggleFavorite_thenIsFavoritedShouldReflectChange(@ForAll("validUser") UserModel user,
                                                               @ForAll("validProduct") ProductModel product) {
        // Given: Save user and product
        UserModel savedUser = userRepository.save(user);
        ProductModel savedProduct = productRepository.save(product);
        
        // Initially should not be favorited
        assertThat(favoriteService.isFavorited(savedUser.getId(), savedProduct.getId())).isFalse();
        
        // When: Toggle to add favorite
        boolean addResult = favoriteService.toggleFavorite(savedUser.getId(), savedProduct.getId());
        
        // Then: Should be favorited
        assertThat(addResult).isTrue();
        assertThat(favoriteService.isFavorited(savedUser.getId(), savedProduct.getId())).isTrue();
        
        // When: Toggle to remove favorite
        boolean removeResult = favoriteService.toggleFavorite(savedUser.getId(), savedProduct.getId());
        
        // Then: Should not be favorited
        assertThat(removeResult).isFalse();
        assertThat(favoriteService.isFavorited(savedUser.getId(), savedProduct.getId())).isFalse();
    }

    @Property(tries = 100)
    void whenRemoveFavorite_thenShouldNotBeFavorited(@ForAll("validUser") UserModel user,
                                                     @ForAll("validProduct") ProductModel product) {
        // Given: Save user and product
        UserModel savedUser = userRepository.save(user);
        ProductModel savedProduct = productRepository.save(product);
        
        // Add to favorites first
        favoriteService.toggleFavorite(savedUser.getId(), savedProduct.getId());
        assertThat(favoriteService.isFavorited(savedUser.getId(), savedProduct.getId())).isTrue();
        
        // When: Remove favorite
        favoriteService.removeFavorite(savedUser.getId(), savedProduct.getId());
        
        // Then: Should not be favorited
        assertThat(favoriteService.isFavorited(savedUser.getId(), savedProduct.getId())).isFalse();
    }

    @Property(tries = 50)
    void whenMultipleProductsAreFavorited_thenFavoriteStatusMapShouldBeAccurate(@ForAll("validUser") UserModel user,
                                                                                @ForAll("validProducts") java.util.List<ProductModel> products) {
        Assume.that(products.size() >= 2);
        
        // Given: Save user and products
        UserModel savedUser = userRepository.save(user);
        java.util.List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        // Favorite every other product
        java.util.Set<Long> favoritedIds = new java.util.HashSet<>();
        for (int i = 0; i < savedProducts.size(); i += 2) {
            ProductModel product = savedProducts.get(i);
            favoriteService.toggleFavorite(savedUser.getId(), product.getId());
            favoritedIds.add(product.getId());
        }
        
        // When: Get favorite status map
        java.util.List<Long> productIds = savedProducts.stream()
            .map(ProductModel::getId)
            .collect(java.util.stream.Collectors.toList());
        
        java.util.Map<Long, Boolean> statusMap = favoriteService.getFavoriteStatusMap(savedUser.getId(), productIds);
        
        // Then: Status map should be accurate
        assertThat(statusMap).hasSize(savedProducts.size());
        
        for (ProductModel product : savedProducts) {
            boolean expectedStatus = favoritedIds.contains(product.getId());
            assertThat(statusMap.get(product.getId())).isEqualTo(expectedStatus);
        }
    }

    @Property(tries = 100)
    void whenFavoriteCountIsRequested_thenShouldReflectActualFavorites(@ForAll("validUsers") java.util.List<UserModel> users,
                                                                       @ForAll("validProduct") ProductModel product) {
        Assume.that(users.size() >= 1);
        
        // Given: Save users and product
        java.util.List<UserModel> savedUsers = userRepository.saveAll(users);
        ProductModel savedProduct = productRepository.save(product);
        
        // Initially no favorites
        assertThat(favoriteService.getFavoriteCount(savedProduct.getId())).isZero();
        
        // When: Some users favorite the product
        int expectedCount = 0;
        for (int i = 0; i < savedUsers.size(); i += 2) { // Every other user
            UserModel user = savedUsers.get(i);
            favoriteService.toggleFavorite(user.getId(), savedProduct.getId());
            expectedCount++;
        }
        
        // Then: Count should be accurate
        long actualCount = favoriteService.getFavoriteCount(savedProduct.getId());
        assertThat(actualCount).isEqualTo(expectedCount);
        
        // When: Remove some favorites
        int removedCount = 0;
        for (int i = 0; i < savedUsers.size(); i += 4) { // Every fourth user (subset of favorited users)
            UserModel user = savedUsers.get(i);
            if (favoriteService.isFavorited(user.getId(), savedProduct.getId())) {
                favoriteService.removeFavorite(user.getId(), savedProduct.getId());
                removedCount++;
            }
        }
        
        // Then: Count should reflect removals
        long finalCount = favoriteService.getFavoriteCount(savedProduct.getId());
        assertThat(finalCount).isEqualTo(expectedCount - removedCount);
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
    Arbitrary<java.util.List<UserModel>> validUsers() {
        return validUser().list().ofMinSize(1).ofMaxSize(5);
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
        return validProduct().list().ofMinSize(2).ofMaxSize(6);
    }
}