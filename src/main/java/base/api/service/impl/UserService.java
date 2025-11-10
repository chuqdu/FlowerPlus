package base.api.service.impl;

import base.api.dto.request.RegisterDto;
import base.api.entity.user.UserModel;
import base.api.repository.IUserRepository;
import base.api.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        // có tồn tại  user nào trùng user name không
        UserModel user = userRepository.findByUserName(dto.getUserName()).orElse(null);

        // không tồn tại -> xử lý tạo mới
        if(user == null){
        // logic
           UserModel newUser = new UserModel();
           newUser.setUserName(dto.getUserName());
           newUser.setEmail(dto.getEmail());
           newUser.setFirstName(dto.getFirstName());
           newUser.setLastName(dto.getLastName());
            // mã hóa mật khẩu
           newUser.setPassword(passwordEncoder.encode(dto.getPassword()));

// end logic

            return  userRepository.save(newUser);

        }

        return null;
    }


}
