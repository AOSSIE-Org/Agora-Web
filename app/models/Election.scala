package models

//Election model class

case class Election(name:String, description:String , creater_name:String,
                    creator_email:String, starting_Date: String, ending_Date: String , realtime_result: Boolean ,
                    voting_Algo: String , canditates: List[String] , voting_preference: String ,
                    invite_bool: Boolean )



object Election{

  var invite_Link = ""
  var admin_link = ""

  def create(election: Election): Unit = {
    invite_Link = "success"
    admin_link = "success"
  }
}