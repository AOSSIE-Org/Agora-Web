package models

import java.util.Date;
//Election model class

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