package models.daos

import com.mongodb.casbah.Imports.{ DBObject, _ }
import com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers
import com.novus.salat._
import models.{ Election, MongoDBConnection }
import org.bson.types.ObjectId

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class ElectionDAOImpl() extends ElectionDAO {

  private val collectionRef = MongoDBConnection.getConnection
  RegisterConversionHelpers()

  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }

  /**
   * Saves a Election.
   *
   * @param election The Election to save.
   * @return The saved Election.
   */
  def save(election: Election): Future[Election] = {
    val bsonElection = grater[Election].asDBObject(election)
    collectionRef.save(bsonElection)
    Future.successful(election)
  }

  def view(id: ObjectId): List[models.Election] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val u                 = collectionRef.findOne(o)
    val list              = u.toList
    list.map(doc => grater[Election].asObject(doc))
  }

  def userElectionList(email: Option[String]): List[models.Election] = {
    val o: DBObject       = MongoDBObject("creatorEmail" -> email)
    val u                 = collectionRef.find(o)
    val list              = u.toList
    list.map(doc => grater[Election].asObject(doc))
  }

  def userElectionListCount(email: Option[String]): Int = {
    val o: DBObject       = MongoDBObject("creatorEmail" -> email)
    val u                 = collectionRef.find(o)
    val list              = u.toList
    list.length
  }

  def viewCandidate(id: ObjectId): List[String] = {
    val o: DBObject = MongoDBObject("id" -> id)
    val list = collectionRef.findOne(o).toList
    val filteredElections = list.map(doc => grater[Election].asObject(doc))
    if (filteredElections.nonEmpty) {
      filteredElections.head.candidates
    } else {
      null // FIXME: replace null with None
    }
  }

  def vote(id: ObjectId, ballotinput: String): Boolean = {
    val o: DBObject = MongoDBObject("id" -> id)
    var ballot      = ListBuffer[String]()
    ballot += ballotinput
    val c = ballot.toList ::: getBallot(id)
    val update = $set("ballot" -> c)
    collectionRef.update( o, update )
    true // FIXME: Seems like this method can return only true. Why its return type is not a Unit?
  }

  def getBallot(id: ObjectId): List[String] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val list              = collectionRef.findOne(o).toList
    val filteredElections = list.map(doc => grater[Election].asObject(doc))
    var value             = null
    if (filteredElections.nonEmpty) {
      filteredElections.head.ballot
    } else {
      null // FIXME: replace null with None
    }
  }

  def getVoterList(id: ObjectId): List[String] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val list              = collectionRef.findOne(o).toList
    val filteredElections = list.map(doc => grater[Election].asObject(doc))
    var value             = null
    if (filteredElections.nonEmpty) {
      filteredElections.head.voterList
    } else {
      null // FIXME: replace null with None
    }
  }

  def addVoter(id: ObjectId , email : String ): Boolean = {
      val o: DBObject = MongoDBObject("id" -> id)
      val defaultVoterList =  getVoterList(id);
      if(!defaultVoterList.contains(email)){
        var voterList      = ListBuffer[String]()
        voterList += email
        val c = voterList.toList ::: getVoterList(id)
        println(c)
        val update = $set("voterList" -> c )
        collectionRef.update( o, update )
        true
      }
      else{
        false
      }
  }

  def getInviteCode(id: ObjectId): String = {
      val o: DBObject       = MongoDBObject("id" -> id)
      val list              = collectionRef.findOne(o).toList
      val filteredElections = list.map(doc => grater[Election].asObject(doc))
      if (filteredElections.nonEmpty) {
        filteredElections.head.inviteCode
      } else {
        null // FIXME: replace null with None
      }
  }

  def getCreatorEmail(id: ObjectId): Option[String] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val list              = collectionRef.findOne(o).toList
    val filteredElections = list.map(doc => grater[Election].asObject(doc))
    if (filteredElections.nonEmpty) {
      Option(filteredElections.head.creatorEmail)
    } else {
      None // FIXME: replace null with None
    }
  }

  def removeVoter(id : ObjectId, email : String ): Boolean = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val list              = collectionRef.findOne(o).toList
    val filteredElections = list.map(doc => grater[Election].asObject(doc))
    val c = getVoterList(id)
    if(c.contains(email)){
    val update = $set("voterList" -> c.filter(_ != email) )
      collectionRef.update( o, update )
      return true
    }
    return false

  }
}
