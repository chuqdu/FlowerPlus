package base.api.repository;

import base.api.entity.UserVoucherModel;
import base.api.entity.VoucherModel;
import base.api.entity.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IUserVoucherRepository extends JpaRepository<UserVoucherModel, Long> {
    
    // Find vouchers assigned to a specific user
    List<UserVoucherModel> findByUser_IdOrderByAssignedAtDesc(Long userId);
    
    // Find vouchers assigned to a specific user with pagination
    Page<UserVoucherModel> findByUser_IdOrderByAssignedAtDesc(Long userId, Pageable pageable);
    
    // Find active (unused and not expired) vouchers for a user
    @Query("SELECT uv FROM UserVoucherModel uv WHERE uv.user.id = :userId " +
           "AND uv.isUsed = false " +
           "AND (uv.voucher.startsAt IS NULL OR uv.voucher.startsAt <= :now) " +
           "AND (uv.voucher.endsAt IS NULL OR uv.voucher.endsAt > :now) " +
           "AND (uv.voucher.usageLimit IS NULL OR uv.voucher.usedCount < uv.voucher.usageLimit) " +
           "ORDER BY uv.assignedAt DESC")
    List<UserVoucherModel> findActiveVouchersByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // Find a specific voucher assignment by voucher code and user
    @Query("SELECT uv FROM UserVoucherModel uv WHERE uv.voucher.code = :code AND uv.user.id = :userId")
    Optional<UserVoucherModel> findByVoucherCodeAndUserId(@Param("code") String code, @Param("userId") Long userId);
    
    // Check if a voucher is already assigned to a user
    boolean existsByVoucherAndUser(VoucherModel voucher, UserModel user);
    
    // Find all voucher assignments for admin management
    @Query("SELECT uv FROM UserVoucherModel uv ORDER BY uv.assignedAt DESC")
    Page<UserVoucherModel> findAllOrderByAssignedAtDesc(Pageable pageable);
    
    // Find voucher assignments by voucher ID
    List<UserVoucherModel> findByVoucher_Id(Long voucherId);
    
    // Find voucher assignments created by specific admin
    List<UserVoucherModel> findByCreatedByOrderByAssignedAtDesc(String createdBy);
    
    // Find voucher assignments within date range
    @Query("SELECT uv FROM UserVoucherModel uv WHERE uv.assignedAt BETWEEN :startDate AND :endDate ORDER BY uv.assignedAt DESC")
    List<UserVoucherModel> findByAssignedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find used voucher assignments
    List<UserVoucherModel> findByIsUsedTrueOrderByUsedAtDesc();
    
    // Find unused voucher assignments
    List<UserVoucherModel> findByIsUsedFalseOrderByAssignedAtDesc();
    
    // Count active vouchers for a user
    @Query("SELECT COUNT(uv) FROM UserVoucherModel uv WHERE uv.user.id = :userId " +
           "AND uv.isUsed = false " +
           "AND (uv.voucher.startsAt IS NULL OR uv.voucher.startsAt <= :now) " +
           "AND (uv.voucher.endsAt IS NULL OR uv.voucher.endsAt > :now) " +
           "AND (uv.voucher.usageLimit IS NULL OR uv.voucher.usedCount < uv.voucher.usageLimit)")
    Long countActiveVouchersByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // Find vouchers expiring soon (within next 24 hours)
    @Query("SELECT uv FROM UserVoucherModel uv WHERE uv.isUsed = false " +
           "AND uv.voucher.endsAt IS NOT NULL " +
           "AND uv.voucher.endsAt BETWEEN :now AND :tomorrow " +
           "ORDER BY uv.voucher.endsAt ASC")
    List<UserVoucherModel> findVouchersExpiringSoon(@Param("now") LocalDateTime now, @Param("tomorrow") LocalDateTime tomorrow);
    
    // Admin filtering methods
    @Query("SELECT uv FROM UserVoucherModel uv WHERE " +
           "(:userId IS NULL OR uv.user.id = :userId) " +
           "AND (:isUsed IS NULL OR uv.isUsed = :isUsed) " +
           "AND (:createdBy IS NULL OR uv.createdBy = :createdBy) " +
           "ORDER BY uv.assignedAt DESC")
    Page<UserVoucherModel> findWithFilters(@Param("userId") Long userId, 
                                          @Param("isUsed") Boolean isUsed, 
                                          @Param("createdBy") String createdBy, 
                                          Pageable pageable);
}