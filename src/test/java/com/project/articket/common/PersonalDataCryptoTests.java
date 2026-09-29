package com.project.articket.common;

import com.project.articket.common.crypto.PersonalDataCrypto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonalDataCryptoTests {

    @Autowired
    private PersonalDataCrypto personalDataCrypto;

    @Test
    void nameEncryptDecryptTest() {

        String originalName = "김태현";

        String encryptedName =
                personalDataCrypto.encryptName(originalName);

        String decryptedName =
                personalDataCrypto.decryptName(encryptedName);

        System.out.println("원본 이름: " + originalName);
        System.out.println("암호화된 이름: " + encryptedName);
        System.out.println("복호화된 이름: " + decryptedName);

        assertNotEquals(originalName, encryptedName);
        assertEquals(originalName, decryptedName);
    }

    @Test
    void nameRandomEncryptionTest() {

        String name = "김태현";

        String firstEncryptedName =
                personalDataCrypto.encryptName(name);

        String secondEncryptedName =
                personalDataCrypto.encryptName(name);

        System.out.println("첫 번째 이름 암호문: " + firstEncryptedName);
        System.out.println("두 번째 이름 암호문: " + secondEncryptedName);

        assertNotEquals(
                firstEncryptedName,
                secondEncryptedName
        );
    }

    @Test
    void phoneEncryptDecryptTest() {

        String originalPhone = "01012345678";

        String encryptedPhone =
                personalDataCrypto.encryptPhone(originalPhone);

        String decryptedPhone =
                personalDataCrypto.decryptPhone(encryptedPhone);

        System.out.println("원본 전화번호: " + originalPhone);
        System.out.println("암호화된 전화번호: " + encryptedPhone);
        System.out.println("복호화된 전화번호: " + decryptedPhone);

        assertNotEquals(originalPhone, encryptedPhone);
        assertEquals(originalPhone, decryptedPhone);
    }

    @Test
    void phoneDeterministicEncryptionTest() {

        String phone = "01012345678";

        String firstEncryptedPhone =
                personalDataCrypto.encryptPhone(phone);

        String secondEncryptedPhone =
                personalDataCrypto.encryptPhone(phone);

        System.out.println(
                "첫 번째 전화번호 암호문: "
                        + firstEncryptedPhone
        );

        System.out.println(
                "두 번째 전화번호 암호문: "
                        + secondEncryptedPhone
        );

        assertEquals(
                firstEncryptedPhone,
                secondEncryptedPhone
        );
    }

    @Test
    void encryptedLengthTest() {

        String name = "김태현";
        String phone = "01012345678";

        String encryptedName =
                personalDataCrypto.encryptName(name);

        String encryptedPhone =
                personalDataCrypto.encryptPhone(phone);

        System.out.println("원본 이름: " + name);
        System.out.println("이름 원본 길이: " + name.length());
        System.out.println("암호화된 이름: " + encryptedName);
        System.out.println("암호화된 이름 길이: " + encryptedName.length());

        System.out.println();

        System.out.println("원본 전화번호: " + phone);
        System.out.println("전화번호 원본 길이: " + phone.length());
        System.out.println("암호화된 전화번호: " + encryptedPhone);
        System.out.println("암호화된 전화번호 길이: " + encryptedPhone.length());
    }
}