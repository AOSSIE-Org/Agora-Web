package service

import models.{Ballot, Score, Winner}

trait ResultService {

  def saveResult(ballots : List[Ballot], algorithm : String, candidates: List[String], objectId : String)
}
