package com.capgemini.test.code.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "smsClient",
        url = "${external.service.url}"
)
public interface SmsClient {

    @PostMapping("/sms")
    void send(@RequestBody SmsRequest request);

}