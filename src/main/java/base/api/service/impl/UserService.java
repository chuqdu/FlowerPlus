package base.api.service.impl;

import base.api.dto.request.DeliveryAddressDto;
import base.api.dto.request.RegisterDto;
import base.api.entity.DeliveryAddressModel;
import base.api.entity.UserModel;
import base.api.enums.UserGender;
import base.api.repository.IDeliveryAddressRepository;
import base.api.repository.IUserRepository;
import base.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IDeliveryAddressRepository deliveryAddressRepository;


    @Override
    public UserModel createUser(UserModel model) {
        return userRepository.save(model);
    }

    @Override
    public UserModel findByUserName(String userName) {
        return userRepository.findByUserName(userName).orElse(null);
    }


    @Override
    public boolean existedByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserModel findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserModel registerUser(RegisterDto dto) {
        UserModel user = userRepository.findByUserName(dto.getUserName()).orElse(null);

        if(user == null){
        // logic
           UserModel newUser = new UserModel();
           newUser.setUserName(dto.getUserName());
           newUser.setEmail(dto.getEmail());
           newUser.setFirstName(dto.getFirstName());
           newUser.setPhone(dto.getPhone());
           newUser.setLastName(dto.getLastName());
           newUser.setRole(dto.getRole());
           newUser.setGender(UserGender.MALE);
           newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
           return userRepository.save(newUser);

        }

        return null;
    }

    @Override
    @Transactional
    public UserModel createUpdateUserAddress(DeliveryAddressDto dto) {
        Long userId = dto.getUserId();
        UserModel user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id=" + userId);
        }

        DeliveryAddressModel address;
        if (dto.getId() != null) {
            address = deliveryAddressRepository
                    .findByIdAndUserId(dto.getId(), userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Delivery address not found with id=" + dto.getId() + " for userId=" + userId));
        } else {
            address = new DeliveryAddressModel();
            address.setUserId(userId);
        }

        address.setAddress(dto.getAddress());
        address.setRecipientName(dto.getRecipientName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setProvince(dto.getProvince());
        address.setDistrict(dto.getDistrict());
        address.setWard(dto.getWard());

        boolean shouldBeDefault = dto.isDefault();
        address.setDefault(shouldBeDefault);

        if (shouldBeDefault) {
            List<DeliveryAddressModel> others = deliveryAddressRepository.findByUserId(userId);
            for (DeliveryAddressModel it : others) {
                if (it.getId() != null && !it.getId().equals(address.getId()) && it.isDefault()) {
                    it.setDefault(false);
                }
                deliveryAddressRepository.saveAll(others);
            }

            deliveryAddressRepository.save(address);

        }
        return userRepository.findById(userId).orElse(user);

    };

    @Override
    @Transactional
    public boolean deleteDeliveryAddress(Long id, Long userId) {
        // 1) Kiểm tra địa chỉ có thuộc user không
        DeliveryAddressModel address = deliveryAddressRepository
                .findByIdAndUserId(id, userId)
                .orElse(null);

        if (address == null) {
            return false;
        }

        boolean wasDefault = address.isDefault();

        deliveryAddressRepository.delete(address);

        if (wasDefault) {
            List<DeliveryAddressModel> remaining = deliveryAddressRepository.findByUserId(userId);
            if (!remaining.isEmpty()) {
                DeliveryAddressModel makeDefault = remaining.get(0);
                if (!makeDefault.isDefault()) {
                    makeDefault.setDefault(true);
                    deliveryAddressRepository.save(makeDefault);
                }
            }
        }

        return true;
    }

    @Override
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }


}
