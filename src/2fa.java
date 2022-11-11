import java.time.Instant;

class C2FA
{
 	public static void main(String[] args)
	{
		String shared_secret = "JBSWY3DPEHPK3PXP";
		int interval = 30;
		long moving_factor = Instant.now().getEpochSecond() / interval;

		byte[] prepped_shared_secret = input_prep.byte_secret(shared_secret);
		byte[] prepped_moving_factor = input_prep.byte_moving_factor(moving_factor);

		hmac.hmac_sha1(prepped_shared_secret, prepped_moving_factor);	
	}
}
