package io.datasheild.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;  // in seconds
    private String tokenType;  // "Bearer"
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private String userId;
        private String email;
        private String username;
        private String firstName;
        private String lastName;
        private java.util.Set<String> roles;
    }
}
