package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.exception.UnauthorizedException;
import com.aps.service.repository.RoleRepository;
import com.aps.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final AuthSessionService authSessionService;

    /**
     * 创建用户
     */
    @Transactional
    @Audited(action = AuditAction.USER_CREATE, resource = "User")
    public User createUser(String username, String password, String email, List<UUID> roleIds) {
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalizeEmail(email);

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ResourceConflictException("用户名已存在: " + normalizedUsername);
        }

        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceConflictException("邮箱已存在: " + normalizedEmail);
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(normalizedEmail);
        user.setEnabled(true);
        user.setRoles(resolveRoles(roleIds));

        User saved = userRepository.save(user);
        return getUserById(saved.getId());
    }

    /**
     * 更新用户信息
     */
    @Transactional
    @Audited(action = AuditAction.USER_UPDATE, resource = "User")
    public User updateUser(UUID userId, String email, Boolean enabled, List<UUID> roleIds) {
        User user = getUserById(userId);
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail != null && userRepository.existsByEmailAndIdNot(normalizedEmail, userId)) {
            throw new ResourceConflictException("邮箱已存在: " + normalizedEmail);
        }

        if (email != null) {
            user.setEmail(normalizedEmail);
        }
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        if (roleIds != null) {
            user.setRoles(resolveRoles(roleIds));
        }

        userRepository.save(user);
        if (Boolean.FALSE.equals(enabled)) {
            authSessionService.revokeAllUserSessions(userId);
        }
        return getUserById(userId);
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
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));
        return hydrateUser(user);
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

        List<Role> resolvedRoles = new ArrayList<>(user.getRoles().stream()
                .map(role -> rolesWithPermissions.getOrDefault(role.getId(), role))
                .toList());

        user.setRoles(resolvedRoles);
        return user;
    }

    /**
     * 分页查询用户
     */
    @Transactional(readOnly = true)
    public Page<User> getUsers(String keyword, Boolean enabled, Pageable pageable) {
        Pageable resolvedPageable = ensureStableSort(pageable);
        Page<User> page = userRepository.search(normalizeKeyword(keyword), enabled, resolvedPageable);
        List<User> hydratedUsers = hydrateUsers(page.getContent());
        return page.map(user -> hydratedUsers.stream()
                .filter(item -> item.getId().equals(user.getId()))
                .findFirst()
                .orElse(user));
    }

    /**
     * 获取所有用户（不分页，仅用于小数据集）
     * @deprecated 使用 getAllUsers(Pageable) 代替
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return hydrateUsers(userRepository.findAll());
    }

    /**
     * 为用户分配角色
     */
    @Transactional
    @Audited(action = AuditAction.ROLE_ASSIGN, resource = "User")
    public User assignRoles(UUID userId, List<UUID> roleIds) {
        User user = getUserById(userId);
        user.setRoles(resolveRoles(roleIds));
        userRepository.save(user);
        log.info("为用户 {} 分配角色数量: {}", user.getUsername(), user.getRoles().size());
        return getUserById(userId);
    }

    /**
     * 为用户分配单个角色
     */
    @Transactional
    @Audited(action = AuditAction.ROLE_ASSIGN, resource = "User")
    public void assignRole(UUID userId, com.aps.domain.enums.RoleType roleType) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleType.name())
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在: " + roleType));

        List<Role> roles = new ArrayList<>(user.getRoles());
        boolean exists = roles.stream().anyMatch(item -> item.getId().equals(role.getId()));
        if (!exists) {
            roles.add(role);
            user.setRoles(roles);
            userRepository.save(user);
            log.info("为用户 {} 分配角色: {}", user.getUsername(), roleType);
        }
    }

    /**
     * 移除用户角色
     */
    @Transactional
    @Audited(action = AuditAction.ROLE_REMOVE, resource = "User")
    public void removeRole(UUID userId, com.aps.domain.enums.RoleType roleType) {
        User user = getUserById(userId);
        List<Role> roles = new ArrayList<>(user.getRoles());
        roles.removeIf(role -> roleType.name().equals(role.getName()));
        user.setRoles(roles);
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
     * 管理员重置密码
     */
    @Transactional
    @Audited(action = AuditAction.USER_UPDATE, resource = "User")
    public void resetPassword(UUID userId, String newPassword) {
        User user = getUserById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        authSessionService.revokeAllUserSessions(userId);
        log.info("管理员已重置用户 {} 的密码", user.getUsername());
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

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        String normalized = normalize(email);
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        return normalized.toLowerCase();
    }

    private String normalizeKeyword(String keyword) {
        String normalized = normalize(keyword);
        if (normalized == null || normalized.isEmpty()) {
            return "";
        }
        return normalized;
    }

    private Pageable ensureStableSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id"))
        );
    }

    private List<Role> resolveRoles(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new ResourceNotFoundException("部分角色不存在");
        }
        return new ArrayList<>(roles);
    }

    private List<User> hydrateUsers(List<User> users) {
        if (users.isEmpty()) {
            return List.of();
        }

        Map<UUID, User> userMap = userRepository.findAllWithRolesByIdIn(users.stream()
                        .map(User::getId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<UUID> roleIds = userMap.values().stream()
                .flatMap(user -> user.getRoles().stream())
                .map(Role::getId)
                .distinct()
                .toList();

        Map<UUID, Role> rolesWithPermissions = roleIds.isEmpty()
                ? Map.of()
                : roleRepository.findAllWithPermissionsByIdIn(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        return users.stream()
                .map(user -> hydrateUser(userMap.getOrDefault(user.getId(), user), rolesWithPermissions))
                .toList();
    }

    private User hydrateUser(User user) {
        List<UUID> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .toList();

        Map<UUID, Role> rolesWithPermissions = roleIds.isEmpty()
                ? Map.of()
                : roleRepository.findAllWithPermissionsByIdIn(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));
        return hydrateUser(user, rolesWithPermissions);
    }

    private User hydrateUser(User user, Map<UUID, Role> rolesWithPermissions) {
        List<Role> resolvedRoles = new ArrayList<>(user.getRoles().stream()
                .map(role -> rolesWithPermissions.getOrDefault(role.getId(), role))
                .toList());
        user.setRoles(resolvedRoles);
        return user;
    }
}
