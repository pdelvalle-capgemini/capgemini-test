package com.capgemini.test.code.controller;

import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.dto.UserDto;
import com.capgemini.test.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody CreateUserRequest request) {

        Long userId = userService.createUser(request);

        return UserDto.builder()
                .id(userId)
                .build();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {

        return userService.getUser(id);
    }
}