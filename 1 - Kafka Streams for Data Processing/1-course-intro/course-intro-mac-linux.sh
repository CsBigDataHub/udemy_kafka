#!/bin/zsh

# download kafka at  https://www.apache.org/dyn/closer.cgi?path=/kafka/0.11.0.1/kafka_2.11-0.11.0.1.tgz
# extract kafka in a folder

### LINUX / MAC OS X ONLY

# open a shell - zookeeper is at localhost:2181
zookeeper-server-start config/zookeeper.properties

# open another shell - kafka is at localhost:9092
kafka-server-start config/server.properties

# create input topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-plaintext-input

# create output topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic streams-wordcount-output

# start a kafka producer
kafka-console-producer --broker-list localhost:9092 --topic streams-plaintext-input
# enter
kafka streams udemy
kafka data processing
kafka streams course
# exit

# verify the data has been written
kafka-console-consumer --bootstrap-server localhost:9092 --topic streams-plaintext-input --from-beginning

# start a consumer on the output topic
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic streams-wordcount-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

# start the streams application
kafka-run-class org.apache.kafka.streams.examples.wordcount.WordCountDemo

# verify the data has been written to the output topic!
