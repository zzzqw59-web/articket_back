package com.project.articket.verification.service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SolapiService {

    private final DefaultMessageService messageService;
    private final String sender;

    public SolapiService(
            @Value("${solapi.api-key}") String apiKey,
            @Value("${solapi.api-secret}") String apiSecret,
            @Value("${solapi.sender}") String sender
    ) {
        this.messageService =
                SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);

        this.sender = sender;
    }

    public void sendVerificationCode(
            String phoneNumber,
            String verificationCode
    ) {

        Message message = new Message();

        message.setFrom(sender);
        message.setTo(phoneNumber);
        message.setText(
                "[ARTICKET] 인증번호는 "
                        + verificationCode
                        + "입니다."
        );

        try {
            messageService.send(message);

        } catch (SolapiMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());

            throw new RuntimeException(
                    "인증번호 문자 발송에 실패했습니다.",
                    exception
            );

        } catch (Exception exception) {
            System.out.println(exception.getMessage());

            throw new RuntimeException(
                    "SOLAPI 문자 발송 중 오류가 발생했습니다.",
                    exception
            );
        }
    }
}