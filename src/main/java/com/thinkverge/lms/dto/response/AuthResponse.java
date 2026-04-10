package com.thinkverge.lms.dto.response;

import com.thinkverge.lms.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private String name;
    private Role role;

}