import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.Base64;

/**
 * Created by adam on 5/21/15.
 */
public class TestEncryption {

    public static void main(String[] args) {
        File publicKey = new File("src/main/resources/publicKey.txt");
        File privateKey = new File("src/main/resources/privateKey.txt");

        if (!publicKey.exists() || !privateKey.exists()) {
            RSAEncryptionUtility.generateKeyPair("src/main/resources/publicKey.txt", "src/main/resources/privateKey.txt");
        }

        String message = "hello, world";
        SecretKey aesSecretKey = AESEncryptionUtility.generateAESKey(256);

        String encryptedMessage = AESEncryptionUtility.encrypt(aesSecretKey, message);

        byte[] encryptedAESKey = RSAEncryptionUtility.encrypt("src/main/resources/publicKey.txt", aesSecretKey.getEncoded());

        byte[] decryptedAESKey = RSAEncryptionUtility.decrypt("src/main/resources/privateKey.txt", encryptedAESKey);
        SecretKey secretKey = new SecretKeySpec(decryptedAESKey, 0, decryptedAESKey.length, "AES");

        String decryptedMessage = AESEncryptionUtility.decrypt(secretKey, encryptedMessage);

        System.out.println("Decrypted message: " + decryptedMessage);
    }

}
