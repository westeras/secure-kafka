import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

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
    public void testSerializeDeserialize() {
        SecretKey key = AESEncryptionUtility.generateAESKey(256);
        String keyString = Base64.encodeBase64String(key.getEncoded());
        String encryptedKey = RSAEncryptionUtility.encrypt("src/main/resources/publicKey.txt", keyString);

        String payload = "hello, world";
        String encryptedPayload = AESEncryptionUtility.encrypt(key, payload);

        GenericRecord recordBefore = new GenericData.Record(this.schema);
        recordBefore.put("secretKey", encryptedKey);
        recordBefore.put("payload", encryptedPayload);

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

        String serialized = new String(out.toByteArray());

        DatumReader<GenericRecord> reader = new GenericDatumReader<>(this.schema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(serialized.getBytes(), null);
        GenericRecord recordAfter = null;
        try {
            recordAfter = reader.read(null, decoder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(recordAfter.get("secretKey"));
        System.out.println(recordAfter.get("payload"));

        //assertEquals("this is a key", recordAfter.get("secretKey").toString());
        //assertEquals("hello, world", recordAfter.get("payload").toString());
    }
}
