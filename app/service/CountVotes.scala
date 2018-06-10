package service

import scala.collection.mutable.ListBuffer

import countvotes.structures._
import countvotes.methods._

import models.Ballot

object  CountVotes {

  def parseCandidates( ballot : String ) : List[Candidate] = {
      var candidatesList = ListBuffer[Candidate]()
      val ballotToList = ballot.split(",|>")
      for(candidate <- ballotToList){
        candidatesList+=new Candidate(candidate)
      }
      candidatesList.toList
  }

  def parseCandidatesWithIndifference(ballot : String) : List[(Candidate,Int)]= {
    var rank = 1
    var ballotVar = ballot
    var candidatesList = ListBuffer[(Candidate,Int)]()
    val list = ballot.split("\\=|>")
    for(candidate <- list){
      ballotVar = ballotVar.replaceFirst(candidate, "")
      val cand = (new Candidate(candidate),rank)
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
      candidatesList += Candidate(candidate)
    }
    candidatesList.toList
  }



  def countVotesMethod(ballots : List[Ballot], algorithm : String, candidates: List[String]) : List[(Candidate, Rational)] = {
    if(ballots.size!=0){
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
        | "Scottish STV" | "Proportional Approval voting"
        | "Ranked Pairs" | "Cumulative voting" => {
          List.empty[(Candidate, Rational)]
        }
        case "Oklahoma Method" => {
          OklahomaMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Satisfaction Approval voting" => {
          SatisfactionApprovalVoting.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Sequential Proportional Approval voting" => {
          SequentialProportionalApprovalVoting.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Top Cycle" => {
          SmithSetMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Approval" => {
          ApprovalRule.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Exhaustive ballot" => {
          InstantExhaustiveBallot.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Baldwin" => {
          BaldwinMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Preferential block voting" => {
          PreferentialBlockVoting.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Exhaustive ballot with dropoff" => {
          InstantExhaustiveDropOffRule.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Uncovered Set" => {
          UncoveredSetMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Copeland" => {
          CopelandMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Minimax Condorcet" => {
          MinimaxCondorcetMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Random Ballot" => {
          RandomBallotMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Majority" => {
          MajorityRuleMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Borda" => {
          BordaRuleMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Kemeny-Young" => {
          KemenyYoungMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Nanson" => {
          NansonMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Instant Runoff 2-round" => {
          InstantRunoff2Round.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Contingent Method" => {
          ContingentMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case "Coomb’s" => {
          CoombRuleMethod.winners(Election.weightedElectionToACTElection(election),candidate,1)
        }
        case _ => {
          List.empty[(Candidate, Rational)]
        }
      }
    }
    else{
      List.empty[(Candidate, Rational)]
    }
  }
}
