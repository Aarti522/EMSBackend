package com.example.ems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.ems.model.User;
import com.example.ems.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        /*
         * Create default ADMIN only if
         * ADMIN account does not already exist.
         */
        if (userRepository.findByEmail("admin@ems.com") == null) {

            User admin = new User();

            admin.setEmail("admin@ems.com");

            /*
             * Default ADMIN password.
             * It will be stored in encrypted form.
             */
            admin.setPassword(
                    passwordEncoder.encode("admin123")
            );

            admin.setRole("ADMIN");

            /*
             * ADMIN is a system-level user.
             * It does not need an Employee record.
             */
            admin.setEmployee(null);

            /*
             * ADMIN should not be treated as
             * a first-time employee login.
             */
            admin.setFirstLogin(false);

            userRepository.save(admin);

            System.out.println("=================================");
            System.out.println("DEFAULT ADMIN ACCOUNT CREATED");
            System.out.println("Email    : admin@ems.com");
            System.out.println("Password : admin123");
            System.out.println("Role     : ADMIN");
            System.out.println("=================================");

        } else {

            System.out.println("=================================");
            System.out.println("ADMIN ACCOUNT ALREADY EXISTS");
            System.out.println("=================================");
        }
    }
}