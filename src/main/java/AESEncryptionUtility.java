import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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
        SecretKey secretKey = keyGenerator.generateKey();

        return secretKey;
    }

    public static String encrypt(SecretKey key, String text) {
        Cipher cipher;
        byte[] encryptedText = null;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            encryptedText = cipher.doFinal(text.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedTextString = encoder.encodeToString(encryptedText);
        return encryptedTextString;
    }

    public static String decrypt(SecretKey key, String text) {
        Cipher cipher;
        byte[] decryptedText = null;

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedText = decoder.decode(text);

        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = cipher.doFinal(encryptedText);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return new String(decryptedText);
    }
}
