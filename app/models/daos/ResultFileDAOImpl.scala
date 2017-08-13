package models.daos

import models.MongoDBConnection
import com.mongodb.casbah.gridfs.Imports._
import java.io.File
import java.io.FileInputStream
import models.services.Countvotes

import models.Ballot
import org.bson.types.ObjectId


class ResultFileDAOImpl() {
    val gridfs = GridFS(MongoDBConnection.getResultConnection)
    def saveResult(ballots : List[Ballot], algorithm : String, candidates: List[String], objectId : ObjectId) = {
      val logo = Countvotes.countvotesMethod(ballots,algorithm,candidates,objectId)
      var id = objectId.toString
      gridfs.remove(id);
      gridfs(logo) { fh =>
        fh.filename = id
        fh.contentType = "text/plain"
      }
    }

    def getResult(id : String ) : File = {
      val gridfs = GridFS(MongoDBConnection.getResultConnection)
      val file = File.createTempFile("temp-file-name", ".txt");
      gridfs.findOne(id).get.writeTo(file)
      file
    }

}
