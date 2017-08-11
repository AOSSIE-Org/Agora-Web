package models.daos

import com.mongodb.casbah.Imports.{ DBObject, _ }
import com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers
import com.novus.salat._
import models.{ Election, MongoDBConnection , Ballot , Voter}
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

  def vote(id: ObjectId, ballot: Ballot): Boolean = {
    val o: DBObject = MongoDBObject("id" -> id)
    var ballotList      = ListBuffer[Ballot]()
    ballotList += ballot
    val c = ballotList.toList ::: getBallots(id)
    val bsonBallot = c.map(doc => grater[Ballot].asDBObject(doc))
    val update = $set("ballot" -> bsonBallot )
    collectionRef.update( o, update )
    true // FIXME: Seems like this method can return only true. Why its return type is not a Unit?
  }

  def getBallots(id: ObjectId): List[Ballot] = {
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

  def getVoterList(id: ObjectId): List[Voter] = {
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

  def addVoter(id: ObjectId , voter : Voter ): Boolean = {
      val o: DBObject = MongoDBObject("id" -> id)
      val defaultVoterList =  getVoterList(id);
      var con : Boolean = true
      for(voterD <- defaultVoterList){
          if(voterD.email==voter.email){
            con = false;
          }
      }

      if(con){
        var voterList      = ListBuffer[Voter]()
        voterList += voter
        val c = voterList.toList ::: getVoterList(id)
        val bsonBallot = c.map(doc => grater[Voter].asDBObject(doc))
        val update = $set("voterList" -> bsonBallot )
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
    for(voter <- c){
        if(voter.email==email){
          val update = $set("voterList" -> c.filter(_ != voter).map(doc => grater[Voter].asDBObject(doc)))
          collectionRef.update( o, update )
          return true
        }
      }
    return false
    }


    def getStartDate(id: ObjectId): Option[java.util.Date] = {
        val o: DBObject       = MongoDBObject("id" -> id)
        val list              = collectionRef.findOne(o).toList
        val filteredElections = list.map(doc => grater[Election].asObject(doc))
        if (filteredElections.nonEmpty) {
          Option(filteredElections.head.start)
        } else {
          None
        }
    }

    def getEndDate(id: ObjectId): Option[java.util.Date] = {
        val o: DBObject       = MongoDBObject("id" -> id)
        val list              = collectionRef.findOne(o).toList
        val filteredElections = list.map(doc => grater[Election].asObject(doc))
        if (filteredElections.nonEmpty) {
          Option(filteredElections.head.end)
        } else {
          None
        }
    }

    //get all the inactive elections
    def getInactiveElections() :  List[models.Election] = {
        val o: DBObject       = MongoDBObject("isStarted" -> false)
        val u                 = collectionRef.find(o)
        val list              = u.toList
      list.map(doc => grater[Election].asObject(doc))
    }

    //get all the completed and uncount elections
    def getCompletedElections() :  Option[List[models.Election]] = {
        val o: DBObject       = MongoDBObject("isCompleted" -> true , "isCounted" -> false)
        val u                 = collectionRef.find(o)
        val list              = u.toList
        Option(list.map(doc => grater[Election].asObject(doc)))
    }

    //Update finished election
    def updateCompleteElection(id : ObjectId) : Boolean = {
      val o: DBObject = MongoDBObject("id" -> id)
      val update = $set("isCompleted" -> true )
      collectionRef.update( o, update )
      true // FIXME: Seems like this method can return only true. Why its return type is not a Unit?
    }

    //Update finished election
    def updateActiveElection(id : ObjectId) : Boolean = {
      val o: DBObject = MongoDBObject("id" -> id)
      val update = $set("isStarted" -> true )
      collectionRef.update( o, update )
      true // FIXME: Seems like this method can return only true. Why its return type is not a Unit?
    }



    //get the all unfinished elections
    def getActiveElection() : List[models.Election] = {
      val o: DBObject       = MongoDBObject("isStarted" -> true , "isCompleted" -> false)
      val u                 = collectionRef.find(o)
      val list              = u.toList
      list.map(doc => grater[Election].asObject(doc))
    }

    def getActiveElectionWithRealTimeResult() : List[models.Election] = {
      val o: DBObject       = MongoDBObject("isStarted" -> true , "isCompleted" -> false , "realtimeResult" -> true)
      val u                 = collectionRef.find(o)
      val list              = u.toList
      list.map(doc => grater[Election].asObject(doc))
    }

    def getBallotVisibility(id : ObjectId) : Option[String] = {
      val o: DBObject = MongoDBObject("id" -> id)
      val list              = collectionRef.findOne(o).toList
      val filteredElections = list.map(doc => grater[Election].asObject(doc))
      if (filteredElections.nonEmpty) {
        Option(filteredElections.head.ballotVisibility)
      } else {
        None
      }
    }
}
