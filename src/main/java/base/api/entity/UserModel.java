package base.api.entity;

import base.api.enums.UserGender;
import base.api.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Các @Entity là JPA annotation được sử dụng để đánh dấu một lớp Java như một
 * thực thể (entity) trong cơ sở dữ liệu.
 */

@Data
@Entity
@Table(name = "user")
public class UserModel extends BaseModel {

        public String userName;

        public String phone;

        public String firstName;

        public UserGender gender;

        public String lastName;

        public LocalDateTime birthDate;

        public String avatar;

        public boolean isActive = true;

        public boolean isVerified = false;

        public String email;

        public String password;

        @Enumerated(EnumType.STRING)
        @Column(length = 50)
        private UserRole role;

        @OneToMany(mappedBy = "userModel", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private List<CategoryModel> categories = new ArrayList<>();

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonBackReference
        private List<OrderModel> orders = new ArrayList<>();

        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
        @JsonManagedReference
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private CartModel cart;

        @OneToMany(mappedBy = "userModel", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonBackReference
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private List<ProductModel> products = new ArrayList<>();

        @OneToMany(mappedBy = "userModel", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private List<DeliveryAddressModel> deliveryAddresses = new ArrayList<>();

        public void setCart(CartModel cart) {
                this.cart = cart;
                if (cart != null && cart.getUser() != this) {
                        cart.setUser(this);
                }
        }
}
