package service

import models.{Ballot, Election, Voter, Winner}

import scala.concurrent.Future

trait ElectionService {

  def save(election: Election): Future[Election]

  def retrieve(id: String): Future[Option[Election]]

  def delete(id: String) : Future[Unit]

  def userElectionList(email: Option[String]): Future[List[Election]]

  def userElectionListCount(email: Option[String]): Future[Int]

  def viewCandidate(id: String): Future[List[String]]

  def vote(id: String, ballot: Ballot): Future[Boolean]

  def getBallots(id: String): Future[List[Ballot]]

  def getVoterList(id: String): Future[List[Voter]]

  def addVoter(id: String , voter : Voter ): Future[Boolean]

  def getInviteCode(id: String): Future[String]

  def getCreatorEmail(id: String): Future[String]

  def removeVoter(id : String, email : String ): Future[Boolean]

  def getStartDate(id: String): Future[java.util.Date]

  def getEndDate(id: String): Future[java.util.Date]

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

  def votedElectionList(email : Option[String]) : Future[List[Election]]
}
