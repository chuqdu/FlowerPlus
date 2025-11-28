package base.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "carts")
public class CartModel extends BaseModel {

    private double totalPrice;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CartItemModel> cartItems = new ArrayList<>();


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference
    private UserModel user;

    public void addItem(CartItemModel item) {
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItemModel item) {
        cartItems.remove(item);
        item.setCart(null);
    }


}
