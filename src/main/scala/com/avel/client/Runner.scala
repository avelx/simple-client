package com.avel.client

import akka.actor.ActorSystem
import akka.actor.ProviderSelection.Cluster
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import akka.stream.scaladsl.Sink

import scala.util.{Failure, Success}

object Runner {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("CassandraClient")
    implicit val dispatcher = system.dispatcher
    import scala.concurrent.Future

    val sessionSettings = CassandraSessionSettings()
    implicit val cassandraSession: CassandraSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings)

    val version: Future[Seq[String]] =
      cassandraSession
        .select("SELECT userid FROM training.users")
        .map(row => row.getString("userid"))
        .runWith(Sink.seq)


    version onComplete {
      case Success(v) => println(v)
      case Failure(ex) => println(ex)
    }

    Thread.sleep(10000)

    system.terminate()
    println("Done")
  }


}
