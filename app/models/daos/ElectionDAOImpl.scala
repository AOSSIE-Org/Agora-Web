package models.daos

import com.mongodb.casbah.Imports.{ DBObject, _ }
import com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers
import com.novus.salat._
import models.{ Election, MongoDBConnection , Ballot , Voter , Winner}
import org.bson.types.ObjectId

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.control.Breaks._



class ElectionDAOImpl() extends ElectionDAO {

  private val collectionRef = MongoDBConnection.getConnection
  RegisterConversionHelpers()

  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }

  /**
   * Saves an Election.
   *
   * @param election The Election to save.
   * @return The saved Election.
   */
  def save(election: Election): Future[Election] = {
    val bsonElection = grater[Election].asDBObject(election)
    collectionRef.save(bsonElection)
    Future.successful(election)
  }

  def view(id: ObjectId): List[Election] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val u                 = collectionRef.findOne(o)
    val list              = u.toList
    list.map(doc => grater[Election].asObject(doc))
  }

  def delete(id: ObjectId) : Option[DBObject] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val result = collectionRef.findAndRemove(o)
    result
  }

  def userElectionList(email: Option[String]): List[Election] = {
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
      List.empty[String]
    }
  }

  def vote(id: ObjectId, ballot: Ballot): Boolean = {
    val o: DBObject = MongoDBObject("id" -> id)
    var ballotList = ListBuffer[Ballot]()
    ballotList += ballot
    val c = ballotList.toList ::: getBallots(id)
    val bsonBallot = c.map(doc => grater[Ballot].asDBObject(doc))
    val update = $set("ballot" -> bsonBallot )
    try{
      val updateResult = collectionRef.update( o, update ,false,false,WriteConcern.Safe)
      true
    }
    catch {
      case e: Exception => {
        false
      }
    }
  }

  def getBallots(id: ObjectId): List[Ballot] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val list              = collectionRef.findOne(o).toList
    val filteredElections = list.map(doc => grater[Election].asObject(doc))
    var value             = null
    if (filteredElections.nonEmpty) {
      filteredElections.head.ballot
    } else {
      List.empty[Ballot]
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
      List.empty[Voter]
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

      if (con) {
        val voterList = ListBuffer[Voter]()
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

  def getInviteCode(id: ObjectId): Option[String] = {
      val o: DBObject       = MongoDBObject("id" -> id)
      val list              = collectionRef.findOne(o).toList
      val filteredElections = list.map(doc => grater[Election].asObject(doc))
      if (filteredElections.nonEmpty) {
        Option(filteredElections.head.inviteCode)
      } else {
        None
      }
  }

  def getCreatorEmail(id: ObjectId): Option[String] = {
    val o: DBObject       = MongoDBObject("id" -> id)
    val list              = collectionRef.findOne(o).toList
    val filteredElections = list.map(doc => grater[Election].asObject(doc))
    if (filteredElections.nonEmpty) {
      Option(filteredElections.head.creatorEmail)
    } else {
      None
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
    def getCompletedElections() :  List[models.Election] = {
        val o: DBObject       = MongoDBObject("isCompleted" -> true , "isCounted" -> false)
        val u                 = collectionRef.find(o)
        val list              = u.toList
        list.map(doc => grater[Election].asObject(doc))
    }

    //Update finished election
    def updateCompleteElection(id : ObjectId) = {
      val o: DBObject = MongoDBObject("id" -> id)
      val update = $set("isCompleted" -> true )
      val updateResult = collectionRef.update( o, update )
    }

    //Update finished election
    def updateActiveElection(id : ObjectId) = {
      val o: DBObject = MongoDBObject("id" -> id)
      val update = $set("isStarted" -> true )
      val updateResult = collectionRef.update( o, update )
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

    def update(election : Election) : Boolean = {
      val bsonElection = grater[Election].asDBObject(election)
      val query = MongoDBObject("id" -> election.id)
      val list              = collectionRef.findOne(query).toList
      val filteredElections = list.map(doc => grater[Election].asObject(doc))
      if(!filteredElections.head.isStarted){
        val res2 = collectionRef.update(query, bsonElection)
        true
      }
      else{
        false
      }
    }

    def getWinners(id : ObjectId) : List[Winner] = {
      val o: DBObject       = MongoDBObject("id" -> id)
      val list              = collectionRef.findOne(o).toList
      val filteredElections = list.map(doc => grater[Election].asObject(doc))
      if (filteredElections.nonEmpty) {
        filteredElections.head.winners
      } else {
         List.empty[Winner]
      }
    }

    def updateWinner(result : List[Winner], id : ObjectId) = {
      val o: DBObject       = MongoDBObject("id" -> id)
      val list              = collectionRef.findOne(o).toList
      val filteredElections = list.map(doc => grater[Election].asObject(doc))
      val update = $set("winners" -> result.map(doc => grater[Winner].asDBObject(doc)))
      collectionRef.update( o, update )
      val updateisCounted = $set("isCounted" -> true)
      collectionRef.update( o, updateisCounted )
    }

    def updateIsCounted( id : ObjectId) = {
      val o: DBObject       = MongoDBObject("id" -> id)
      val list              = collectionRef.findOne(o).toList
      val filteredElections = list.map(doc => grater[Election].asObject(doc))
      val update = $set("isCounted" -> true)
      collectionRef.update( o, update )
    }

    def votedElectionList(email : Option[String]) : List[Election] = {
      var votedList      = ListBuffer[Election]()
      val o: DBObject       = MongoDBObject("isStarted" -> true)
      val u                 = collectionRef.find(o)
      val list              = u.toList
      val electionList= list.map(doc => grater[Election].asObject(doc))
      if(email!=None){
        for(election <- electionList){
          breakable {
            for(ballot <- election.ballot){
              if(ballot.voterEmail == email.get){
                votedList += election
                break
              }
            }
          }
        }
      }
      votedList.toList
    }


}
