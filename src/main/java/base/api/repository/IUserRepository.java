package base.api.repository;

import base.api.enums.UserRole;
import base.api.entity.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JpaRepository<UserModel, Long> là một interface trong Spring Data JPA
 * cung cấp các phương thức CRUD (Create, Read, Update, Delete) cơ bản cho thực thể UserModel với kiểu dữ liệu của khóa chính là Long.
 * Ngoài ra, chúng ta còn có thể viết các phương thức truy vấn tùy chỉnh dựa trên
 * tên phương thức hoặc sử dụng @Query để viết các truy vấn JPQL hoặc SQL tùy chỉnh. (được viết bằng JPQL - Java Persistence Query Language)
 */

@Repository
public interface IUserRepository extends JpaRepository<UserModel, Long>, JpaSpecificationExecutor<UserModel> {

    UserModel findByUserNameAndEmail(String userName, String Email);

    Optional<UserModel> findByEmail(String email);

    @Query("SELECT u FROM UserModel u WHERE u.userName = :login OR u.email = :login")
    Optional<UserModel> findByUserName(@Param("login") String login);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    Page<UserModel> findAllByRole(UserRole role, Pageable pageable);
    Page<UserModel> findAllByUserNameAndRole(String userName, UserRole role, Pageable pageable);

    @Query("SELECT u FROM UserModel u WHERE u.userName = :login OR u.email = :login")
    Optional<UserModel> findByUserNameOrEmail(@Param("login") String login);

    /**
     * Where userName LIKE %?
     */
    List<UserModel> findByUserNameStartingWith(String userName);

    /**
     * Where userName LIKE %?%
     */

    List<UserModel> findByUserNameContaining(String userName);

    // RAW JPQL
    @Query("SELECT u FROM UserModel u WHERE u.userName = ?1 AND u.email = ?2")
    List<UserModel> getUserEntityBy(String userName, String email);

    int countByCreatedAtAfter(LocalDateTime after);


}
