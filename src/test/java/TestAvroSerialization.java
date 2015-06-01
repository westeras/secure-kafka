import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * Created by adam on 5/29/15.
 */
public class TestAvroSerialization {

    Schema schema;

    @Before
    public void setUp() {
        Schema.Parser parser = new Schema.Parser();
        this.schema = null;
        try {
            this.schema = parser.parse(getClass().getResourceAsStream("message.avsc"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSerializeDeserialize() throws UnsupportedEncodingException {
        SecretKey key = AESEncryptionUtility.generateAESKey(256);
        byte[] encryptedKey = RSAEncryptionUtility.encrypt("src/main/resources/publicKey.txt", key.getEncoded());

        String payload = "hello, world";
        byte[] encryptedPayload = AESEncryptionUtility.encrypt(key, payload.getBytes("UTF-8"));

        ByteBuffer secretKeyBB = ByteBuffer.wrap(encryptedKey);
        ByteBuffer payloadBB = ByteBuffer.wrap(encryptedPayload);

        GenericRecord recordBefore = new GenericData.Record(this.schema);
        recordBefore.put("secretKey", secretKeyBB);
        recordBefore.put("payload", payloadBB);

        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(this.schema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        try {
            writer.write(recordBefore, encoder);
            encoder.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String serialized = new String(out.toByteArray());

        DatumReader<GenericRecord> reader = new GenericDatumReader<>(this.schema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(out.toByteArray(), null);
        GenericRecord recordAfter = null;
        try {
            recordAfter = reader.read(null, decoder);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ByteBuffer secretKeyAfter = (ByteBuffer) recordAfter.get("secretKey");
        ByteBuffer payloadAfter = (ByteBuffer) recordAfter.get("payload");

        byte[] secretKeyAfterBytes = getBytesFromByteBuffer(secretKeyAfter);
        byte[] payloadAfterBytes = getBytesFromByteBuffer(payloadAfter);

        byte[] decryptedSecretKey = null;
        byte[] decryptedPayload = null;

        try {
            decryptedSecretKey = RSAEncryptionUtility.decrypt("src/main/resources/privateKey.txt", secretKeyAfterBytes);
            SecretKey secretKey = new SecretKeySpec(decryptedSecretKey, 0, decryptedSecretKey.length, "AES");

            decryptedPayload = AESEncryptionUtility.decrypt(secretKey, payloadAfterBytes);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        assertEquals(payload, new String(decryptedPayload, "UTF-8"));
    }

    private byte[] getBytesFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);

        return bytes;
    }
}
