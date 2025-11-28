package base.api.config;

import base.api.enums.UserGender;
import base.api.enums.UserRole;
import base.api.entity.UserModel;
import base.api.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
        createDefaultShopOwner();
        createDefaultUser();
    }

    private void createDefaultAdmin() {
        String adminEmail = "admin@gmail.com";

        if (!userService.existedByEmail(adminEmail)) {
            UserModel admin = new UserModel();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("123123"));
            admin.setUserName("admin");
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            admin.setGender(UserGender.MALE);
            admin.setAvatar("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTYHxo84-zFT5V8l697Qq0AwX-1QtzC2lHcfK12GbQ-rtsn-gLPtfpvnHjpzTR9sPs6obQ&usqp=CAU");
            admin.setBirthDate(LocalDateTime.of(1990, 6, 15, 0, 0));
            admin.setActive(true);
            userService.createUser(admin);
            System.out.println("Default admin created: " + adminEmail);
        } else {
            System.out.println("Admin already exists");
        }
    }

    private void createDefaultShopOwner() {
        String shopOwner = "shop@gmail.com";

        if (!userService.existedByEmail(shopOwner)) {
            UserModel shopowner = new UserModel();
            shopowner.setEmail(shopOwner);
            shopowner.setPassword(passwordEncoder.encode("123123"));
            shopowner.setUserName("shop");
            shopowner.setFirstName("Shop");
            shopowner.setLastName("Owner");
            shopowner.setRole(UserRole.SHOP_OWNER);
            shopowner.setActive(true);
            shopowner.setGender(UserGender.MALE);
            shopowner.setAvatar("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTYHxo84-zFT5V8l697Qq0AwX-1QtzC2lHcfK12GbQ-rtsn-gLPtfpvnHjpzTR9sPs6obQ&usqp=CAU");
            shopowner.setBirthDate(LocalDateTime.of(1990, 6, 15, 0, 0));
            shopowner.setActive(true);
            userService.createUser(shopowner);
            System.out.println("Default shop owner created: " + shopOwner);
        } else {
            System.out.println("Shop owner already exists");
        }
    }

    private void createDefaultUser() {
        String userFlower = "user@gmail.com";

        if (!userService.existedByEmail(userFlower)) {
            UserModel user = new UserModel();
            user.setEmail(userFlower);
            user.setPassword(passwordEncoder.encode("123123"));
            user.setUserName("user");
            user.setFirstName("user");
            user.setLastName("flower");
            user.setRole(UserRole.SHOP_OWNER);
            user.setActive(true);
            user.setGender(UserGender.MALE);
            user.setAvatar("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTYHxo84-zFT5V8l697Qq0AwX-1QtzC2lHcfK12GbQ-rtsn-gLPtfpvnHjpzTR9sPs6obQ&usqp=CAU");
            user.setBirthDate(LocalDateTime.of(1990, 6, 15, 0, 0));
            user.setActive(true);
            userService.createUser(user);
            System.out.println("Default user flower created: " + user);
        } else {
            System.out.println("user already exists");
        }
    }


}
