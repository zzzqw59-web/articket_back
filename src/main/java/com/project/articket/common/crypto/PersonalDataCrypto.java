package com.project.articket.common.crypto;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.DeterministicAead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.RegistryConfiguration;
import com.google.crypto.tink.TinkJsonProtoKeysetFormat;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.daead.DeterministicAeadConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class PersonalDataCrypto {

    private static final byte[] NAME_ASSOCIATED_DATA =
            "MEMBER_NAME".getBytes(StandardCharsets.UTF_8);

    private static final byte[] PHONE_ASSOCIATED_DATA =
            "MEMBER_PHONE".getBytes(StandardCharsets.UTF_8);

    private final Aead nameAead;
    private final DeterministicAead phoneDeterministicAead;

    public PersonalDataCrypto(
            @Value("${tink.name-keyset}") String nameKeysetBase64,
            @Value("${tink.phone-keyset}") String phoneKeysetBase64
    ) throws Exception {

        AeadConfig.register();
        DeterministicAeadConfig.register();

        String nameKeysetJson =
                new String(
                        Base64.getDecoder().decode(nameKeysetBase64),
                        StandardCharsets.UTF_8
                );

        String phoneKeysetJson =
                new String(
                        Base64.getDecoder().decode(phoneKeysetBase64),
                        StandardCharsets.UTF_8
                );

        KeysetHandle nameKeysetHandle =
                TinkJsonProtoKeysetFormat.parseKeyset(
                        nameKeysetJson,
                        InsecureSecretKeyAccess.get()
                );

        KeysetHandle phoneKeysetHandle =
                TinkJsonProtoKeysetFormat.parseKeyset(
                        phoneKeysetJson,
                        InsecureSecretKeyAccess.get()
                );

        this.nameAead =
                nameKeysetHandle.getPrimitive(
                        RegistryConfiguration.get(),
                        Aead.class
                );

        this.phoneDeterministicAead =
                phoneKeysetHandle.getPrimitive(
                        RegistryConfiguration.get(),
                        DeterministicAead.class
                );
    }

    public String encryptName(String name) {

        try {
            byte[] encrypted =
                    nameAead.encrypt(
                            name.getBytes(StandardCharsets.UTF_8),
                            NAME_ASSOCIATED_DATA
                    );

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "이름 암호화 중 오류가 발생했습니다.",
                    exception
            );
        }
    }

    public String decryptName(String encryptedName) {

        try {
            byte[] encrypted =
                    Base64.getDecoder().decode(encryptedName);

            byte[] decrypted =
                    nameAead.decrypt(
                            encrypted,
                            NAME_ASSOCIATED_DATA
                    );

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception exception) {
            throw new RuntimeException(
                    "이름 복호화 중 오류가 발생했습니다.",
                    exception
            );
        }
    }

    public String encryptPhone(String phone) {

        try {
            // 실제 휴대폰 번호는 구분기호 없이 암호화하여 중복 조회 기준을 통일한다.
            // 회원탈퇴 익명화 값(withdrawn_...)은 휴대폰 번호가 아니므로 유지한다.
            String normalizedPhone = phone.replaceAll("\\D", "");
            String valueToEncrypt = !phone.startsWith("withdrawn_")
                    && normalizedPhone.matches("01\\d{8,9}")
                    ? normalizedPhone
                    : phone;

            byte[] encrypted =
                    phoneDeterministicAead.encryptDeterministically(
                            valueToEncrypt.getBytes(StandardCharsets.UTF_8),
                            PHONE_ASSOCIATED_DATA
                    );

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "전화번호 암호화 중 오류가 발생했습니다.",
                    exception
            );
        }
    }

    public String decryptPhone(String encryptedPhone) {

        try {
            byte[] encrypted =
                    Base64.getDecoder().decode(encryptedPhone);

            byte[] decrypted =
                    phoneDeterministicAead.decryptDeterministically(
                            encrypted,
                            PHONE_ASSOCIATED_DATA
                    );

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception exception) {
            throw new RuntimeException(
                    "전화번호 복호화 중 오류가 발생했습니다.",
                    exception
            );
        }
    }
}