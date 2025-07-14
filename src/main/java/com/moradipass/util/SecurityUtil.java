package com.moradipass.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // For master password hashing

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec; // For PBKDF2
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SecurityUtil {

    // For password hashing (master password)
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // --- Master Password Hashing (BCrypt remains) ---
    public static String hashMasterPassword(String masterPassword) {
        return passwordEncoder.encode(masterPassword);
    }

    public static boolean checkMasterPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    // --- AES Encryption for Password Entries ---
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // CBC mode with PKCS5 padding

    // Key Derivation Function (KDF) parameters
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536; // Number of iterations for PBKDF2 (should be high)
    private static final int KEY_LENGTH = 128; // AES key length in bits (128, 192, or 256)

    // Generates a new random salt for PBKDF2 (for master password key derivation)
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 bytes for salt is common
        random.nextBytes(salt);
        return salt;
    }

    // Generates a new random 16-byte IV for each AES encryption of a PasswordEntry
    public static byte[] generateIv() {
        byte[] iv = new byte[16]; // AES requires 16-byte IV for CBC mode
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // Derives a strong AES SecretKey from a master password and salt using PBKDF2
    public static SecretKey deriveKeyFromMasterPassword(char[] masterPassword, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        KeySpec spec = new PBEKeySpec(masterPassword, salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    public static String encrypt(String plainText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, "UTF-8");
    }
}