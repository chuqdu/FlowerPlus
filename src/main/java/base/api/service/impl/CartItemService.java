package base.api.service.impl;

import base.api.entity.CartItemModel;
import base.api.repository.ICartItemRepository;
import base.api.service.ICartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemService implements ICartItemService {

    @Autowired
    private ICartItemRepository cartItemRepository;

//    @Override
//    public List<CartItemModel> getCartItemsByCartId(Long cartId) {
//        return cartItemRepository.findByCartModel_Id(cartId);
//    }
}
