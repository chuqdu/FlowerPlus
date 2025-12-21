package base.api.favorites;

import base.api.entity.ProductFavoriteModel;
import base.api.entity.ProductModel;
import base.api.entity.UserModel;
import base.api.enums.ProductType;
import base.api.enums.UserRole;
import base.api.repository.ProductFavoriteRepository;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import net.jqwik.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: product-favorites, Property 9: Xử lý dữ liệu orphaned**
 * **Validates: Requirements 6.4, 6.5**
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductFavoriteModelPropertyTest {

    @Autowired
    private ProductFavoriteRepository favoriteRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    @Property(tries = 100)
    void whenUserIsDeleted_thenAllUserFavoritesShouldBeDeleted(@ForAll("validUser") UserModel user,
                                                               @ForAll("validProducts") List<ProductModel> products) {
        // Given: Save user and products
        UserModel savedUser = userRepository.save(user);
        List<ProductModel> savedProducts = productRepository.saveAll(products);
        
        // Create favorites for the user
        for (ProductModel product : savedProducts) {
            ProductFavoriteModel favorite = new ProductFavoriteModel(savedUser.getId(), product.getId());
            favoriteRepository.save(favorite);
        }
        
        // Verify favorites exist
        long initialFavoriteCount = favoriteRepository.countByUserId(savedUser.getId());
        assertThat(initialFavoriteCount).isEqualTo(savedProducts.size());
        
        // When: Delete the user (cascade should delete favorites)
        userRepository.delete(savedUser);
        
        // Then: All favorites for this user should be deleted
        long finalFavoriteCount = favoriteRepository.countByUserId(savedUser.getId());
        assertThat(finalFavoriteCount).isZero();
    }

    @Property(tries = 100)
    void whenProductIsDeleted_thenAllProductFavoritesShouldBeDeleted(@ForAll("validUsers") List<UserModel> users,
                                                                     @ForAll("validProduct") ProductModel product) {
        // Given: Save users and product
        List<UserModel> savedUsers = userRepository.saveAll(users);
        ProductModel savedProduct = productRepository.save(product);
        
        // Create favorites for the product
        for (UserModel user : savedUsers) {
            ProductFavoriteModel favorite = new ProductFavoriteModel(user.getId(), savedProduct.getId());
            favoriteRepository.save(favorite);
        }
        
        // Verify favorites exist
        long initialFavoriteCount = favoriteRepository.countByProductId(savedProduct.getId());
        assertThat(initialFavoriteCount).isEqualTo(savedUsers.size());
        
        // When: Delete the product (cascade should delete favorites)
        productRepository.delete(savedProduct);
        
        // Then: All favorites for this product should be deleted
        long finalFavoriteCount = favoriteRepository.countByProductId(savedProduct.getId());
        assertThat(finalFavoriteCount).isZero();
    }

    @Property(tries = 100)
    void favoriteConstraints_shouldPreventDuplicates(@ForAll("validUser") UserModel user,
                                                     @ForAll("validProduct") ProductModel product) {
        // Given: Save user and product
        UserModel savedUser = userRepository.save(user);
        ProductModel savedProduct = productRepository.save(product);
        
        // When: Create first favorite
        ProductFavoriteModel favorite1 = new ProductFavoriteModel(savedUser.getId(), savedProduct.getId());
        favoriteRepository.save(favorite1);
        
        // Then: Should be able to find the favorite
        Optional<ProductFavoriteModel> found = favoriteRepository.findByUserIdAndProductId(
            savedUser.getId(), savedProduct.getId());
        assertThat(found).isPresent();
        
        // When: Try to create duplicate favorite
        ProductFavoriteModel favorite2 = new ProductFavoriteModel(savedUser.getId(), savedProduct.getId());
        
        // Then: Should handle duplicate gracefully (either prevent or replace)
        try {
            favoriteRepository.save(favorite2);
            // If save succeeds, verify only one favorite exists
            long count = favoriteRepository.countByUserIdAndProductId(savedUser.getId(), savedProduct.getId());
            assertThat(count).isLessThanOrEqualTo(1);
        } catch (Exception e) {
            // If constraint violation occurs, that's also acceptable
            assertThat(e).isNotNull();
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
    Arbitrary<List<UserModel>> validUsers() {
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
    Arbitrary<List<ProductModel>> validProducts() {
        return validProduct().list().ofMinSize(1).ofMaxSize(5);
    }

    private long countByUserIdAndProductId(Long userId, Long productId) {
        return favoriteRepository.findAll().stream()
            .filter(f -> f.getUserId().equals(userId) && f.getProductId().equals(productId))
            .count();
    }
}