import org.junit.Test;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by adam on 5/28/15.
 */
public class TestEncryptionUtilities {

    @Test
    public void testRSAEncryption() {
        String message = "hello, world";

        String encryptedMessage = RSAEncryptionUtility.encrypt("src/main/resources/publicKey.txt", message);
        String decryptedMessage = RSAEncryptionUtility.decrypt("src/main/resources/privateKey.txt", encryptedMessage);

        assertNotEquals("hello, world", encryptedMessage);
        assertEquals("hello, world", decryptedMessage);
    }

    @Test
    public void testAESEncryption() {
        String message = "hello, world";

        SecretKey key = AESEncryptionUtility.generateAESKey(256);
        String encryptedMessage = AESEncryptionUtility.encrypt(key, message);
        String decryptedMessage = AESEncryptionUtility.decrypt(key, encryptedMessage);

        assertNotEquals("hello, world", encryptedMessage);
        assertEquals("hello, world", decryptedMessage);
    }
}
