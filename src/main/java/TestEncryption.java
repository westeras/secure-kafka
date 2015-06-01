import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by adam on 5/21/15.
 */
public class TestEncryption {

    public static void main(String[] args) throws UnsupportedEncodingException, IllegalBlockSizeException {
        File publicKey = new File("src/main/resources/publicKey.txt");
        File privateKey = new File("src/main/resources/privateKey.txt");

        if (!publicKey.exists() || !privateKey.exists()) {
            RSAEncryptionUtility.generateKeyPair("src/main/resources/publicKey.txt", "src/main/resources/privateKey.txt");
        }

        // Message to encrypt
        String message = "hello, world";

        // Create AES key to encrypt text
        SecretKey aesSecretKey = AESEncryptionUtility.generateAESKey(256);
        String aesSecretKeyString = Base64.encodeBase64String(aesSecretKey.getEncoded());
        System.out.println(aesSecretKeyString);
        // Encrypt message using AES
        byte[] encryptedMessage = AESEncryptionUtility.encrypt(aesSecretKey, message.getBytes("UTF-8"));

        // Encrypt AES key using RSA public key
        byte[] encryptedAESKey = RSAEncryptionUtility.encrypt("src/main/resources/publicKey.txt", aesSecretKey.getEncoded());

        // At this point data would be sent over the insecure network

        // Decrypt AES key using RSA private key and create Java SecretKey from result
        byte[] decryptedAESKey = RSAEncryptionUtility.decrypt("src/main/resources/privateKey.txt", encryptedAESKey);
        byte[] decryptedAESKeyBytes = Base64.decodeBase64(decryptedAESKey);
        SecretKey secretKey = new SecretKeySpec(decryptedAESKeyBytes, 0, decryptedAESKeyBytes.length, "AES");

        // Decrypt message using decrypted AES key
        byte[] decryptedMessage = AESEncryptionUtility.decrypt(secretKey, encryptedMessage);

        System.out.println("Decrypted message: " + decryptedMessage);
    }

}
