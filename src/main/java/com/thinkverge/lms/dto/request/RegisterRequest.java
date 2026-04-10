package com.thinkverge.lms.dto.request;

import com.thinkverge.lms.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private Role role;

}