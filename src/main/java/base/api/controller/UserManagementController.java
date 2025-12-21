package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.request.paging.PageResponseDTO;
import base.api.dto.response.TFUResponse;
import base.api.entity.UserModel;
import base.api.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user-management")
public class UserManagementController extends BaseAPIController {

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_OWNER')")
    public ResponseEntity<TFUResponse<List<UserModel>>> getUsers(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            // Convert page from 1-based to 0-based for Spring Data
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Specification<UserModel> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    String searchPattern = "%" + searchTerm.trim().toLowerCase() + "%";
                    Predicate userNamePredicate = cb.like(cb.lower(root.get("userName")), searchPattern);
                    Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchPattern);
                    Predicate firstNamePredicate = cb.like(cb.lower(root.get("firstName")), searchPattern);
                    Predicate lastNamePredicate = cb.like(cb.lower(root.get("lastName")), searchPattern);
                    Predicate phonePredicate = cb.like(cb.lower(root.get("phone")), searchPattern);
                    
                    predicates.add(cb.or(
                        userNamePredicate,
                        emailPredicate,
                        firstNamePredicate,
                        lastNamePredicate,
                        phonePredicate
                    ));
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            
            Page<UserModel> userPage = userRepository.findAll(spec, pageable);
            List<UserModel> users = userPage.getContent();
            
            return success(users);
        } catch (Exception e) {
            return badRequest("Có lỗi xảy ra khi lấy danh sách người dùng: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_OWNER')")
    public ResponseEntity<TFUResponse<UserModel>> updateUser(
            @PathVariable Long id,
            @RequestBody UserModel userUpdate) {
        try {
            UserModel existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
            // Update fields
            if (userUpdate.getFirstName() != null) {
                existingUser.setFirstName(userUpdate.getFirstName());
            }
            if (userUpdate.getLastName() != null) {
                existingUser.setLastName(userUpdate.getLastName());
            }
            if (userUpdate.getEmail() != null) {
                existingUser.setEmail(userUpdate.getEmail());
            }
            if (userUpdate.getPhone() != null) {
                existingUser.setPhone(userUpdate.getPhone());
            }
            if (userUpdate.getRole() != null) {
                existingUser.setRole(userUpdate.getRole());
            }
            if (userUpdate.getAvatar() != null) {
                existingUser.setAvatar(userUpdate.getAvatar());
            }
            
            UserModel updatedUser = userRepository.save(existingUser);
            return success(updatedUser, "Cập nhật người dùng thành công");
        } catch (Exception e) {
            return badRequest("Có lỗi xảy ra khi cập nhật người dùng: " + e.getMessage());
        }
    }

    @PutMapping("/users/{id}/block")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SHOP_OWNER')")
    public ResponseEntity<TFUResponse<UserModel>> blockUser(
            @PathVariable Long id,
            @RequestBody Boolean block) {
        try {
            UserModel user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
            user.setActive(!block); // block = true means set isActive = false
            UserModel updatedUser = userRepository.save(user);
            
            String message = block ? "Đã khóa người dùng" : "Đã mở khóa người dùng";
            return success(updatedUser, message);
        } catch (Exception e) {
            return badRequest("Có lỗi xảy ra khi cập nhật trạng thái người dùng: " + e.getMessage());
        }
    }
}

