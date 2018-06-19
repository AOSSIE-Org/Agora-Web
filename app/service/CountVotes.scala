package service

import agora.model.{Candidate, Election, PreferenceBallot}
import agora.votecounter._

import scala.collection.mutable.ListBuffer
import models.Ballot
import spire.math.Rational

object  CountVotes {

  def parseBallot(ballot : Ballot ) : List[Candidate] = {
      var candidatesList = ListBuffer[Candidate]()
      val ballotToList = ballot.voteBallot.split(",|>")
      for(candidate <- ballotToList){
        candidatesList += Candidate(candidate)
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
      val cand = (Candidate(candidate),rank)
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

  def parseCandidates( candidates : List[String]) : List[Candidate] = {
    var candidatesList = ListBuffer[Candidate]()
    for(candidate <- candidates){
      candidatesList += Candidate(candidate)
    }
    candidatesList.toList
  }

  def parsePreferenceBallots(ballots: List[Ballot]) : List[agora.model.PreferenceBallot] = {
    var preferenceBallots = ListBuffer[agora.model.PreferenceBallot]()
    var id = 1
    for (ballot <- ballots) {
      preferenceBallots += PreferenceBallot(parseBallot(ballot), id, Rational(1,1))
      id = id + 1
    }
    preferenceBallots.toList
  }

  def countVotesMethod(ballots : List[Ballot], algorithm : String, candidateNames: List[String], noVacancies: Int) : List[(Candidate, Rational)] = {
    if(ballots.size!=0){
      val candidates = parseCandidates(candidateNames)
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
        case "Oklahoma" => {
          val election = Election(parsePreferenceBallots(ballots))
          Oklahoma.winners(election, candidates, noVacancies)
        }
        case "SAV" => {
          val election = Election(parsePreferenceBallots(ballots))
          SatisfactionApprovalVoting.winners(election, candidates, noVacancies)
        }
        case "Sequential Proportional Approval voting" => {
          val election = Election(parsePreferenceBallots(ballots))
          SequentialProportionalApprovalVoting.winners(election, candidates,noVacancies)
        }
        case "SmithSet" => {
          val election = Election(parsePreferenceBallots(ballots))
          SmithSet.winners(election, candidates,noVacancies)
        }
        case "Approval" => {
          val election = Election(parsePreferenceBallots(ballots))
          ApprovalRule.winners(election,candidates,noVacancies)
        }
        case "Exhaustive ballot" => {
          val election = Election(parsePreferenceBallots(ballots))
          InstantExhaustiveBallot.winners(election ,candidates,noVacancies)
        }
        case "Baldwin" => {
          val election = Election(parsePreferenceBallots(ballots))
          BaldwinMethod.winners(election, candidates,noVacancies)
        }

        case "Exhaustive ballot with dropoff" => {
          val election = Election(parsePreferenceBallots(ballots))
          InstantExhaustiveDropOffRule.winners(election, candidates,noVacancies)
        }

        case "Uncovered Set" => {
          val election = Election(parsePreferenceBallots(ballots))
          UncoveredSet.winners(election,candidates,noVacancies)
        }
        case "Copeland" => {
          val election = Election(parsePreferenceBallots(ballots))
          Copeland.winners(election, candidates,noVacancies)
        }
        case "Minimax Condorcet" => {
          val election = Election(parsePreferenceBallots(ballots))
          MinimaxCondorcet.winners(election,candidates,noVacancies)
        }
        case "Random Ballot" => {
          val election = Election(parsePreferenceBallots(ballots))
          RandomBallot.winners(election, candidates,noVacancies)
        }
        case "Majority" => {
          val election = Election(parsePreferenceBallots(ballots))
          Majority.winners(election ,candidates,noVacancies)
        }
        case "Borda" => {
          val election = Election(parsePreferenceBallots(ballots))
          Borda.winners(election, candidates,noVacancies)
        }
        case "Kemeny-Young" => {
          val election = Election(parsePreferenceBallots(ballots))
          KemenyYoung.winners(election,candidates,noVacancies)
        }
        case "Nanson" => {
          val election = Election(parsePreferenceBallots(ballots))
          Nanson.winners(election, candidates, noVacancies)
        }
        case "Instant Runoff 2-round" => {
          val election = Election(parsePreferenceBallots(ballots))
          InstantRunoff2Round.winners(election ,candidates,noVacancies)
        }
        case "Contingent Method" => {
          val election = Election(parsePreferenceBallots(ballots))
          Contingent.winners(election,candidates,noVacancies)
        }
        case "Coomb’s" => {
          val election = Election(parsePreferenceBallots(ballots))
          Coomb.winners(election,candidates,noVacancies)
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
