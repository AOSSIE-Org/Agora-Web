package dao

import javax.inject.Inject
import models.Election.ElectionWrites
import models.{Ballot, Election, Voter, Winner}
import play.api.libs.json._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection
import service.ElectionService

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.Breaks._


class ElectionDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends ElectionService {
  /**
    * The data store for the elections.
    */
  private def electionsCollection = reactiveMongoApi.database.map(_.collection[JSONCollection]("election"))

  /**
    * Saves an Election.
    *
    * @return The saved Election.
    */
  override def save(election: Election): Future[Election] = {
    electionsCollection.flatMap(_.insert(election)).flatMap {
      _ => Future.successful(election)
    }
  }

  override def retrieve(id: String): Future[Option[Election]] = {
    val query = Json.obj("_id" -> id)
    electionsCollection.flatMap(_.find(query).one[Election])
  }

  override def delete(id: String): Future[Unit] = {
    val query = Json.obj("_id" -> id)
    electionsCollection.flatMap(_.remove(query)).flatMap {
      _ => Future.successful(())
    }
  }

  override def userElectionList(email: Option[String]): Future[List[Election]] = {
    val query = Json.obj("creatorEmail" -> email)
    electionsCollection.flatMap(_.find(query).cursor[Election]().collect[List]())
  }

  override def userElectionListCount(email: Option[String]): Future[Int] = {
    userElectionList(email).flatMap(elects => Future.successful(elects.length))
  }

  override def viewCandidate(id: String): Future[List[String]] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.candidates)
      case _ => Future.successful(List.empty[String])
    }
  }

  override def vote(id: String, ballot: Ballot): Future[Boolean] = {
    val query = Json.obj("_id" -> id)
    var ballotList = ListBuffer[Ballot]()
    ballotList += ballot
    getBallots(id).flatMap {
      result =>
        val ballots = ballotList.toList.:::(result)
        val modifier = Json.obj("$set" -> Json.arr("ballot" -> ballots))
        electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
    }
  }

  override def getBallots(id: String): Future[List[Ballot]] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.ballot)
      case _ => Future.successful(List.empty[Ballot])
    }
  }

  override def getVoterList(id: String): Future[List[Voter]] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.voterList)
      case _ => Future.successful(List.empty[Voter])
    }
  }

  private def isVoterInList(voter: Voter, list: List[Voter]): Boolean = {
    var result: Boolean = false
    for (voterD <- list) {
      if (voterD.email == voter.email) {
        result = true
      }
    }
    result
  }

  override def addVoter(id: String, voter: Voter): Future[Boolean] = {
    val query = Json.obj("_id" -> id)
    getVoterList(id).flatMap {
      result =>
        if (!isVoterInList(voter, result)) {
          val voterList = ListBuffer[Voter]()
          voterList += voter
          val voters = voterList.toList.:::(result)
          val modifier = Json.obj("$set" -> Json.arr("voterList" -> voters))
          electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
        } else {
          Future.successful(false)
        }
    }
  }

  override def getInviteCode(id: String): Future[String] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.inviteCode)
      case _ => Future.successful("")
    }
  }

  override def getCreatorEmail(id: String): Future[String] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.creatorEmail)
      case _ => Future.successful("")
    }
  }

  override def removeVoter(id: String, email: String): Future[Boolean] = {
    val query = Json.obj("_id" -> id)
    getVoterList(id).flatMap {
      result =>
        for (index <- result.indices) {
          if (result(index).email == email) {
            val voters = result.drop(index)
            val modifier = Json.obj("$set" -> Json.arr("voterList" -> voters))
            electionsCollection.flatMap(_.update(query, modifier))
            return Future.successful(true)
          }
        }
        return Future.successful(false)
    }
  }


  override def getStartDate(id: String): Future[java.util.Date] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.start)
    }
  }

  override def getEndDate(id: String): Future[java.util.Date] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.end)
    }
  }

  //get all the inactive elections
  override def getInactiveElections(): Future[List[Election]] = {
    val query = Json.obj("isStarted" -> false)
    electionsCollection.flatMap(_.find(query).cursor[Election](readPreference = ReadPreference.primary).collect[List]())
  }

  //get all the completed and uncount elections
  override def getCompletedElections(): Future[List[Election]] = {
    val query = Json.obj("isCompleted" -> true, "isCounted" -> false)
    electionsCollection.flatMap(_.find(query).cursor[Election](readPreference = ReadPreference.primary).collect[List]())
  }

  //Update finished election
  override def updateCompleteElection(id: String) = {
    val query = Json.obj("_id" -> id)
    val modifier = Json.obj("$set" -> Json.obj("isCompleted" -> true))
    electionsCollection.flatMap(_.update(query, modifier))
  }

  //Update active election
  override def updateActiveElection(id: String) = {
    val query = Json.obj("_id" -> id)
    val modifier = Json.obj("$set" -> Json.obj("isStarted" -> true))
    electionsCollection.flatMap(_.update(query, modifier))
  }

  //get the all unfinished elections
  override def getActiveElection(): Future[List[Election]] = {
    val query = Json.obj("isStarted" -> true, "isCompleted" -> false)
    electionsCollection.flatMap(_.find(query).cursor[Election](readPreference = ReadPreference.primary).collect[List]())
  }

  override def getActiveElectionWithRealTimeResult(): Future[List[Election]] = {
    val query = Json.obj("isStarted" -> true, "isCompleted" -> false, "realtimeResult" -> true)
    electionsCollection.flatMap(_.find(query).cursor[Election](readPreference = ReadPreference.primary).collect[List]())
  }

  override def getBallotVisibility(id: String): Future[Option[String]] = {
    retrieve(id).flatMap {
      case Some(result) => Future.successful(Some(result.ballotVisibility))
      case _ => Future.successful(None)
    }
  }

  override def update(election: Election): Future[Boolean] = {
    val query = Json.obj("_id" -> election.id)
    val modifier = Json.obj("$set" -> Json.toJson(election)(ElectionWrites))
    electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
  }

  override def getWinners(id: String): Future[List[Winner]] = {
    retrieve(id).flatMap {
      case Some(result) => Future.successful(result.winners)
      case _ => Future.successful(List.empty[Winner])
    }
  }

  override def updateWinner(result: List[Winner], id: String) = {
    electionsCollection.flatMap(_.update(Json.obj("_id" -> id), Json.obj("$set" -> Json.arr("winners" -> result))))
    electionsCollection.flatMap(_.update(Json.obj("_id" -> id), Json.obj("$set" -> Json.obj("isCounted" -> true))))
  }

  override def updateIsCounted(id: String) = {
    electionsCollection.flatMap(_.update(Json.obj("_id" -> id), Json.obj("$set" -> Json.obj("isCounted" -> true))))
  }

  override def getStatus(id : String) : Future[String] = {
    retrieve(id).flatMap {
      case Some(result) => Future.successful(result.status())
    }
  }

  override def votedElectionList(email: Option[String]): Future[List[Election]] = {
    val electionList = electionsCollection.flatMap(_.find(Json.obj("isStarted" -> true))
      .cursor[Election](readPreference = ReadPreference.primary).collect[List]())
    if (email.isDefined) {
      var votedList = ListBuffer[Election]()
      electionList.flatMap {
        elects =>
          for (election <- elects) {
            breakable {
              for (ballot <- election.ballot) {
                if (ballot.voterEmail == email.get) {
                  votedList += election
                  break
                }
              }
            }
          }
          Future.successful(votedList.toList)
      }
    } else {
      Future.successful(List.empty)
    }
  }
}
