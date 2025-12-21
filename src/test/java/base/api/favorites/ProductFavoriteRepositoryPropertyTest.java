package base.api.favorites;

import base.api.entity.ProductFavoriteModel;
import base.api.entity.ProductModel;
import base.api.entity.UserModel;
import base.api.enums.ProductType;
import base.api.enums.UserRole;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import base.api.repository.ProductFavoriteRepository;
import net.jqwik.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: product-favorites, Property 2: Persistence ngay lập tức**
 * **Validates: Requirements 1.5, 3.4**
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductFavoriteRepositoryPropertyTest {

    @Autowired
    private ProductFavoriteRepository favoriteRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Property(tries = 100)
    void whenFavoriteIsSaved_thenItShouldBeImmediatelyRetrievable(@ForAll("validUser") UserModel user,
                                                                  @ForAll("validProduct") ProductModel product) {
        // Given: Save user and product
        UserModel savedUser = userRepository.save(user);
        ProductModel savedProduct = productRepository.save(product);
        
        // When: Create and save favorite
        ProductFavoriteModel favorite = new ProductFavoriteModel(savedUser.getId(), savedProduct.getId());
        ProductFavoriteModel savedFavorite = favoriteRepository.save(favorite);
        
        // Then: Should be immediately retrievable
        assertThat(savedFavorite.getId()).isNotNull();
        
        Optional<ProductFavoriteModel> retrieved = favoriteRepository.findByUserIdAndProductId(
            savedUser.getId(), savedProduct.getId());
        
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(savedFavorite.getId());
        assertThat(retrieved.get().getUserId()).isEqualTo(savedUser.getId());
        assertThat(retrieved.get().getProductId()).isEqualTo(savedProduct.getId());
    }

    @Property(tries = 100)
    void whenFavoriteIsDeleted_thenItShouldBeImmediatelyUnavailable(@ForAll("validUser") UserModel user,
                                                                    @ForAll("validProduct") ProductModel product) {
        // Given: Save user, product and favorite
        UserModel savedUser = userRepository.save(user);
        ProductModel savedProduct = productRepository.save(product);
        
        ProductFavoriteModel favorite = new ProductFavoriteModel(savedUser.getId(), savedProduct.getId());
        favoriteRepository.save(favorite);
        
        // Verify favorite exists
        assertThat(favoriteRepository.existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId())).isTrue();
        
        // When: Delete favorite
        favoriteRepository.deleteByUserIdAndProductId(savedUser.getId(), savedProduct.getId());
        
        // Then: Should be immediately unavailable
        assertThat(favoriteRepository.existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId())).isFalse();
        
        Optional<ProductFavoriteModel> retrieved = favoriteRepository.findByUserIdAndProductId(
            savedUser.getId(), savedProduct.getId());
        assertThat(retrieved).isEmpty();
    }

    @Property(tries = 100)
    void whenMultipleFavoritesAreSaved_thenCountShouldBeAccurate(@ForAll("validUser") UserModel user,
                                                                 @ForAll("validProducts") java.util.List<ProductModel> products) {
        // Given: Save user and products
        UserModel savedUser = userRepository.save(user);
        java.util.List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        // When: Create favorites for all products
        for (ProductModel product : savedProducts) {
            ProductFavoriteModel favorite = new ProductFavoriteModel(savedUser.getId(), product.getId());
            favoriteRepository.save(favorite);
        }
        
        // Then: Count should be accurate immediately
        long count = favoriteRepository.countByUserId(savedUser.getId());
        assertThat(count).isEqualTo(savedProducts.size());
        
        // Verify each favorite exists
        for (ProductModel product : savedProducts) {
            assertThat(favoriteRepository.existsByUserIdAndProductId(savedUser.getId(), product.getId())).isTrue();
        }
    }

    @Property(tries = 100)
    void whenFavoriteStatusChanges_thenExistenceCheckShouldReflectChange(@ForAll("validUser") UserModel user,
                                                                         @ForAll("validProduct") ProductModel product) {
        // Given: Save user and product
        UserModel savedUser = userRepository.save(user);
        ProductModel savedProduct = productRepository.save(product);
        
        // Initially no favorite should exist
        assertThat(favoriteRepository.existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId())).isFalse();
        
        // When: Add favorite
        ProductFavoriteModel favorite = new ProductFavoriteModel(savedUser.getId(), savedProduct.getId());
        favoriteRepository.save(favorite);
        
        // Then: Should immediately exist
        assertThat(favoriteRepository.existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId())).isTrue();
        
        // When: Remove favorite
        favoriteRepository.deleteByUserIdAndProductId(savedUser.getId(), savedProduct.getId());
        
        // Then: Should immediately not exist
        assertThat(favoriteRepository.existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId())).isFalse();
    }

    @Property(tries = 50)
    void whenFavoritesAreQueriedWithPagination_thenResultsShouldBeConsistent(@ForAll("validUser") UserModel user,
                                                                             @ForAll("validProducts") java.util.List<ProductModel> products) {
        Assume.that(products.size() >= 2); // Need at least 2 products for meaningful pagination test
        
        // Given: Save user and products
        UserModel savedUser = userRepository.save(user);
        java.util.List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        // Create favorites
        for (ProductModel product : savedProducts) {
            ProductFavoriteModel favorite = new ProductFavoriteModel(savedUser.getId(), product.getId());
            favoriteRepository.save(favorite);
        }
        
        // When: Query with pagination
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(0, Math.max(1, products.size() / 2));
        
        org.springframework.data.domain.Page<ProductFavoriteModel> page = 
            favoriteRepository.findByUserIdWithProduct(savedUser.getId(), pageable);
        
        // Then: Results should be consistent
        assertThat(page.getTotalElements()).isEqualTo(savedProducts.size());
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().size()).isLessThanOrEqualTo(pageable.getPageSize());
        
        // Verify all returned favorites belong to the user
        for (ProductFavoriteModel fav : page.getContent()) {
            assertThat(fav.getUserId()).isEqualTo(savedUser.getId());
            assertThat(fav.getProduct()).isNotNull();
            assertThat(fav.getProduct().getIsActive()).isTrue();
        }
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
        return validProduct().list().ofMinSize(1).ofMaxSize(5);
    }
}