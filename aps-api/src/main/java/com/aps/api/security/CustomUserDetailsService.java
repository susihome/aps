package com.aps.api.security;

import com.aps.domain.entity.Permission;
import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;
import com.aps.service.repository.RoleRepository;
import com.aps.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用优化的查询，一次性加载用户和角色
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        List<UUID> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .toList();

        Map<UUID, Role> rolesWithPermissions = roleRepository.findAllWithPermissionsByIdIn(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        List<Role> resolvedRoles = user.getRoles().stream()
                .map(role -> rolesWithPermissions.getOrDefault(role.getId(), role))
                .toList();

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getEnabled(),
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                getAuthorities(resolvedRoles)
        );
    }

    /**
     * 获取用户的权限列表（角色 + 权限）
     */
    private List<GrantedAuthority> getAuthorities(List<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加角色（以 ROLE_ 前缀）
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // 添加角色对应的权限
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
        }

        return authorities;
    }
}
