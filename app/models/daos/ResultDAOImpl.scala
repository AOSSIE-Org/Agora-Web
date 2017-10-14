package models.daos

import models.MongoDBConnection
import com.mongodb.casbah.gridfs.Imports._
import java.io.File
import models.services.Countvotes

import models.Ballot
import models.Winner
import models.Score

import org.bson.types.ObjectId

class ResultDAOImpl() {
    val electionDAOImpl = new ElectionDAOImpl()
    val gridfs = GridFS(MongoDBConnection.getResultConnection)

    def saveResult(ballots : List[Ballot], algorithm : String, candidates: List[String], objectId : ObjectId) = {
      val result = Countvotes.countvotesMethod(ballots,algorithm,candidates,objectId)
      if(result.size!=0){
        val winnerList = for ((candidate, rational ) <- result) yield {
          new Winner( candidate,new Score(rational.numerator.intValue , rational.denominator.intValue ))
        }
        electionDAOImpl.updateWinner(winnerList.toList, objectId);
      }
      else{
        electionDAOImpl.updateIsCounted(objectId)
      }
    }

    def getResult(id : String ) : File = {
      val gridfs = GridFS(MongoDBConnection.getResultConnection)
      val file = File.createTempFile("result", ".txt");
      gridfs.findOne(id).get.writeTo(file)
      file
    }

}
