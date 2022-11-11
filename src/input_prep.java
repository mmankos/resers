import java.io.ByteArrayOutputStream;

public class input_prep 
{
    public static byte[] byte_secret(String secret_key) 
    {
        secret_key += "=".repeat(secret_key.length() % 8);
        byte[] decoded_secret = base32.decode(secret_key);
        return decoded_secret;
    }

    public static byte[] reverse(byte[] array) 
    {
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            
            j--;
            i++;
        }
        return array;
    }

    public static byte[] byte_moving_factor(long moving_factor) 
    {
        ByteArrayOutputStream prepped_moving_factor = new ByteArrayOutputStream();
        for (int i = 0; i < 8; i++) {
            while (moving_factor != 0) {
                prepped_moving_factor.write((int) moving_factor & 0xFF);
                moving_factor >>= 8;
                i++;
            }
            prepped_moving_factor.write(0);
        }
        byte[] byte_prepped_moving_factor = prepped_moving_factor.toByteArray();
        reverse(byte_prepped_moving_factor);
        return byte_prepped_moving_factor;
    }
}
