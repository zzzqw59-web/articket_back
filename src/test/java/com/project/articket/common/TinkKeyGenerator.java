//package com.project.articket.common;
//
//import com.google.crypto.tink.InsecureSecretKeyAccess;
//import com.google.crypto.tink.KeysetHandle;
//import com.google.crypto.tink.TinkJsonProtoKeysetFormat;
//import com.google.crypto.tink.aead.AeadConfig;
//import com.google.crypto.tink.aead.PredefinedAeadParameters;
//import com.google.crypto.tink.daead.DeterministicAeadConfig;
//import com.google.crypto.tink.daead.PredefinedDeterministicAeadParameters;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
//public class TinkKeyGenerator {
//
//    public static void main(String[] args) throws Exception {
//
//        AeadConfig.register();
//        DeterministicAeadConfig.register();
//
//        KeysetHandle nameKeyset =
//                KeysetHandle.generateNew(
//                        PredefinedAeadParameters.AES256_GCM
//                );
//
//        KeysetHandle phoneKeyset =
//                KeysetHandle.generateNew(
//                        PredefinedDeterministicAeadParameters.AES256_SIV
//                );
//
//        String nameKeysetJson =
//                TinkJsonProtoKeysetFormat.serializeKeyset(
//                        nameKeyset,
//                        InsecureSecretKeyAccess.get()
//                );
//
//        String phoneKeysetJson =
//                TinkJsonProtoKeysetFormat.serializeKeyset(
//                        phoneKeyset,
//                        InsecureSecretKeyAccess.get()
//                );
//
//        String nameKeysetBase64 =
//                Base64.getEncoder().encodeToString(
//                        nameKeysetJson.getBytes(StandardCharsets.UTF_8)
//                );
//
//        String phoneKeysetBase64 =
//                Base64.getEncoder().encodeToString(
//                        phoneKeysetJson.getBytes(StandardCharsets.UTF_8)
//                );
//
//        System.out.println("=== NAME KEYSET BASE64 ===");
//        System.out.println(nameKeysetBase64);
//
//        System.out.println();
//
//        System.out.println("=== PHONE KEYSET BASE64 ===");
//        System.out.println(phoneKeysetBase64);
//    }
//}