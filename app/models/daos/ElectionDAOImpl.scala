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
      "creater_name" -> election.createrName,
      "creatorEmail" -> election.creatorEmail,
      "start" -> election.start,
      "end" -> election.end ,
      "realtimeResult" -> election.realtimeResult ,
      "votingAlgo" -> election.votingAlgo,
      "candidates" -> candidates ,
      "isPublic" -> election.isPublic,
      "isInvite" -> election.isInvite,
      "createdTime" ->  new java.util.Date
  );
  val collectionRef = MongoDBConnection.getConnection()
  collectionRef.save(electionObject)
  Future.successful(election)
  }
}

/**
 * The companion object.
 */
object ElectionDAOImpl {


}
