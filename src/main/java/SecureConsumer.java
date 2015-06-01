import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by adam on 5/28/15.
 */
public class SecureConsumer extends Thread {
    Properties properties;
    private final ConsumerConnector consumer;
    private final String topic;

    Schema schema;

    public SecureConsumer() {
        this.properties = new PropertiesUtility("/kafka.properties").getProperties();

        this.consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig());
        this.topic = this.properties.getProperty("topic");

        Schema.Parser parser = new Schema.Parser();
        try {
            this.schema = parser.parse(getClass().getResourceAsStream("message.avsc"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SecureConsumer secureConsumer = new SecureConsumer();
        secureConsumer.run();
    }

    public void run() {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(this.topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = this.consumer.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> stream = consumerMap.get(this.topic).get(0);
        ConsumerIterator<byte[], byte[]> it = stream.iterator();

        while (it.hasNext()) {
            byte[] message = it.next().message();
            String decryptedMessage = decryptMessage(message);
        }
    }

    private String decryptMessage(byte[] avroDocument) {
        DatumReader<GenericRecord> reader = new GenericDatumReader<>(this.schema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(avroDocument, null);
        GenericRecord record = null;
        try {
            record = reader.read(null, decoder);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ByteBuffer secretKey = (ByteBuffer) record.get("secretKey");
        ByteBuffer payload = (ByteBuffer) record.get("payload");

        byte[] secretKeyBytes = getBytesFromByteBuffer(secretKey);
        byte[] payloadBytes = getBytesFromByteBuffer(payload);

        byte[] decryptedSecretKey = null;
        byte[] decryptedPayload = null;

        try {
            decryptedSecretKey = RSAEncryptionUtility.decrypt(this.properties.getProperty("privateKeyPath"), secretKeyBytes);
            SecretKey key = new SecretKeySpec(decryptedSecretKey, 0, decryptedSecretKey.length, "AES");

            decryptedPayload = AESEncryptionUtility.decrypt(key, payloadBytes);
        } catch (UnsupportedEncodingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        String decryptedPayloadString = "";
        try {
            decryptedPayloadString = new String(decryptedPayload, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println(decryptedPayloadString);

        return decryptedPayloadString;
    }

    private ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", this.properties.getProperty("zkHosts"));
        props.put("group.id", this.properties.getProperty("consumerGroupID"));
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }

    private byte[] getBytesFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);

        return bytes;
    }
}
