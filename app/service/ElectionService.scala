package service

import com.mohiva.play.silhouette.api.LoginInfo
import models.{Ballot, Election, Voter, Winner}
import org.joda.time.DateTime

import scala.concurrent.Future

trait ElectionService {

  def save(election: Election): Future[Election]

  def retrieve(id: String): Future[Option[Election]]

  def delete(id: String) : Future[Unit]

  def userElectionList(loginInfo : LoginInfo): Future[List[Election]]

  def userElectionListCount(loginInfo : LoginInfo): Future[Int]

  def viewCandidate(id: String): Future[List[String]]

  def vote(id: String, ballot: Ballot): Future[Boolean]

  def getBallots(id: String): Future[List[Ballot]]

  def getVoterList(id: String): Future[List[Voter]]

  def addVoter(id: String , voter : Voter ): Future[Boolean]

  def addVoters(id: String, voters: List[Voter]) : Future[List[Voter]]

  def getInviteCode(id: String): Future[String]

  def getCreatorEmail(id: String): Future[String]

  def removeVoter(id : String, email : String ): Future[Boolean]

  def getStartDate(id: String): Future[DateTime]

  def getEndDate(id: String): Future[DateTime]

  //get all the inactive elections
  def getInactiveElections() :  Future[List[Election]]

  //get all the completed and uncount elections
  def getCompletedElections() :  Future[List[Election]]

  //Update finished election
  def updateCompleteElection(id : String)

  //Update active election
  def updateActiveElection(id : String)

  //get the all unfinished elections
  def getActiveElection() : Future[List[Election]]

  def getActiveElectionWithRealTimeResult() : Future[List[Election]]

  def getBallotVisibility(id : String) : Future[Option[String]]

  def update(election : Election) : Future[Boolean]

  def getWinners(id : String) : Future[List[Winner]]

  def updateWinner(result : List[Winner], id : String)

  def updateIsCounted( id : String)

  def getStatus(id : String) : Future[String]

  def savePollLink(id: String, voterLink: String): Future[Boolean]

}
