package models.daos

import models.MongoDBConnection
import com.mongodb.casbah.gridfs.Imports._
import java.io.File
import java.io.FileInputStream

class ResultFileDAOImpl() {
    val gridfs = GridFS(MongoDBConnection.getResultConnection)

    def saveResult() = {
      val logo = new FileInputStream("public/files/Examples/candidates.txt")
      gridfs(logo) { fh =>
        fh.filename = "candidates.txt"
        fh.contentType = "text/plain"
      }
    }

    def getResult() : File = {
      val gridfs = GridFS(MongoDBConnection.getResultConnection)
      val file = File.createTempFile("temp-file-name", ".txt");
      gridfs.findOne("candidates.txt").get.writeTo(file)
      file
    }

}
