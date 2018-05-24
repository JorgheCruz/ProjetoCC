import java.util.Formatter;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Cypher {

	private static String secretKey = "cc1718";

    	public String encrypt(String message) {

        	String hash = null;

        	try {
            		SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            
			sha256_HMAC.init(keySpec);
			byte[] encodedBytes = Base64.getEncoder().encode(sha256_HMAC.doFinal(message.getBytes()));
			hash = new String(encodedBytes);
		} 
		catch (NoSuchAlgorithmException exception) {
            		System.out.println("The chosen algorithm does not exist\n");
        	} 
		catch (InvalidKeyException exception) {
            		System.out.println("The key provided is not valid!\n");
        	}

        	return hash;
    	}

	public String checkMessage(String message) {
		
		String finalMessage = null;
		String msg[] = message.split("\n");

		if ((encrypt(msg[0])).equals(msg[1]))
			finalMessage = msg[0];

		return finalMessage;
	}
}

