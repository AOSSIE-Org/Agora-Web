package models.services

import countvotes._
import models.Ballot
import models.MongoDBConnection
import com.mongodb.casbah.gridfs.Imports._
import java.io.FileInputStream
import scala.collection.mutable.ListBuffer

object  Countvotes {

  def  getWeightedBallots(ballots : List[Ballot]): List[String] = {
      var ballotList      = ListBuffer[String]()
      for(ballot <- ballots){
        ballotList += ballot.voteBallot
      }
      ballotList.toList
    }

  def countvotesMethod() = {
    val gridfs = GridFS(MongoDBConnection.getResultConnection)
    // val logo = new FileInputStream("public/files/Examples/candidates.txt")
    // gridfs(logo) { fh =>
    //   fh.filename = "candidates.txt"
    //   fh.contentType = "text/plain"
    // }
    val file = gridfs.findOne("candidates.txt")
    println(file)
  }

}
