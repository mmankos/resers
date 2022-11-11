import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class hmac {
    public static byte[] hmac_sha1(byte[] secret_key, byte[] moving_factor) {
        try {
            Mac hmac_sha_1 = Mac.getInstance("HmacSHA1");
            
            SecretKeySpec secret = new SecretKeySpec(secret_key, "HmacSHA1");
            hmac_sha_1.init(secret);
            
            byte[] digest = hmac_sha_1.doFinal(moving_factor);
            totp.TOTP(digest);
            
            return digest;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
