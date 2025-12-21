package base.api.service.impl;

import base.api.dto.request.DeliveryAddressDto;
import base.api.dto.request.RegisterDto;
import base.api.dto.request.UpdateProfileDto;
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

    @Autowired
    private base.api.repository.IPasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private base.api.repository.IEmailVerificationTokenRepository emailVerificationTokenRepository;


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

    @Autowired
    private base.api.config.EmailService emailService;

    @Override
    public UserModel registerUser(RegisterDto dto) {
        // Kiểm tra username đã tồn tại
        UserModel existingUser = userRepository.findByUserName(dto.getUserName()).orElse(null);
        if(existingUser != null){
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Kiểm tra email đã tồn tại
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // Tạo user mới
        UserModel newUser = new UserModel();
        newUser.setUserName(dto.getUserName());
        newUser.setEmail(dto.getEmail());
        newUser.setFirstName(dto.getFirstName());
        newUser.setPhone(dto.getPhone());
        newUser.setLastName(dto.getLastName());
        newUser.setRole(dto.getRole() != null ? dto.getRole() : base.api.enums.UserRole.USER);
        newUser.setGender(UserGender.MALE);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setVerified(false); // Chưa xác thực email
        
        UserModel savedUser = userRepository.save(newUser);

        // Tạo token xác thực email
        String verificationToken = java.util.UUID.randomUUID().toString();
        String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));

        base.api.entity.EmailVerificationTokenModel tokenModel = new base.api.entity.EmailVerificationTokenModel();
        tokenModel.setVerificationToken(verificationToken);
        tokenModel.setVerificationCode(verificationCode);
        tokenModel.setEmail(savedUser.getEmail());
        tokenModel.setUserId(savedUser.getId());
        tokenModel.setExpiresAt(java.time.LocalDateTime.now().plusHours(24)); // Hết hạn sau 24 giờ
        emailVerificationTokenRepository.save(tokenModel);

        // Gửi email xác thực
        try {
            String subject = "Xác thực tài khoản FlowerPlus 🌸";
            String fullName = (dto.getFirstName() != null ? dto.getFirstName() : "") + 
                            (dto.getLastName() != null ? " " + dto.getLastName() : "");
            if(fullName.trim().isEmpty()) {
                fullName = dto.getUserName();
            }

            String verifyUrl = "https://flower-plus.vercel.app/auth/verify-email/" + verificationToken;
            
            String body = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>" +
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                "</div>" +
                "<h2 style='color: #e91e63;'>Xin chào %s!</h2>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>FlowerPlus</strong>!</p>" +
                "<p>Để hoàn tất đăng ký, vui lòng xác thực địa chỉ email của bạn.</p>" +
                "<div style='background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: center;'>" +
                "<h3 style='color: #e91e63; margin-top: 0;'>Mã xác thực của bạn:</h3>" +
                "<div style='font-size: 32px; font-weight: bold; color: #e91e63; letter-spacing: 5px; margin: 15px 0;'>%s</div>" +
                "<p style='color: #666; font-size: 14px;'>Mã này sẽ hết hạn sau 24 giờ</p>" +
                "</div>" +
                "<p>Hoặc bạn có thể click vào nút bên dưới để xác thực email:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='%s' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Xác thực email</a>" +
                "</div>" +
                "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                "<h3 style='color: #e91e63; margin-top: 0;'>Thông tin tài khoản:</h3>" +
                "<p><strong>Tên đăng nhập:</strong> %s</p>" +
                "<p><strong>Email:</strong> %s</p>" +
                "</div>" +
                "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #856404;'><strong>⚠️ Lưu ý:</strong> Bạn cần xác thực email để có thể đăng nhập vào hệ thống.</p>" +
                "</div>" +
                "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                fullName,
                verificationCode,
                verifyUrl,
                dto.getUserName(),
                dto.getEmail()
            );
            
            emailService.sendHtmlEmail(dto.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return savedUser;
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

        }
        else{
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

    @Override
    @Transactional
    public base.api.dto.response.InitiateForgotPasswordResponse initiateForgotPassword(String contactInfo) throws Exception {
        // Tìm user theo email hoặc username
        UserModel user = null;
        if (contactInfo.contains("@")) {
            user = userRepository.findByEmail(contactInfo).orElse(null);
        } else {
            user = userRepository.findByUserName(contactInfo).orElse(null);
        }

        if (user == null) {
            throw new Exception("Không tìm thấy tài khoản với thông tin này");
        }

        // Xóa token cũ nếu có
        passwordResetTokenRepository.deleteByEmail(user.getEmail());

        // Tạo token và mã xác thực
        String resetToken = java.util.UUID.randomUUID().toString();
        String verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));

        // Lưu token
        base.api.entity.PasswordResetTokenModel tokenModel = new base.api.entity.PasswordResetTokenModel();
        tokenModel.setResetToken(resetToken);
        tokenModel.setVerificationCode(verificationCode);
        tokenModel.setEmail(user.getEmail());
        tokenModel.setUserId(user.getId());
        tokenModel.setExpiresAt(java.time.LocalDateTime.now().plusHours(1)); // Hết hạn sau 1 giờ
        passwordResetTokenRepository.save(tokenModel);

        // Gửi email
        try {
            String subject = "Đặt lại mật khẩu FlowerPlus 🔐";
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + 
                            (user.getLastName() != null ? " " + user.getLastName() : "");
            if(fullName.trim().isEmpty()) {
                fullName = user.getUserName();
            }

            String resetUrl = "https://flower-plus.vercel.app/auth/forgot-password/" + resetToken;
            
            String body = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>" +
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                "</div>" +
                "<h2 style='color: #e91e63;'>Xin chào %s!</h2>" +
                "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>" +
                "<div style='background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: center;'>" +
                "<h3 style='color: #e91e63; margin-top: 0;'>Mã xác thực của bạn:</h3>" +
                "<div style='font-size: 32px; font-weight: bold; color: #e91e63; letter-spacing: 5px; margin: 15px 0;'>%s</div>" +
                "<p style='color: #666; font-size: 14px;'>Mã này sẽ hết hạn sau 1 giờ</p>" +
                "</div>" +
                "<p>Hoặc bạn có thể click vào nút bên dưới để đặt lại mật khẩu:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='%s' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Đặt lại mật khẩu</a>" +
                "</div>" +
                "<div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #856404;'><strong>⚠️ Lưu ý:</strong> Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>" +
                "</div>" +
                "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                fullName,
                verificationCode,
                resetUrl
            );
            
            emailService.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send reset password email: " + e.getMessage());
            throw new Exception("Không thể gửi email. Vui lòng thử lại sau.");
        }

        base.api.dto.response.InitiateForgotPasswordResponse response = new base.api.dto.response.InitiateForgotPasswordResponse();
        response.setResetToken(resetToken);
        response.setMessage("Mã xác thực đã được gửi đến email của bạn");
        return response;
    }

    @Override
    @Transactional
    public void completeForgotPassword(base.api.dto.request.CompleteForgotPasswordDto dto) throws Exception {
        // Kiểm tra mật khẩu khớp
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new Exception("Mật khẩu xác nhận không khớp");
        }

        // Kiểm tra độ dài mật khẩu
        if (dto.getNewPassword().length() < 6) {
            throw new Exception("Mật khẩu phải có ít nhất 6 ký tự");
        }

        // Tìm token
        base.api.entity.PasswordResetTokenModel tokenModel = passwordResetTokenRepository
                .findByResetToken(dto.getResetToken())
                .orElseThrow(() -> new Exception("Token không hợp lệ"));

        // Kiểm tra token đã sử dụng
        if (tokenModel.isUsed()) {
            throw new Exception("Token đã được sử dụng");
        }

        // Kiểm tra token hết hạn
        if (tokenModel.isExpired()) {
            throw new Exception("Token đã hết hạn");
        }

        // Kiểm tra mã xác thực
        if (!tokenModel.getVerificationCode().equals(dto.getVerificationCode())) {
            throw new Exception("Mã xác thực không đúng");
        }

        // Cập nhật mật khẩu
        UserModel user = userRepository.findById(tokenModel.getUserId())
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng"));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // Đánh dấu token đã sử dụng
        tokenModel.setUsed(true);
        passwordResetTokenRepository.save(tokenModel);
    }

    @Override
    @Transactional
    public void verifyEmail(base.api.dto.request.VerifyEmailDto dto) throws Exception {
        // Tìm token
        base.api.entity.EmailVerificationTokenModel tokenModel = emailVerificationTokenRepository
                .findByVerificationToken(dto.getVerificationToken())
                .orElseThrow(() -> new Exception("Token không hợp lệ"));

        // Kiểm tra token đã sử dụng
        if (tokenModel.isUsed()) {
            throw new Exception("Token đã được sử dụng");
        }

        // Kiểm tra token hết hạn
        if (tokenModel.isExpired()) {
            throw new Exception("Token đã hết hạn");
        }

        // Kiểm tra mã xác thực
        if (!tokenModel.getVerificationCode().equals(dto.getVerificationCode())) {
            throw new Exception("Mã xác thực không đúng");
        }

        // Cập nhật trạng thái verified cho user
        UserModel user = userRepository.findById(tokenModel.getUserId())
                .orElseThrow(() -> new Exception("Không tìm thấy người dùng"));

        user.setVerified(true);
        userRepository.save(user);

        // Đánh dấu token đã sử dụng
        tokenModel.setUsed(true);
        emailVerificationTokenRepository.save(tokenModel);

        // Gửi email chào mừng sau khi xác thực thành công
        try {
            String subject = "Chào mừng bạn đến với FlowerPlus! 🌸";
            String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + 
                            (user.getLastName() != null ? " " + user.getLastName() : "");
            if(fullName.trim().isEmpty()) {
                fullName = user.getUserName();
            }
            
            String body = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>" +
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                "<h1 style='color: #e91e63; margin: 0;'>🌸 FlowerPlus 🌸</h1>" +
                "</div>" +
                "<h2 style='color: #e91e63;'>Xin chào %s!</h2>" +
                "<p>Email của bạn đã được xác thực thành công! 🎉</p>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>FlowerPlus</strong> - Nơi mang đến những bông hoa tươi đẹp nhất!</p>" +
                "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                "<h3 style='color: #e91e63; margin-top: 0;'>Thông tin tài khoản:</h3>" +
                "<p><strong>Tên đăng nhập:</strong> %s</p>" +
                "<p><strong>Email:</strong> %s</p>" +
                "<p><strong>Trạng thái:</strong> <span style='color: #4caf50; font-weight: bold;'>✓ Đã xác thực</span></p>" +
                "</div>" +
                "<p>Bạn có thể bắt đầu khám phá và mua sắm các sản phẩm hoa tươi đẹp của chúng tôi ngay bây giờ!</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='http://localhost:3000' style='background-color: #e91e63; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Khám phá ngay</a>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi.</p>" +
                "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>© 2024 FlowerPlus. All rights reserved.</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                fullName,
                user.getUserName(),
                user.getEmail()
            );
            
            emailService.sendHtmlEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserModel updateProfile(Long userId, UpdateProfileDto dto) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra email đã tồn tại (nếu thay đổi email)
        if (!user.getEmail().equals(dto.getEmail()) && existedByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
        }

        // Cập nhật thông tin
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setBirthDate(dto.getBirthDate());
        
        if (dto.getGender() != null) {
            user.setGender(UserGender.valueOf(dto.getGender()));
        }

        return userRepository.save(user);
    }
}
