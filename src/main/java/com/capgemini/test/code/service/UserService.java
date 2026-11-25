package com.capgemini.test.code.service;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.dto.UserDto;
import com.capgemini.test.code.entity.Role;
import com.capgemini.test.code.entity.Room;
import com.capgemini.test.code.entity.User;
import com.capgemini.test.code.exception.UserAlreadyExistsException;
import com.capgemini.test.code.exception.UserNotFoundException;
import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.repository.RoomRepository;
import com.capgemini.test.code.repository.UserRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final DniClient dniClient;
    private final NotificationService notificationService;

    public Long createUser(CreateUserRequest request) {

        validateRequest(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Ya existe ese usuario");
        }

        try {

            var response = dniClient.check(
                    new CheckDniRequest(request.getDni()));

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ValidationException("DNI invalido");
            }

        } catch (FeignException ex) {

            throw new ValidationException("DNI invalido");

        }

        Room room = roomRepository
                .findById(request.getRoomId())
                .orElseThrow(() ->
                        new ValidationException("Sala no encontrada"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDni(request.getDni());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setRoom(room);

        User savedUser = userRepository.save(user);

        notificationService.notifyUser(savedUser);

        return savedUser.getId();
    }

    public UserDto getUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("Usuario no encontrado"));

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dni(user.getDni())
                .role(user.getRole().name())
                .build();
    }

    private void validateRequest(CreateUserRequest request) {

        if (request.getName() == null || request.getName().length() > 6) {
            throw new ValidationException("El nombre no puede superar los 6 caracteres");
        }

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new ValidationException("El email no es valido");
        }

        try {
            Role.valueOf(request.getRole().toUpperCase());

        } catch (IllegalArgumentException ex) {
            throw new ValidationException("El rol debe ser admin o superadmin");
        }
    }
}