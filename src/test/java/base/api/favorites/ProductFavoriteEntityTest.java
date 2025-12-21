package base.api.favorites;

import base.api.entity.ProductFavoriteModel;
import base.api.entity.ProductModel;
import base.api.entity.UserModel;
import base.api.enums.ProductType;
import base.api.enums.UserRole;
import base.api.repository.IProductRepository;
import base.api.repository.IUserRepository;
import base.api.repository.ProductFavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ProductFavoriteEntityTest {

    @Autowired
    private ProductFavoriteRepository favoriteRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductRepository productRepository;

    private UserModel testUser;
    private ProductModel testProduct;

    @BeforeEach
    void setUp() {
        // Create test user
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

        // Create test product
        testProduct = new ProductModel();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test product description");
        testProduct.setPrice(100.0);
        testProduct.setStock(10);
        testProduct.setProductType(ProductType.PRODUCT);
        testProduct.setIsActive(true);
        testProduct.setImages("test-image.jpg");
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void shouldCreateFavoriteSuccessfully() {
        // Given
        ProductFavoriteModel favorite = new ProductFavoriteModel(testUser.getId(), testProduct.getId());

        // When
        ProductFavoriteModel savedFavorite = favoriteRepository.save(favorite);

        // Then
        assertThat(savedFavorite.getId()).isNotNull();
        assertThat(savedFavorite.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedFavorite.getProductId()).isEqualTo(testProduct.getId());
        assertThat(savedFavorite.getCreatedAt()).isNotNull();
        assertThat(savedFavorite.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindFavoriteByUserAndProduct() {
        // Given
        ProductFavoriteModel favorite = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        favoriteRepository.save(favorite);

        // When
        Optional<ProductFavoriteModel> found = favoriteRepository.findByUserIdAndProductId(
            testUser.getId(), testProduct.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(testUser.getId());
        assertThat(found.get().getProductId()).isEqualTo(testProduct.getId());
    }

    @Test
    void shouldCheckIfFavoriteExists() {
        // Given
        ProductFavoriteModel favorite = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        favoriteRepository.save(favorite);

        // When
        boolean exists = favoriteRepository.existsByUserIdAndProductId(testUser.getId(), testProduct.getId());
        boolean notExists = favoriteRepository.existsByUserIdAndProductId(testUser.getId(), 999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldPreventDuplicateFavorites() {
        // Given
        ProductFavoriteModel favorite1 = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        favoriteRepository.save(favorite1);

        // When & Then
        ProductFavoriteModel favorite2 = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        assertThatThrownBy(() -> favoriteRepository.saveAndFlush(favorite2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldCountFavoritesByProduct() {
        // Given
        UserModel user2 = new UserModel();
        user2.setUserName("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("password123");
        user2.setFirstName("Test2");
        user2.setLastName("User2");
        user2.setRole(UserRole.USER);
        user2.setActive(true);
        user2.setVerified(true);
        user2 = userRepository.save(user2);

        ProductFavoriteModel favorite1 = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        ProductFavoriteModel favorite2 = new ProductFavoriteModel(user2.getId(), testProduct.getId());
        favoriteRepository.save(favorite1);
        favoriteRepository.save(favorite2);

        // When
        long count = favoriteRepository.countByProductId(testProduct.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCountFavoritesByUser() {
        // Given
        ProductModel product2 = new ProductModel();
        product2.setName("Test Product 2");
        product2.setDescription("Test product 2 description");
        product2.setPrice(200.0);
        product2.setStock(5);
        product2.setProductType(ProductType.PRODUCT);
        product2.setIsActive(true);
        product2.setImages("test-image2.jpg");
        product2 = productRepository.save(product2);

        ProductFavoriteModel favorite1 = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        ProductFavoriteModel favorite2 = new ProductFavoriteModel(testUser.getId(), product2.getId());
        favoriteRepository.save(favorite1);
        favoriteRepository.save(favorite2);

        // When
        long count = favoriteRepository.countByUserId(testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldDeleteFavoriteByUserAndProduct() {
        // Given
        ProductFavoriteModel favorite = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        favoriteRepository.save(favorite);
        
        assertThat(favoriteRepository.existsByUserIdAndProductId(testUser.getId(), testProduct.getId())).isTrue();

        // When
        favoriteRepository.deleteByUserIdAndProductId(testUser.getId(), testProduct.getId());

        // Then
        assertThat(favoriteRepository.existsByUserIdAndProductId(testUser.getId(), testProduct.getId())).isFalse();
    }

    @Test
    void shouldCascadeDeleteWhenUserIsDeleted() {
        // Given
        ProductFavoriteModel favorite = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        favoriteRepository.save(favorite);
        
        assertThat(favoriteRepository.countByUserId(testUser.getId())).isEqualTo(1);

        // When
        userRepository.delete(testUser);

        // Then
        assertThat(favoriteRepository.countByUserId(testUser.getId())).isZero();
    }

    @Test
    void shouldCascadeDeleteWhenProductIsDeleted() {
        // Given
        ProductFavoriteModel favorite = new ProductFavoriteModel(testUser.getId(), testProduct.getId());
        favoriteRepository.save(favorite);
        
        assertThat(favoriteRepository.countByProductId(testProduct.getId())).isEqualTo(1);

        // When
        productRepository.delete(testProduct);

        // Then
        assertThat(favoriteRepository.countByProductId(testProduct.getId())).isZero();
    }
}