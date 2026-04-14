package com.aps.api.config;

import com.aps.domain.entity.User;
import com.aps.domain.enums.RoleType;
import com.aps.service.UserService;
import com.aps.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${app.seed.admin.password:#{null}}")
    private String adminPassword;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:admin@aps.com}")
    private String adminEmail;

    @Value("${app.seed.planner.password:#{null}}")
    private String plannerPassword;

    @Value("${app.seed.supervisor.password:#{null}}")
    private String supervisorPassword;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("数据初始化已禁用");
            return;
        }

        // 检查是否已有用户
        if (userRepository.count() > 0) {
            log.info("数据库已有用户，跳过初始化");
            return;
        }

        log.info("开始初始化默认数据...");

        try {
            // 验证密码是否配置
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("未配置管理员密码 (app.seed.admin.password)，跳过管理员用户创建");
            } else {
                User admin = userService.createUser(adminUsername, adminPassword, adminEmail, List.of());
                userService.assignRole(admin.getId(), RoleType.ADMIN);
                log.info("默认管理员用户已创建: {}", adminUsername);
            }

            if (plannerPassword == null || plannerPassword.isBlank()) {
                log.warn("未配置计划员密码 (app.seed.planner.password)，跳过计划员用户创建");
            } else {
                User planner = userService.createUser("planner", plannerPassword, "planner@aps.com", List.of());
                userService.assignRole(planner.getId(), RoleType.PLANNER);
                log.info("默认计划员用户已创建: planner");
            }

            if (supervisorPassword == null || supervisorPassword.isBlank()) {
                log.warn("未配置主管密码 (app.seed.supervisor.password)，跳过主管用户创建");
            } else {
                User supervisor = userService.createUser("supervisor", supervisorPassword, "supervisor@aps.com", List.of());
                userService.assignRole(supervisor.getId(), RoleType.SUPERVISOR);
                log.info("默认主管用户已创建: supervisor");
            }

            log.info("默认数据初始化完成！");
        } catch (Exception e) {
            log.error("初始化默认数据失败: {}", e.getMessage(), e);
        }
    }
}
