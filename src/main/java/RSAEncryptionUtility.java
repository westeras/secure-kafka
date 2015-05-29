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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    private static void savePublicKey(PublicKey key, String filePath) {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key.getEncoded());
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(spec.getEncoded());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String keyPath, String text) {
        Cipher cipher;
        byte[] encryptedText = null;

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey(keyPath));

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
            System.exit(-1);
        }

        String encryptedTextString = Base64.encodeBase64String(encryptedText);
        return encryptedTextString;
    }

    public static String decrypt(String keyPath, String text) {
        Cipher cipher;
        byte[] decryptedText = null;

        byte[] encryptedText = Base64.decodeBase64(text);

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, loadPrivateKey(keyPath));

            decryptedText = cipher.doFinal(encryptedText);
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

        return new String(decryptedText);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return pk;
    }
}
