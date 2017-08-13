package models.services

import countvotes._

import models.MongoDBConnection
import com.mongodb.casbah.gridfs.Imports._
import java.io.FileInputStream
import scala.collection.mutable.ListBuffer
import org.bson.types.ObjectId
import java.io.File

import countvotes.parsers._
import countvotes.structures._
import countvotes.algorithms._
import countvotes.methods._



import models.Ballot

object  Countvotes {

  def parseCandidates(ballot : String ) : List[Candidate] = {
      var candidatesList      = ListBuffer[Candidate]()
      val ballotToList = ballot.split(">")
      for(candidate <- ballotToList){
        candidatesList+=new Candidate(candidate)
      }
      candidatesList.toList
  }


  def  getWeightedBallots(ballots : List[Ballot]): List[WeightedBallot] = {
      var weightedBallotList      = ListBuffer[WeightedBallot]()
      var i = 1
      for(ballot <- ballots){
        weightedBallotList+=new WeightedBallot(parseCandidates(ballot.voteBallot),i,1)
        i=i+1
      }
      weightedBallotList.toList
  }

  def parseCandidates(candidates : List[String]) : List[Candidate] = {
    var candidatesList      = ListBuffer[Candidate]()
    for(candidate <- candidates){
      candidatesList+=new Candidate(candidate)
    }
    candidatesList.toList
  }

  def countvotesMethod(ballots : List[Ballot], algorithm : String, candidates: List[String], id : ObjectId) : File = {
    val election = getWeightedBallots(ballots)
    val candidate = parseCandidates(candidates)
    var r = (new EVACSnoLPMethod).runScrutiny(Election.weightedElectionToACTElection(election),candidate,1)
    val winnersfile = File.createTempFile( id.toString, ".txt");
    r.writeWinners(winnersfile.getAbsolutePath())
    winnersfile
  }

}
