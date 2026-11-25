package com.capgemini.test.code;

import com.capgemini.test.code.clients.*;
import com.capgemini.test.code.dto.CreateUserRequest;
import com.capgemini.test.code.dto.UserDto;
import com.capgemini.test.code.entity.Room;
import com.capgemini.test.code.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @MockBean
    private DniClient dniClient;

    @MockBean
    private EmailClient emailClient;

    @MockBean
    private SmsClient smsClient;

    private Long roomId;

    @BeforeEach
    void setUp() {

        Room room = roomRepository.findById(1L)
                .orElseGet(() -> {
                    Room r = new Room();
                    r.setName("Sala 1");
                    return roomRepository.save(r);
                });

        roomId = room.getId();
    }

    @Test
    void test_create_get_user_ok() throws Exception {
        when(dniClient.check(any(CheckDniRequest.class)))
                .thenReturn(ResponseEntity.ok(new CheckDniResponse()));

        String random = String.valueOf((int) (Math.random() * 900000) + 100000);

        CreateUserRequest request = createRequest(
                "pablo" + random + "@email.com",
                "677" + random,
                random + "A",
                "admin"
        );

        String json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readValue(json, UserDto.class).getId();

        verify(emailClient).send(any(EmailRequest.class));

        mockMvc.perform(get("/users/" + createdId))
                .andExpect(status().isOk());
    }

    @Test
    void test_create_user_superadmin_sends_sms() throws Exception {
        when(dniClient.check(any(CheckDniRequest.class)))
                .thenReturn(ResponseEntity.ok(new CheckDniResponse()));

        //asegurar que el numero siempre tenga 6 dígitos
        String random = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );

        CreateUserRequest request =
                createRequest(
                        "super" + random + "@email.com",
                        "677" + random,
                        random + "A",
                        "SUPERADMIN"
                );

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(smsClient).send(any(SmsRequest.class));
    }

    @Test
    void test_user_not_found() throws Exception {
        mockMvc.perform(get("/users/99999"))
                .andExpect(status().isNotFound());
    }

    private CreateUserRequest createRequest(
            String email,
            String phone,
            String dni,
            String role) {

        CreateUserRequest request =
                new CreateUserRequest();

        request.setRoomId(roomId);
        request.setName("pablo");
        request.setEmail(email);
        request.setPhone(phone);
        request.setRole(role);
        request.setDni(dni);

        return request;
    }
}