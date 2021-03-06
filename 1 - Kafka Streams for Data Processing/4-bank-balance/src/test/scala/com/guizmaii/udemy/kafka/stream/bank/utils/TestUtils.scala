package com.guizmaii.udemy.kafka.stream.bank.utils

import cats.effect.{ IO, Resource }
import com.guizmaii.udemy.kafka.stream.bank.Main.config
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ Deserializer, Serializer }
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.{ Topology, TopologyTestDriver }

object TestUtils {

  private implicit final class ConsumerRecordFactoryOps[K, V](private val factory: ConsumerRecordFactory[K, V])
      extends AnyVal {
    def make(k: K, v: V): ConsumerRecord[Array[Byte], Array[Byte]] = factory.create(k, v)
  }

  private implicit final class TopologyTestDriverOps(private val topologyTestDriver: TopologyTestDriver)
      extends AnyVal {
    def read[K: Deserializer, V: Deserializer](topic: NewTopic): ProducerRecord[K, V] =
      topologyTestDriver.readOutput(topic.name, implicitly[Deserializer[K]], implicitly[Deserializer[V]])
  }

  implicit final class ProducerRecordOps[K, V](private val record: ProducerRecord[K, V]) extends AnyVal {
    def keyAndValue: (K, V) = record.key() -> record.value()
  }

  private def ConsumerRecordFactory[K: Serializer, V: Serializer](topic: NewTopic): ConsumerRecordFactory[K, V] =
    new ConsumerRecordFactory[K, V](topic.name, implicitly[Serializer[K]], implicitly[Serializer[V]])

  private def topologyTestDriverR(topology: Topology): Resource[IO, TopologyTestDriver] =
    Resource.fromAutoCloseable(IO.delay { new TopologyTestDriver(topology, config) })

  trait Producer[K, V] {
    def produce(topic: NewTopic)(k: K, v: V): Unit
  }

  trait Consumer[K, V] {
    def consume(topic: NewTopic): ProducerRecord[K, V]
  }

  def testStream[InputKey: Serializer, InputValue: Serializer, OutputKey: Deserializer, OutputValue: Deserializer, T](
    topologyBuilder: StreamsBuilder => IO[Topology]
  )(test: (Producer[InputKey, InputValue], Consumer[OutputKey, OutputValue]) => T): T = {
    val program =
      for {
        topology <- topologyBuilder(new StreamsBuilder)
        test <- topologyTestDriverR(topology).use { testDriver =>
                 IO.delay {
                   val producer = new Producer[InputKey, InputValue] {
                     override def produce(topic: NewTopic)(k: InputKey, v: InputValue): Unit =
                       testDriver.pipeInput(ConsumerRecordFactory[InputKey, InputValue](topic).make(k, v))
                   }
                   val consumer = new Consumer[OutputKey, OutputValue] {
                     override def consume(topic: NewTopic): ProducerRecord[OutputKey, OutputValue] =
                       testDriver.read[OutputKey, OutputValue](topic)
                   }

                   test(producer, consumer)
                 }
               }
      } yield test

    program.unsafeRunSync()
  }

}
