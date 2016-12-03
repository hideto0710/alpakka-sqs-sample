package com.github.hideto0710.samples

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.sqs.scaladsl.SqsSource
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.{ FiniteDuration, _ }

object AlpakkaSQSSample {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    val credentials = new BasicAWSCredentials(
      config.getString("aws.accessKey"), config.getString("aws.secretKey")
    )
    implicit val sqsClient: AmazonSQSAsyncClient =
      new AmazonSQSAsyncClient(credentials).withEndpoint(config.getString("aws.sqs.endpoint"))

    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()

    val queue = config.getString("aws.sqs.url")

    SqsSource(queue)
      .runForeach((message) => {
        println(message.getBody)
        sqsClient.deleteMessage(
          new DeleteMessageRequest(queue, message.getReceiptHandle)
        )
      })
  }
}
