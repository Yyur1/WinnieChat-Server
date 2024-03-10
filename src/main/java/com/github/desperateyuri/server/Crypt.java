package com.github.desperateyuri.server;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypt {
    public static byte[] keyExchange(DataInputStream datainput, DataOutputStream dataoutput) throws InvalidKeyException,
            NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        SecureRandom secureRandom = new SecureRandom();  // Be careful with the consumption of the entropy poll
        keyPairGenerator.initialize(256, secureRandom);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        byte[] serverPublicKey = keyPair.getPublic().getEncoded();
        dataoutput.write(serverPublicKey);  // Exchange first
        byte[] exchange = datainput.readNBytes(91);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec x = new X509EncodedKeySpec(exchange);
        PublicKey clientPublicKey = keyFactory.generatePublic(x);
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(clientPublicKey, true);
        return keyAgreement.generateSecret();
    }
    public static String EncryptAES(String text, byte[] sessionKey) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = new SecretKeySpec(sessionKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] KEY_IV = "c558Gq0YQK2QUlMc".getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(KEY_IV));
        byte[] ciphertext = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(ciphertext);
    }
    public static String DecryptAES(String text, byte[] sessionKey) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = new SecretKeySpec(sessionKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] KEY_IV = "c558Gq0YQK2QUlMc".getBytes();
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(KEY_IV));
        byte[] temp = cipher.doFinal(Base64.getDecoder().decode(text));
        return new String(temp, StandardCharsets.UTF_8);
    }
}
