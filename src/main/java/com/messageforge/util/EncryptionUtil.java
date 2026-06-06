package com.messageforge.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@Component
public class EncryptionUtil {

    private final String key;

    public EncryptionUtil(@Value("${app.encryption.key:your-encryption-key-32-chars-long!}") String key) {
        // Ensure key is exactly 32 bytes for AES-256
        if (key.length() < 32) {
            this.key = String.format("%-32s", key).substring(0, 32);
        } else {
            this.key = key.substring(0, 32);
        }
    }

    public byte[] encrypt(String data) {
        if (data == null) {
            return null;
        }
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = new byte[12]; // 12 bytes IV for GCM
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            return combined;
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during credential encryption", e);
        }
    }

    public String decrypt(byte[] encryptedData) {
        if (encryptedData == null || encryptedData.length <= 12) {
            return null;
        }
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = new byte[12];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);
            
            int encryptedSize = encryptedData.length - iv.length;
            byte[] encryptedBytes = new byte[encryptedSize];
            System.arraycopy(encryptedData, iv.length, encryptedBytes, 0, encryptedSize);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during credential decryption", e);
        }
    }
}
