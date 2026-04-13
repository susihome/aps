package com.aps.api.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * 自定义用户主体，携带 UUID
 * 用于 @PreAuthorize 表达式中访问 authentication.principal.id
 */
@Getter
public class UserPrincipal extends User {

    private final UUID id;

    public UserPrincipal(UUID id, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    public UserPrincipal(UUID id, String username, String password,
                         boolean enabled, boolean accountNonExpired,
                         boolean credentialsNonExpired, boolean accountNonLocked,
                         Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserPrincipal other)) {
            return false;
        }
        return super.equals(obj) && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
