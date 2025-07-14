package com.moradipass.util;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordGenerator {

    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:'\",.<>/?`~";

    /**
     * Generates a random password based on specified criteria.
     *
     * @param length The desired length of the password.
     * @param includeLowercase Whether to include lowercase characters.
     * @param includeUppercase Whether to include uppercase characters.
     * @param includeDigits Whether to include digits.
     * @param includeSpecial Whether to include special characters.
     * @return A randomly generated password.
     */
    public static String generateRandomPassword(int length,
                                                boolean includeLowercase,
                                                boolean includeUppercase,
                                                boolean includeDigits,
                                                boolean includeSpecial) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be greater than 0.");
        }

        StringBuilder charPool = new StringBuilder();
        if (includeLowercase) charPool.append(LOWERCASE_CHARS);
        if (includeUppercase) charPool.append(UPPERCASE_CHARS);
        if (includeDigits) charPool.append(DIGITS);
        if (includeSpecial) charPool.append(SPECIAL_CHARS);

        if (charPool.length() == 0) {
            throw new IllegalArgumentException("At least one character type must be selected.");
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // Ensure at least one character from each selected type is included (if possible within length)
        if (includeLowercase && password.length() < length) {
            password.append(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        }
        if (includeUppercase && password.length() < length) {
            password.append(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        }
        if (includeDigits && password.length() < length) {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        if (includeSpecial && password.length() < length) {
            password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        }

        // Fill the rest of the password length from the combined character pool
        for (int i = password.length(); i < length; i++) {
            password.append(charPool.charAt(random.nextInt(charPool.length())));
        }

        // Shuffle the characters to ensure randomness in character placement
        List<Character> pwdChars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars, random);

        return pwdChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}