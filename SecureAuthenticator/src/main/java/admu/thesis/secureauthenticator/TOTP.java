package admu.thesis.secureauthenticator;


import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Ian on 7/22/13.
 */
public class TOTP {
    private static final int NUM_OTP = 1000000;
    private static final String HMAC_SHA1 = "HmacSHA1";


    public TOTP() {}

    public static byte[] hmacSha(byte[] keyBytes, byte[] text) {
        try {
            Mac hmac;
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes,HMAC_SHA1);
            hmac = Mac.getInstance(HMAC_SHA1);
            hmac.init(keySpec);
            return hmac.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int generateTOTP(byte[] key, long time) {
        byte[] msg = ByteBuffer.allocate(8).putLong(time).array();
        byte[] hash = hmacSha(key, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        int otp = binary % NUM_OTP;

        return otp;
    }
}
