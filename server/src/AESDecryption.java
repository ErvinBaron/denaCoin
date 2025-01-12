import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class AESDecryption {



    private static final String ENCRYPTION_KEY = "1234567890123456"; // Fixed key (same as sent to the client)

    public static String decrypt(String encryptedData) throws Exception {
        byte[] keyBytes = ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]); // Fixed IV (same as client)

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] originalBytes = cipher.doFinal(encryptedBytes);

        return new String(originalBytes, StandardCharsets.UTF_8);
    }


}


