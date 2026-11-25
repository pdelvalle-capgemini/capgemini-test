package com.capgemini.test.code;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.CheckDniResponse;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.entity.Role;
import com.capgemini.test.code.entity.Room;
import com.capgemini.test.code.entity.User;
import com.capgemini.test.code.exception.UserAlreadyExistsException;
import com.capgemini.test.code.exception.ValidationException;
import com.capgemini.test.code.repository.RoomRepository;
import com.capgemini.test.code.repository.UserRepository;
import com.capgemini.test.code.service.NotificationService;
import com.capgemini.test.code.service.UserService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DniClient dniClient;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest request;
    private Room room;

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest();
        request.setRoomId(1L);
        request.setName("pablo");
        request.setEmail("pablo@email.com");
        request.setPhone("677998899");
        request.setRole("admin");
        request.setDni("23454234W");

        room = new Room();
        room.setId(1L);
        room.setName("Sala 1");
    }

    @Test
    void test_create_user_ok() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(dniClient.check(any(CheckDniRequest.class)))
                .thenReturn(ResponseEntity.ok(new CheckDniResponse()));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setRole(Role.ADMIN);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Long resultId = userService.createUser(request);

        assertEquals(1L, resultId);
    }

    @Test
    void test_email_duplicado() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(request));

        assertEquals("Ya existe ese usuario", ex.getMessage());
    }

    @Test
    void test_nombre_invalido() {
        request.setName("nombreMuyLargooo");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(request));

        assertEquals("El nombre no puede superar los 6 caracteres", ex.getMessage());
    }

    @Test
    void test_rol_invalido() {
        request.setRole("usuario");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.createUser(request));

        assertEquals("El rol debe ser admin o superadmin", ex.getMessage());
    }

    @Test
    void test_email_invalido() {
        request.setEmail("correo-invalido");

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userService.createUser(request)
        );

        assertEquals("El email no es valido", ex.getMessage());
    }

    @Test
    void test_dni_invalido() {
        request.setDni("99999999w");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(dniClient.check(any(CheckDniRequest.class)))
                .thenThrow(mock(FeignException.class));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userService.createUser(request)
        );

        assertEquals("DNI invalido", ex.getMessage());
    }

}
