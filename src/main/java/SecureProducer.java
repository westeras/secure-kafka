import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * Created by adam on 5/27/15.
 */
public class SecureProducer {

    Schema schema;

    public SecureProducer() {
        Schema.Parser parser = new Schema.Parser();
        this.schema = null;
        try {
            this.schema = parser.parse(getClass().getResourceAsStream("message.avsc"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private KeyedMessage<String, byte[]> buildAvroRecord(String messageText, String keyPath) throws UnsupportedEncodingException {
        GenericRecord record = new GenericData.Record(this.schema);

        SecretKey key = AESEncryptionUtility.generateAESKey(256);
        byte[] encryptedKey = RSAEncryptionUtility.encrypt(keyPath, key.getEncoded());
        byte[] encryptedMessage = AESEncryptionUtility.encrypt(key, messageText.getBytes("UTF-8"));

        record.put("secretKey", ByteBuffer.wrap(encryptedKey));
        record.put("payload", ByteBuffer.wrap(encryptedMessage));

        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(this.schema);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        try {
            writer.write(record, encoder);
            encoder.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new KeyedMessage<>("secure", out.toByteArray());
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        PropertiesUtility propertiesUtility = new PropertiesUtility("/kafka.properties");
        Properties properties = propertiesUtility.getProperties();

        properties.setProperty("metadata.broker.list", properties.getProperty("brokerHosts"));
        properties.setProperty("serializer.class", "kafka.serializer.DefaultEncoder");

        ProducerConfig producerConfig = new ProducerConfig(properties);
        Producer<String, byte[]> producer = new Producer<>(producerConfig);
        SecureProducer secureProducer = new SecureProducer();

        String message = "Hello, world!";
        KeyedMessage<String, byte[]> km = secureProducer.buildAvroRecord(message, properties.getProperty("publicKeyPath"));
        producer.send(km);
    }
}
