package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.RoleType;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.exception.UnauthorizedException;
import com.aps.service.repository.RoleRepository;
import com.aps.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     */
    @Transactional
    @Audited(action = AuditAction.USER_CREATE, resource = "User")
    public User createUser(String username, String password, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new ResourceConflictException("用户名已存在: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    @Audited(action = AuditAction.USER_UPDATE, resource = "User")
    public User updateUser(UUID userId, String email, Boolean enabled) {
        User user = getUserById(userId);

        if (email != null) {
            user.setEmail(email);
        }
        if (enabled != null) {
            user.setEnabled(enabled);
        }

        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    @Audited(action = AuditAction.USER_DELETE, resource = "User")
    public void deleteUser(UUID userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("用户已删除: {}", user.getUsername());
    }

    /**
     * 根据ID获取用户
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));
    }

    /**
     * 根据用户名获取用户
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        List<UUID> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .toList();

        if (roleIds.isEmpty()) {
            return user;
        }

        Map<UUID, Role> rolesWithPermissions = roleRepository.findAllWithPermissionsByIdIn(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        List<Role> resolvedRoles = user.getRoles().stream()
                .map(role -> rolesWithPermissions.getOrDefault(role.getId(), role))
                .toList();

        user.setRoles(resolvedRoles);
        return user;
    }

    /**
     * 获取所有用户（分页）
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 获取所有用户（不分页，仅用于小数据集）
     * @deprecated 使用 getAllUsers(Pageable) 代替
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 为用户分配角色
     */
    @Transactional
    @Audited(action = AuditAction.ROLE_ASSIGN, resource = "User")
    public void assignRole(UUID userId, RoleType roleType) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleType.name())
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在: " + roleType));

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
            log.info("为用户 {} 分配角色: {}", user.getUsername(), roleType);
        }
    }

    /**
     * 移除用户角色
     */
    @Transactional
    @Audited(action = AuditAction.ROLE_REMOVE, resource = "User")
    public void removeRole(UUID userId, RoleType roleType) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleType.name())
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在: " + roleType));

        user.getRoles().remove(role);
        userRepository.save(user);
        log.info("移除用户 {} 的角色: {}", user.getUsername(), roleType);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("原密码不正确");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("用户 {} 已修改密码", user.getUsername());
    }

    /**
     * 更新最后登录时间
     */
    @Transactional
    public void updateLastLoginTime(UUID userId) {
        User user = getUserById(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
