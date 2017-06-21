package models

import java.util.Date;

/**
  * Election model which is created by user or guest
  *
  * @param name The name of the election
  * @param description The short description about the election
  * @param creater_name The name of the creator of the election
  * @param creatorEmail The email of the creator of the election
  * @param start The start date of the election
  * @param end The end date of the election
  * @param realtimeResult Specify whether show the results in real time or not
  * @param votingAlgo The voting alogorithm for the election
  * @param canditates The canditate list for the election
  * @param isPublic Specify Whether the election is public or not
  * @param isInvite Specify Whether the election is invitable or not
  */

case class Election(name:String, description:String , creater_name:String,
                    creatorEmail : String, start : Date , end : Date , realtimeResult : Boolean ,
                    votingAlgo : String , canditates : List[String] , isPublic : Boolean ,
                    isInvite : Boolean )



object Election{

  var invite_Link = ""
  var admin_link = ""

  def create(election: Election): Unit = {
    invite_Link = "success"
    admin_link = "success"
  }
}