package models.services

import countvotes._

import models.MongoDBConnection
import com.mongodb.casbah.gridfs.Imports._
import java.io.FileInputStream
import scala.collection.mutable.ListBuffer
import org.bson.types.ObjectId
import java.io.File
import java.util.regex.Pattern

import countvotes.parsers._
import countvotes.structures._
import countvotes.algorithms._
import countvotes.methods._



import models.Ballot

object  Countvotes {

  def parseCandidates( ballot : String ) : List[Candidate] = {
      var candidatesList = ListBuffer[Candidate]()
      val ballotToList = ballot.split(",|>")
      for(candidate <- ballotToList){
        candidatesList+=new Candidate(candidate)
      }
      candidatesList.toList
  }

  def parseCandidatesWithIndifference(ballot : String) : List[(Candidate,Int,Int)]= {
    var rank = 1
    var score = 0
    var ballotVar = ballot
    var candidatesList = ListBuffer[(Candidate,Int,Int)]()
    val list = ballot.split("\\=|>")
    for(candidate <- list){
      ballotVar = ballotVar.replaceFirst(candidate, "")
      val cand = (new Candidate(candidate),rank,score)
      candidatesList += cand
      if(ballotVar.size>0){
        if(ballotVar.charAt(0)=='>'){
          rank = rank + 1
        }
        ballotVar = ballotVar.drop(1)
      }

    }
    candidatesList.toList
  }


  def  getWeightedBallots( ballots : List[Ballot] ): List[WeightedBallot] = {
      var weightedBallotList      = ListBuffer[WeightedBallot]()
      var i = 1
      for(ballot <- ballots){
        weightedBallotList += new WeightedBallot(parseCandidates(ballot.voteBallot),i,1)
        i=i+1
      }
      weightedBallotList.toList
  }

  def getWeightedBallotsWithRanked(ballots : List[Ballot]) = {
      for(ballot <- ballots){
        println(parseCandidatesWithIndifference(ballot.voteBallot))
      }
  }

  def parseCandidates( candidates : List[String]) : List[Candidate] = {
    var candidatesList = ListBuffer[Candidate]()
    for(candidate <- candidates){
      candidatesList += new Candidate(candidate)
    }
    candidatesList.toList
  }



  def countvotesMethod( ballots : List[Ballot], algorithm : String, candidates: List[String], id : ObjectId) : List[(Candidate, Rational)] = {
    val election = getWeightedBallots(ballots)
    val candidate = parseCandidates(candidates)
    /**
    Algorithms which are not menitioned in the doc
      EVACS
      EVACSnoLP
      EVACSDWD
      Senate
      Simple
      Egalitarian
      HybridPluralityPreferentialBlockVoting
    **/

    algorithm match {
      case "Range Voting" | "Schulze" | "SMC"
      | "Warren STV"
      | "Meek STV"
      | "Oklahoma Method"
      | "Scottish STV" | "Proportional Approval voting"
      | "Ranked Pairs" | "Cumulative voting" => {
        null
      }
      case "Satisfaction Approval voting" => {
        val h = SatisfactionApprovalVoting.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Sequential Proportional Approval voting" => {
        val h = SequentialProportionalApprovalVoting.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Top Cycle" => {
        val h = SmithSetMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Approval" => {
        val h = ApprovalRule.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Exhaustive ballot" => {
        val h = InstantExhaustiveBallot.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Baldwin" => {
        val h = BaldwinMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Preferential block voting" => {
        val h = PreferentialBlockVoting.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Exhaustive ballot with dropoff" => {
        val h = InstantExhaustiveDropOffRule.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Uncovered Set" => {
        val h = UncoveredSetMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Copeland" => {
        val h = CopelandMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Minimax Condorcet" => {
        val h = MinimaxCondorcetMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Random Ballot" => {
        val h = RandomBallotMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Majority" => {
        val h = MajorityRuleMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Borda" => {
        val h = BordaRuleMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Kemeny-Young" => {
        val h = KemenyYoungMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Nanson" => {
        val h = NansonMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h

        // println(h(0).getClass.getSimpleName)
      }
      case "Instant Runoff 2-round" => {
        val h = InstantRunoff2Round.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Contingent Method" => {
        val h = ContingentMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case "Coomb’s" => {
        val h = CoombRuleMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        return h
      }
      case _ => {
        null
      }
    }
  }
}
