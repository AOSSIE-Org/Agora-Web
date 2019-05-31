package dao

import java.net.URLDecoder
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.Election._
import models.{Ballot, Election, Voter, Winner}
import org.joda.time.DateTime
import play.api.libs.json._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection
import service.ElectionService

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.Breaks._

import java.security.MessageDigest
import java.math.BigInteger


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
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    electionsCollection.flatMap(_.find(query).one[Election])
  }

  override def delete(id: String): Future[Unit] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    electionsCollection.flatMap(_.remove(query)).flatMap {
      _ => Future.successful(())
    }
  }

  override def userElectionList(loginInfo: LoginInfo): Future[List[Election]] = {
    val query = Json.obj("loginInfo" -> loginInfo)
    electionsCollection.flatMap(_.find(query).cursor[Election]().collect[List]())
  }

  override def userElectionListCount(loginInfo: LoginInfo): Future[Int] = {
    userElectionList(loginInfo).flatMap(elects => Future.successful(elects.length))
  }

  override def viewCandidate(id: String): Future[List[String]] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.candidates)
      case _ => Future.successful(List.empty[String])
    }
  }

  override def vote(id: String, ballot: Ballot): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    var ballotList = ListBuffer[Ballot]()
    ballotList += ballot
    getBallots(id).flatMap {
      result =>
        val ballots = ballotList.toList.:::(result)
        val modifier = Json.obj("$set" -> Json.obj("ballot" -> Json.toJson(ballots)))
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
    //Should return gmail for emails like abc@gmail.com or abc.d@gmail.com
    // i.e return the string between the last @ and the first .
    val domainRegx = """(?<=@)[^.]+(?=\.)""".r

    //Should return username for email eg abc for abc@gmail.com
    //i.e return the the string before the last @
    val usernameRegx = """.+(?=@)""".r

    val decodedEmail = URLDecoder.decode(voter.email, "UTF-8").toLowerCase()
    for (voterD <- list) {
      val decodedEmailInList = URLDecoder.decode(voterD.email, "UTF-8").toLowerCase()
      if (decodedEmail == decodedEmailInList) {
        return true
      } else {
        //Check if this email is from gmail and test for difference in username with . (dots)
        for {
          domain <- domainRegx.findFirstIn(decodedEmail)
        } yield {
          if (domain == "gmail") {
            for {
              username1 <- usernameRegx.findFirstIn(decodedEmail)
              username2 <- usernameRegx.findFirstIn(decodedEmailInList)
            } yield {
              //For emails with abc+def@gmail.com everything after the + is ignored
              val realUsername1 = username1.split('+').head
              val realUsername2 = username2.split('+').head
              if(username1.replaceAll("\\.", "") == username2.replaceAll("\\.", ""))
                return true
            }
          }
        }
      }
    }
    return false
  }

  private def isVoterInBallot(voter: Voter, list: List[Ballot]): Boolean = {
    for (voterD <- list) {
      if (voterD.voterEmail == voter.email)
        return true
    }
    return false
  }

  override def addVoter(id: String, voter: Voter): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    val hashedVoter = Voter(voter.name, md5HashString(voter.email.concat(getInviteCode(id).toString)))
    getVoterList(id).flatMap {
      result =>
        if (!isVoterInList(hashedVoter, result)) {
          getBallots(id).flatMap {
            ballotResult =>
              if (!isVoterInBallot(hashedVoter, ballotResult)) {
                val voterList = ListBuffer[Voter]()
                voterList += hashedVoter
                val voters = voterList.toList.:::(result)
                val modifier = Json.obj("$set" -> Json.obj("voterList" -> Json.toJson(voters)))
                electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
              }
              else {
                Future.successful(false)
              }
          }
        } else {
            Future.successful(false)
        }
    }
  }

  override def addVoters(id: String, voters: List[Voter]): Future[List[Voter]] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    getVoterList(id).flatMap {
      result =>
         getBallots(id).flatMap {
            ballotResult =>
              val hashedVoters = voters.map{ voter => Voter(voter.name, md5HashString(voter.email.concat(getInviteCode(id).toString))) }
              val hashedFilteredList = hashedVoters.filter(voter => !isVoterInList(voter, result) && !isVoterInBallot(voter, ballotResult))
              val allVoters = hashedFilteredList.:::(result)
              val modifier = Json.obj("$set" -> Json.obj("voterList" -> Json.toJson(allVoters)))
              electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(voters))
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
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    getVoterList(id).flatMap {
      result =>
        val voters = result.filter(v => v.email != email)
        val modifier = Json.obj("$set" -> Json.obj("voterList" -> Json.toJson(voters)))
        electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
    }
  }


  override def getStartDate(id: String): Future[DateTime] = {
    retrieve(id).flatMap {
      case Some(elect) => Future.successful(elect.start)
    }
  }

  override def getEndDate(id: String): Future[DateTime] = {
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
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    val modifier = Json.obj("$set" -> Json.obj("isCompleted" -> Json.toJson(true)))
    electionsCollection.flatMap(_.update(query, modifier))
  }

  //Update active election
  override def updateActiveElection(id: String) = {
    val query = Json.obj("_id" -> id)
    val modifier = Json.obj("$set" -> Json.obj("isStarted" -> Json.toJson(true)))
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
    val query = Json.obj("_id" -> Json.obj("$oid" -> election.id.get))
    val modifier = Json.obj("$set" -> Json.toJson(election.copy(id = None)))
    electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
  }

  override def getWinners(id: String): Future[List[Winner]] = {
    retrieve(id).flatMap {
      case Some(result) => Future.successful(result.winners)
      case _ => Future.successful(List.empty[Winner])
    }
  }

  override def updateWinner(result: List[Winner], id: String) = {
    electionsCollection.flatMap(_.update(Json.obj("_id" -> Json.obj("$oid" -> id)),
      Json.obj("$set" -> Json.obj("winners" -> Json.toJson(result), "isCounted" -> Json.toJson(true)))))
  }

  override def updateIsCounted(id: String) = {
    electionsCollection.flatMap(_.update(Json.obj("_id" -> Json.obj("$oid" -> id)),
      Json.obj("$set" -> Json.obj("isCounted" -> Json.toJson(true)))))
  }

  override def getStatus(id: String): Future[String] = {
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
  override def md5HashString(key: String): String = {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(key.getBytes)
    val bigInt = new BigInteger(1,digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }
}
