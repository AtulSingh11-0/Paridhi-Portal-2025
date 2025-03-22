package com.megatronix.paridhi.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialAdminSetup implements ApplicationRunner {
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  
  @Value("${super-admin.default.email}")
  private String defatulSuperAdminEmail;

  @Value("${super-admin.default.password}")
  private String defatulSuperAdminPassword;

  @Value("${super-admin.default.name}")
  private String defatulSuperAdminName;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    // check if any super admin exists
    if ( userRepository.findAll().stream().noneMatch(user -> Role.ROLE_SUPERADMIN.equals(user.getRole())) ) {
      log.info("No super admin found. Creating default super admin");
      
      // create default super admin and save to database
      User superAdmin = User.builder()
        .email(defatulSuperAdminEmail)
        .name(defatulSuperAdminName)
        .password(passwordEncoder.encode(defatulSuperAdminPassword))
        .role(Role.ROLE_SUPERADMIN)
        .isVerified(true)
        .createdAt(LocalDateTime.now())
      .build();

      userRepository.save(superAdmin);

      log.info("Default super admin created with email:{} ", defatulSuperAdminEmail);
      log.info("Default super admin created with password:{} ", defatulSuperAdminPassword);
    } else {
      log.info("Super admin already exists");
    }
  }
}
