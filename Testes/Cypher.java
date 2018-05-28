import java.util.Formatter;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
/**
 Esta classe foi desenvolvida com o objetivo de verificar a integridade e autenticação de origem 
 das mensagens trocadas entre o Monitor e os vários Agentes com uma assinatura digital simétrica.
 Contém as funções de cifrar texto e de averiguar as mensagens recebidas.
 @author	Grupo 28
*/
public class Cypher {

	private static String secretKey = "cc1718";
	/**
	 Função que aplica o inicio do algoritmo HMAC á mensagem passada como argumento.
	 @param message mensagem que será encriptada
	 @return mensagem encriptada.
	 */
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

    /** 
     Função que recebe a mensagem enviada do exterior, 
     e verifica a integridade e autenticidade da mesma.
     @param message mensagem enviada do exterior 
     @return mensagem descodificada
    */
	public String checkMessage(String message) {
		
		String finalMessage = null;
		String msg[] = message.split("\n");

		if ((encrypt(msg[0])).equals(msg[1]))
			finalMessage = msg[0];

		return finalMessage;
	}
}

