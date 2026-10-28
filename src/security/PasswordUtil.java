package security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String FORMAT = "pbkdf2-sha256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final int MAX_ACCEPTED_ITERATIONS = 2_000_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(char[] password)
            throws GeneralSecurityException {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derivedKey = derive(password, salt, ITERATIONS);

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return FORMAT + "$" + ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$"
                + encoder.encodeToString(derivedKey);
    }

    public static boolean verify(char[] password, String encodedHash)
            throws GeneralSecurityException {
        if (password == null || encodedHash == null) {
            return false;
        }

        String[] parts = encodedHash.split("\\$", -1);
        if (parts.length != 4 || !FORMAT.equals(parts[0])) {
            return false;
        }

        final int iterations;
        final byte[] salt;
        final byte[] expectedKey;
        try {
            iterations = Integer.parseInt(parts[1]);
            salt = Base64.getUrlDecoder().decode(parts[2]);
            expectedKey = Base64.getUrlDecoder().decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (iterations < 1 || iterations > MAX_ACCEPTED_ITERATIONS
                || salt.length != SALT_BYTES
                || expectedKey.length != KEY_BITS / 8) {
            return false;
        }

        byte[] actualKey = derive(password, salt, iterations);
        return MessageDigest.isEqual(expectedKey, actualKey);
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM)
                    .generateSecret(spec)
                    .getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
