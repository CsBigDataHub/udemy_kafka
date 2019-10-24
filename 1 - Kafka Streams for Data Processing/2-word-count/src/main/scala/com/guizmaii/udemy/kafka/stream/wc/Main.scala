package com.guizmaii.udemy.kafka.stream.wc

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.KTable

/**
 * Kafka Stream Scala DSL doc:
 *   - https://kafka.apache.org/documentation/streams/developer-guide/dsl-api.html#scala-dsl
 *
 * Producer console command:
 *   $ kafka-console-producer --broker-list localhost:9092 --topic word-count-input
 *
 * Consumer console command:
 *   $ kafka-console-consumer --bootstrap-server localhost:9092 --topic word-count-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
 *
 */
object Main extends App {

  val config = new Properties
  config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app")
  config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes._

  val builder = new StreamsBuilder

  val wordCounts: KTable[String, Long] =
    builder
      .stream[String, String]("word-count-input")
      .peek((k, v) => println(s"k, v: $k, $v"))
      .flatMapValues(_.toLowerCase.split(' '))
      .selectKey((_, v) => v)
      .groupByKey
      .count()

  wordCounts.toStream.to("word-count-output")

  val streams = new KafkaStreams(builder.build(), config)
  streams.start()

  sys.addShutdownHook {
    val _ = streams.close(Duration.ofSeconds(10))
  }
}
