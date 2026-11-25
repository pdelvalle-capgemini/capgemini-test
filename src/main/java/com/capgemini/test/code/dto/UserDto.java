package com.capgemini.test.code.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class UserDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String dni;

}