package models

import java.util.Date
import org.bson.types.ObjectId

/**
 * Election model which is created by user or guest
 *
 * @param id The name of the election
 * @param name The name of the election
 * @param description The short description about the election
 * @param creatorName The name of the creator of the election
 * @param creatorEmail The email of the creator of the election
 * @param start The start date of the election
 * @param end The end date of the election
 * @param realtimeResult Specify whether show the results in real time or not
 * @param votingAlgo The voting alogorithm for the election
 * @param candidates The canditate list for the election
 * @param ballotVisibility Specify  the ballot visibility level
 * @param voterListVisibility Specify  the voter list visibility level
 * @param isInvite Specify Whether the election is invitable or not
 * @param isCompleted Specify the election is completed or not
 * @param isStarted Specify  the election is started or not
 * @param createdTime created time of election
 * @param adminLink  admin link for the election
 * @param inviteCode secret code for the election
 * @param ballot ballot list of the election
 * @param voterList voter list of the election
 * @param winners winner list
 * @param isCounted is the election is counted or not
 */
case class Election(
  id: ObjectId,
  name: String,
  description: String,
  creatorName: String,
  creatorEmail: String,
  start: Date,
  end: Date,
  realtimeResult: Boolean,
  votingAlgo: String,
  candidates: List[String],
  ballotVisibility: String,
  voterListVisibility : Boolean,
  isInvite: Boolean,
  isCompleted: Boolean,
  isStarted : Boolean,
  createdTime: Date,
  adminLink: String,
  inviteCode: String,
  ballot: List[Ballot],
  voterList : List[Voter],
  winners : List[Winner],
  isCounted : Boolean,
  noVacancies : Int   
) {
  def status(): String = {
    if (!isStarted) "Not yet started"
    else if (isStarted && !isCompleted) "Active"
    else "Completed"
  }
}
