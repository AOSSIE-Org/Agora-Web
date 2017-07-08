package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.Election
import models.daos.ElectionDAOImpl._

import scala.collection.mutable
import scala.concurrent.Future

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.DBObject
import org.bson.types.ObjectId

import models.MongoDBConnection



class ElectionDAOImpl extends ElectionDAO {
    val collectionRef = MongoDBConnection.getConnection();


  /**
   * Saves a Election.
   *
   * @param Election The Election to save.
   * @return The saved Election.
   */
  def save(election: Election) = {

  val candidates = election.candidates.split(",")
  val  electionObject : MongoDBObject = MongoDBObject(
      "name" -> election.name,
      "description" -> election.description,
      "creatorName" -> election.creatorName,
      "creatorEmail" -> election.creatorEmail,
      "start" -> election.start,
      "end" -> election.end ,
      "realtimeResult" -> election.realtimeResult ,
      "votingAlgo" -> election.votingAlgo,
      "candidates" -> candidates ,
      "isPublic" -> election.isPublic,
      "isInvite" -> election.isInvite,
      "isCompleted" -> false,
      "createdTime" ->  new java.util.Date
  );

  collectionRef.save(electionObject)
  Future.successful(election)
  }


  def view(id: ObjectId) : List[com.mongodb.casbah.Imports.DBObject] = {


      val o : DBObject = MongoDBObject("_id" -> id)
      val u = collectionRef.findOne(o)
      val list = u.toList


    return list;

  }

  def userElectionList(email : Option[String]) : List[com.mongodb.casbah.Imports.DBObject] = {
          val o : DBObject = MongoDBObject("creatorEmail" -> email)
          val u = collectionRef.find(o)
          val list = u.toList
          return list;
  }

}

/**
 * The companion object.
 */
object ElectionDAOImpl {


}
