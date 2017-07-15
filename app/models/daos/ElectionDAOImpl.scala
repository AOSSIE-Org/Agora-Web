package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.Election
import models.daos.ElectionDAOImpl._

import scala.collection.mutable
import scala.concurrent.Future

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.DBObject
import org.bson.types.ObjectId

import models.MongoDBConnection

import play.api.Play

import com.novus.salat._
import com.novus.salat.global._
import scala.collection.mutable.ListBuffer


import com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers




class ElectionDAOImpl ()extends ElectionDAO  {


    val collectionRef = MongoDBConnection.getConnection();
    RegisterConversionHelpers()

implicit val ctx = new Context {
   val name = "Custom_Classloader"
}

  /**
   * Saves a Election.
   *
   * @param Election The Election to save.
   * @return The saved Election.
   */
  def save(election: Election) = {
  val candidates = election.candidates
  val bsonElection = grater[Election].asDBObject(election)
  collectionRef.save(bsonElection)
  Future.successful(election)
  }


  def view(id: ObjectId) : List[models.Election] = {
      val o : DBObject = MongoDBObject("_id" -> id)
      val u = collectionRef.findOne(o)
      val list = u.toList
      val filteredElections = list map (doc => grater[Election].asObject(doc))
      return filteredElections;

  }

  def userElectionList(email : Option[String]) : List[models.Election] = {
          val o : DBObject = MongoDBObject("creatorEmail" -> email)
          val u = collectionRef.find(o)
          val list = u.toList
          val filteredElections = list map (doc => grater[Election].asObject(doc))
          return filteredElections;
  }


  def viewCandidate(id: ObjectId) : List[String] = {
          val o : DBObject = MongoDBObject("_id" -> id)
          val list = collectionRef.findOne(o).toList
          val filteredElections = list map (doc => grater[Election].asObject(doc))
          var value = null;
          if(!filteredElections.isEmpty){
          return filteredElections.head.candidates
          }
          return null;

      }

  def vote(id: ObjectId , ballotinput: String) : Boolean = {
          val o : DBObject = MongoDBObject("_id" -> id)
          var ballot = ListBuffer[String]()
          ballot+= ballotinput
          val c = ballot.toList ::: getBallot(id)




          val update = $set("ballot" -> c)
          val list = collectionRef.update( o, update )
          return true
  }

  def getBallot(id: ObjectId): List[String] = {
          val o : DBObject = MongoDBObject("_id" -> id)
          val list = collectionRef.findOne(o).toList
          val filteredElections = list map (doc => grater[Election].asObject(doc))
          var value = null;
          if(!filteredElections.isEmpty){
              return filteredElections.head.ballot
          }
          return null
  }
}

/**
 * The companion object.
 */
object ElectionDAOImpl {


}
