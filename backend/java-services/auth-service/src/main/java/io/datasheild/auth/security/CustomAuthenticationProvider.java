package io.datasheild.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import io.datasheild.auth.entity.User;
import io.datasheild.auth.exception.UnauthorizedException;
import io.datasheild.auth.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = (String) authentication.getCredentials();
        UUID tenantId = (UUID) authentication.getDetails();

        User user = userRepository.findByTenantIdAndEmail(tenantId, email)
                .orElseThrow(() -> {
                    log.warn("User not found: {} in tenant: {}", email, tenantId);
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!user.getStatus().equals(User.UserStatus.ACTIVE)) {
            throw new UnauthorizedException("User account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Invalid password for user: {} in tenant: {}", email, tenantId);
            throw new UnauthorizedException("Invalid email or password");
        }

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken(user.getId(), password, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
