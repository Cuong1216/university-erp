package com.wiz.universityerpapi.config;

import com.wiz.universityerpapi.entity.ChucDanh;
import com.wiz.universityerpapi.entity.GiangVien;
import com.wiz.universityerpapi.entity.Role;
import com.wiz.universityerpapi.entity.User;
import com.wiz.universityerpapi.repository.ChucDanhRepository;
import com.wiz.universityerpapi.repository.GiangVienRepository;
import com.wiz.universityerpapi.repository.RoleRepository;
import com.wiz.universityerpapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final GiangVienRepository giangVienRepository;
    private final ChucDanhRepository chucDanhRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Checking database seed initialization...");

            // 1. Khởi tạo Roles nếu chưa có
            List<String> roleNames = List.of("ROLE_ADMIN", "ROLE_GIANG_VIEN", "ROLE_GIAO_VU", "ROLE_SINH_VIEN");
            for (String rName : roleNames) {
                if (roleRepository.findByRoleName(rName).isEmpty()) {
                    roleRepository.save(Role.builder().roleName(rName).build());
                    log.info("Created role: {}", rName);
                }
            }

            // 2. Khởi tạo tài khoản Admin
            createAccountIfNotExists("admin", "admin123", "ROLE_ADMIN", null, new BigDecimal("1.50"));

            // 3. Khởi tạo tài khoản Giảng viên (GV001)
            createAccountIfNotExists("gv01", "gv123", "ROLE_GIANG_VIEN", "GV001", new BigDecimal("1.30"));

            // 4. Khởi tạo tài khoản Giáo vụ
            createAccountIfNotExists("giaovu", "giaovu123", "ROLE_GIAO_VU", null, new BigDecimal("1.00"));

            // 5. Khởi tạo tài khoản Sinh viên
            createAccountIfNotExists("sv01", "sv123", "ROLE_SINH_VIEN", null, new BigDecimal("1.00"));

            log.info("Database seed verification complete.");
        };
    }

    private void createAccountIfNotExists(String username, String rawPassword, String roleName, String maGv, BigDecimal heSo) {
        if (!userRepository.existsByUsername(username)) {
            Optional<Role> roleOpt = roleRepository.findByRoleName(roleName);
            if (roleOpt.isPresent()) {
                User user = User.builder()
                        .username(username)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .status("ACTIVE")
                        .roles(Collections.singleton(roleOpt.get()))
                        .build();
                User savedUser = userRepository.save(user);

                if (maGv != null && !giangVienRepository.existsById(maGv)) {
                    String maCd = "CD_INIT_" + maGv;
                    ChucDanh chucDanh = chucDanhRepository.findById(maCd).orElseGet(() -> {
                        ChucDanh cd = ChucDanh.builder()
                                .maCd(maCd)
                                .tenCd("Chức danh " + maGv)
                                .heSoCd(heSo != null ? heSo : new BigDecimal("1.00"))
                                .build();
                        return chucDanhRepository.save(cd);
                    });

                    GiangVien gv = GiangVien.builder()
                            .maGv(maGv)
                            .user(savedUser)
                            .chucDanh(chucDanh)
                            .hoDem("Nguyen")
                            .ten("Giang Vien " + maGv)
                            .email(username + "@truong.edu.vn")
                            .trangThaiLamViec("Đang công tác")
                            .build();
                    giangVienRepository.save(gv);
                }

                log.info("Created test user '{}' with role '{}' and maGv '{}'", username, roleName, maGv);
            }
        }
    }
}
