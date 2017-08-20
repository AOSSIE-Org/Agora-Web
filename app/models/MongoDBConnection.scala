package models

import com.mongodb.casbah.Imports._
import com.typesafe.config.ConfigFactory

object MongoDBConnection {

  def getConnection: MongoCollection = {
    val mongoUri    = MongoClientURI(ConfigFactory.load().getString("mongodb.default.uri"))
    val mongoClient = MongoClient(mongoUri)
    val db          = mongoClient("heroku_2gt8k1nz")
    db("electionData")
  }

  def getResultConnection : MongoDB = {
    val mongoUri    = MongoClientURI(ConfigFactory.load().getString("mongodb.default.uri"))
    val mongoClient = MongoClient(mongoUri)
    val db          = mongoClient("heroku_2gt8k1nz")
    db
  }
  def getUserConnection : MongoCollection = {
    val mongoUri    = MongoClientURI(ConfigFactory.load().getString("mongodb.default.uri"))
    val mongoClient = MongoClient(mongoUri)
    val db          = mongoClient("heroku_2gt8k1nz")
    db("userData")
  }
  def getAuthConnection : MongoCollection = {
    val mongoUri    = MongoClientURI(ConfigFactory.load().getString("mongodb.default.uri"))
    val mongoClient = MongoClient(mongoUri)
    val db          = mongoClient("heroku_2gt8k1nz")
    db("authData")
  }
}
