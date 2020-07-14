package com.avel.client

import akka.actor.ActorSystem
import akka.stream.alpakka.cassandra.{CassandraSessionSettings, CassandraWriteSettings}
import akka.stream.alpakka.cassandra.scaladsl.{CassandraFlow, CassandraSession, CassandraSessionRegistry}
import akka.stream.scaladsl.{Sink, Source}
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import scala.util.{Failure, Success}
import java.util.UUID.randomUUID

object Runner {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("CassandraClient")
    implicit val dispatcher = system.dispatcher
    import scala.concurrent.Future

    case class User(userid: String, username: String)

    val sessionSettings = CassandraSessionSettings()
    implicit val cassandraSession: CassandraSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings)

    val users = Seq( User(randomUUID().toString, "alpha"), User(randomUUID().toString, "fox") )

    val statementBinder: (User, PreparedStatement) => BoundStatement =
      (user, preparedStatement) => preparedStatement.bind( user.userid, user.username)


    val written: Future[Seq[User]] = Source(users)
      .via(
        CassandraFlow.create(CassandraWriteSettings.defaults,
          s"INSERT INTO training.users(userid, username) VALUES (?, ?)",
          statementBinder)
      )
      .runWith(Sink.seq)

//    written onComplete {
//      case Success(v) => println(v)
//      case Failure(ex) => println(ex)
//    }

    val version: Future[Seq[User]] =
      cassandraSession
        .select("SELECT userid, username FROM training.users")
        .map{ row => User(row.getString("userid"), row.getString("username")) }
        .runWith(Sink.seq)

    for {
      w <- written
      if w.length > 0
      r <- version
    } yield println(r mkString("\n"))


//    version onComplete {
//      case Success(v) => println(v .mkString("\n"))
//      case Failure(ex) => println(ex)
//    }

    Thread.sleep(3000)

    system.terminate()
    println("Done")
  }


}
