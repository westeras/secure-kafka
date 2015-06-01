# secure-kafka

This repository contains an example of an encrypting Kafka producer and consumer pair.

AES 256 is used to encrypt the message payload.  An RSA key pair is then used to encrypt the AES symmetric key.  Both the encrypted payload and the encrypted AES key are then serialized using Avro and sent to Kafka.  On the consumer end, the RSA private key is used to decrypt the AES key, which is then used to decrypt the payload.
