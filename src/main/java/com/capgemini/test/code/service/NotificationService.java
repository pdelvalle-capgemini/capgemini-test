package com.capgemini.test.code.service;

import com.capgemini.test.code.clients.EmailClient;
import com.capgemini.test.code.clients.EmailRequest;
import com.capgemini.test.code.clients.SmsClient;
import com.capgemini.test.code.clients.SmsRequest;
import com.capgemini.test.code.entity.Role;
import com.capgemini.test.code.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailClient emailClient;
    private final SmsClient smsClient;

    public void notifyUser(User user) {

        if (Role.ADMIN.equals(user.getRole())) {
            emailClient.send(
                    new EmailRequest(
                            user.getEmail(),
                            "usuario guardado"
                    )
            );
        } else if (Role.SUPERADMIN.equals(user.getRole())) {
            smsClient.send(
                    new SmsRequest(
                            user.getPhone(),
                            "usuario guardado"
                    )
            );
        }
    }
}