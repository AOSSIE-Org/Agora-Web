package models

import java.util.Date
import org.bson.types.ObjectId

/**
 * Election model which is created by user or guest
 *
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
 * @param isInvite Specify Whether the election is invitable or not
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
  isInvite: Boolean,
  isCompleted: Boolean,
  createdTime: Date,
  adminLink: String,
  inviteCode: String,
  ballot: List[Ballot],
  voterList : List[Voter]
)
