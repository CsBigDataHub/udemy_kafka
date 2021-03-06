#!/bin/zsh

# create input topic with one partition to get full ordering
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic favourite-colour-input

# create output log compacted topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic favourite-colour-output --config cleanup.policy=compact


# launch a Kafka consumer
kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic favourite-colour-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

# launch the streams application

# then produce data to it
kafka-console-producer --broker-list localhost:9092 --topic favourite-colour-input --property parse.key=true --property key.separator=,
#
stephane,blue
john,green
stephane,red
alice,red


# list all topics that we have in Kafka (so we can observe the internal topics)
kafka-topics --list --zookeeper localhost:2181
