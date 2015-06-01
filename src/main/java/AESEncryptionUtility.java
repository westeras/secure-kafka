import javax.crypto.*;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by adam on 5/27/15.
 */
public class AESEncryptionUtility {

    public static SecretKey generateAESKey(int keyLengthBits) {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        keyGenerator.init(keyLengthBits);

        return keyGenerator.generateKey();
    }

    public static byte[] encrypt(SecretKey key, byte[] message) throws UnsupportedEncodingException {
        Cipher cipher;
        byte[] encryptedText = null;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            encryptedText = cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        String encryptedTextString = new String(encryptedText, "UTF-8"); //Base64.encodeBase64String(encryptedText);
        return encryptedText;
    }

    public static byte[] decrypt(SecretKey key, byte[] message) throws UnsupportedEncodingException {
        Cipher cipher;
        byte[] decryptedText = null;

        //byte[] encryptedText = text.getBytes("UTF-8"); //Base64.decodeBase64(text);

        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(message);
        } catch (InvalidKeyException | BadPaddingException | NoSuchAlgorithmException
                | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return decryptedText;
    }
}
