package com.capgemini.test.code.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    private Long roomId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String dni;

}