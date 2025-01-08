import java.security.MessageDigest;

public class StringUtil {
    public static String applySHA256(String input) {
        try {
            // crearting the object of message digest that uses the Sha-256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Convert the input string to bytes and hash them using SHA-256 algorithm
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));

            //returns a string representation of the hashed bytes
            StringBuilder hexString = new StringBuilder();
            // displays a hash string of bytes in a hexidecimal 0-9 a-f format.
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
