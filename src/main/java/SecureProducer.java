import kafka.javaapi.producer.Producer;
import kafka.message.Message;
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
import org.apache.commons.codec.binary.Base64;
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

    private KeyedMessage<String, String> buildAvroRecord(String messageText, String keyPath) {
        GenericRecord record = new GenericData.Record(this.schema);

        SecretKey key = AESEncryptionUtility.generateAESKey(256);
        String keyString = Base64.encodeBase64String(key.getEncoded());
        String encryptedKeyString = RSAEncryptionUtility.encrypt(keyPath, keyString);
        System.out.println(encryptedKeyString);
        String encryptedMessage = AESEncryptionUtility.encrypt(key, messageText);

        record.put("secretKey", keyString);
        record.put("payload", encryptedMessage);

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

        //Message message = new Message(out.toByteArray());
        return new KeyedMessage<String, String>("secure", new String(out.toByteArray()));
    }

    public static void main(String[] args) {
        PropertiesUtility propertiesUtility = new PropertiesUtility("/kafka.properties");
        Properties properties = propertiesUtility.getProperties();

        properties.setProperty("metadata.broker.list", properties.getProperty("brokerHosts"));
        properties.setProperty("serializer.class", "kafka.serializer.StringEncoder");

        ProducerConfig producerConfig = new ProducerConfig(properties);
        Producer<String, String> producer = new Producer<>(producerConfig);
        SecureProducer secureProducer = new SecureProducer();

        String message = "Hello, world!";
        KeyedMessage<String, String> km = secureProducer.buildAvroRecord(message, properties.getProperty("publicKeyPath"));
        producer.send(km);
    }
}
