import org.junit.Test;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by adam on 5/28/15.
 */
public class TestEncryptionUtilities {

    @Test
    public void testRSAEncryption() throws UnsupportedEncodingException, IllegalBlockSizeException {
        String message = "hello, world";
        byte[] encodedMessage = message.getBytes("UTF-8");

        byte[] encryptedMessage = RSAEncryptionUtility.encrypt("src/main/resources/publicKey.txt", encodedMessage);
        byte[] decryptedMessage = RSAEncryptionUtility.decrypt("src/main/resources/privateKey.txt", encryptedMessage);

        assertNotEquals("hello, world", new String(encryptedMessage, "UTF-8"));
        assertEquals("hello, world", new String(decryptedMessage, "UTF-8"));
    }

    @Test
    public void testAESEncryption() throws UnsupportedEncodingException {
        String message = "hello, world";
        byte[] encodedMessage = message.getBytes("UTF-8");

        SecretKey key = AESEncryptionUtility.generateAESKey(256);
        byte[] encryptedMessage = AESEncryptionUtility.encrypt(key, encodedMessage);
        byte[] decryptedMessage = AESEncryptionUtility.decrypt(key, encryptedMessage);



        assertNotEquals("hello, world", new String(encryptedMessage, "UTF-8"));
        assertEquals("hello, world", new String(decryptedMessage, "UTF-8"));
    }
}
