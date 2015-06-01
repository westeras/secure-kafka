import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by adam on 5/27/15.
 */
public class RSAEncryptionUtility {

    private static final int RSA_KEY_SIZE = 2048;

    public static void generateKeyPair(String publicKeyPath, String privateKeyPath) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

            keyPairGenerator.initialize(RSA_KEY_SIZE, random);
            KeyPair keyPair = keyPairGenerator.genKeyPair();

            savePublicKey(keyPair.getPublic(), publicKeyPath);
            savePrivateKey(keyPair.getPrivate(), privateKeyPath);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    private static void savePublicKey(PublicKey key, String filePath) {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key.getEncoded());
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(spec.getEncoded());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void savePrivateKey(PrivateKey key, String filePath) {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key.getEncoded());
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(spec.getEncoded());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String keyPath, byte[] text) throws UnsupportedEncodingException {
        Cipher cipher;
        byte[] encryptedText = null;

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey(keyPath));

            encryptedText = cipher.doFinal(text);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        //String encryptedTextString = new String(encryptedText, "UTF-8"); //Base64.encodeBase64String(encryptedText);
        return encryptedText;
    }

    public static byte[] decrypt(String keyPath, byte[] text) throws UnsupportedEncodingException, IllegalBlockSizeException {
        Cipher cipher;
        byte[] decryptedText = null;

        //byte[] encryptedText = text.getBytes("UTF-8"); //Base64.decodeBase64(text);

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, loadPrivateKey(keyPath));

            decryptedText = cipher.doFinal(text);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //return new String(decryptedText, "UTF-8");
        return decryptedText;
    }

    private static PrivateKey loadPrivateKey(String keyPath) {
        File pkFile = new File(keyPath);
        PrivateKey pk = null;

        try {
            FileInputStream fis = new FileInputStream(pkFile);
            byte[] encodedPK = new byte[fis.available()];
            fis.read(encodedPK);
            fis.close();

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encodedPK);
            pk = keyFactory.generatePrivate(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return pk;
    }

    private static PublicKey loadPublicKey(String keyPath) {
        File pkFile = new File(keyPath);
        PublicKey pk = null;
        try {
            FileInputStream fis = new FileInputStream(pkFile);
            byte[] encodedPK = new byte[fis.available()];
            fis.read(encodedPK);
            fis.close();

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(encodedPK);
            pk = keyFactory.generatePublic(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return pk;
    }
}
