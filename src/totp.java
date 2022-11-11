public class totp
{
    public static void TOTP(byte[] hmac_hash) {
        int offset = hmac_hash[hmac_hash.length - 1] & 0xf;
		    int ten_to_the_power_of_6 = 1000000;
        int code = ((hmac_hash[offset]    & 0x7f) << 24 |
                	 (hmac_hash[offset + 1] & 0xff) << 16 |
                	 (hmac_hash[offset + 2] & 0xff) << 8 |
                	 (hmac_hash[offset + 3] & 0xff));

        String totp = String.valueOf((int) (code % ten_to_the_power_of_6));
        while (totp.length() < 6) {
            totp = '0' + totp;
        }

        System.out.println();
        System.out.println();
        System.out.printf("Current TOTP: ");
        System.out.println(totp);
        System.out.println();
    }
}
