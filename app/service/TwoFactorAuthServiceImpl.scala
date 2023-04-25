package service

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.Clock
import models.security.User
import models.{TotpToken, TrustedDevices}
import org.joda.time.{DateTime, DateTimeZone}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.math.pow

class TwoFactorAuthServiceImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi, clock: Clock) (implicit ex: ExecutionContext) extends TwoFactorAuthService {
  import utils.BSONUtils.loginInfoHandler
  private def totpTokenCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("totpTokens"))

  private def trustedDevicesCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("trustedDevices"))

  override def totp(time: Long, returnDigits: Int, crypto: String): Future[String] = {
        val msg: Array[Byte] = BigInt(time).toByteArray.reverse.padTo(8, 0.toByte).reverse
        val secret = new SecretKeySpec(crypto.getBytes("UTF-8"), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(secret)
        
        val hash = mac.doFinal(time.toString.getBytes("UTF-8"))
        val offset: Int = hash(hash.length - 1) & 0xf
        val binary: Long = ((hash(offset) & 0x7f) << 24) |
        ((hash(offset + 1) & 0xff) << 16) |
        ((hash(offset + 2) & 0xff) << 8 |
            (hash(offset + 3) & 0xff))

        val otp: Long = binary % (pow(10, returnDigits)).toLong

        Future.successful(("0" * returnDigits + otp.toString).takeRight(returnDigits))
  }

  override def toByteArray(underlying : BigInt): Array[Byte] = {
    val b = underlying.toByteArray
    if (b(0) == 0) b.tail else b
  }

  override def save(token: TotpToken) = {
    totpTokenCollection.flatMap(_.insert.one(token)).flatMap(_ => Future.successful(token))
  }

  override def find(crypto: String) = totpTokenCollection.flatMap(_.find(BSONDocument("crypto" -> crypto))
     .one[TotpToken])

  override def findUser(loginInfo: LoginInfo): Future[Option[TotpToken]] =
    totpTokenCollection.flatMap(_.find(BSONDocument("userLoginInfo" -> loginInfo)).one[TotpToken])

  override def createTotpToken(loginInfo: LoginInfo): Future[TotpToken] = {
    findUser(loginInfo).flatMap {
      case Some(token) =>
        if(!token.expiry.isBefore(clock.now.withZone(DateTimeZone.UTC)))
          Future.successful(token)
        else{
          val crypto = UUID.randomUUID().toString
          val expiry = 300
          var expiresOn = clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry)
          save(TotpToken(crypto, loginInfo, expiresOn, 0))
        }
      case None =>
        val crypto = UUID.randomUUID().toString
        val expiry = 300
        var expiresOn = clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry)
        save(TotpToken(crypto, loginInfo, expiresOn, 0))
    }
  }

  override def findExpired(dateTime: DateTime) =
     totpTokenCollection.flatMap(_.find(BSONDocument.empty).cursor[TotpToken]().collect[Seq](Int.MaxValue, Cursor.FailOnError[Seq[TotpToken]]()))
       .flatMap(tokens => Future.successful(tokens.filter(token => token.expiry.isBefore(dateTime))))

  override def remove(crypto: String) = {
     totpTokenCollection.flatMap(_.delete.one(BSONDocument("crypto" -> crypto))).flatMap(_ => Future.successful(()))
  }

  override def removeExpired() = {
    findExpired(clock.now.withZone(DateTimeZone.UTC)).flatMap { totpTokens =>
      Future.sequence(totpTokens.map { token =>
        remove(token.crypto).map(_ => token)
      })  
    }
  }
  
  override def totpSeq( time: Long, returnDigits: Int, crypto: String, windowSize: Int = 3): Future[Seq[String]] = {
      Future.sequence((-windowSize to windowSize).map{ inc => 
        for{
          token <- totp(time + inc, 6, crypto)
        } yield token
      })
  }

  override def wrongAttempt(crypto: String): Future[Boolean] = {
    find(crypto).flatMap { 
      case Some(token) =>
        val query = BSONDocument("crypto" -> crypto)
        totpTokenCollection.flatMap(_.update.one(query, TotpToken(token.crypto, token.userLoginInfo, token.expiry, token.attempts + 1))).flatMap(_ => Future.successful(true))
    }
    
  }

  override def validate(otp: String, crypto: String): Future[Boolean] = {
    find(crypto).flatMap { 
      case Some(token) =>
        if(token.attempts > 5){
          Future.successful(false)
        }
        else if(token.expiry.isBefore(clock.now.withZone(DateTimeZone.UTC))){
          wrongAttempt(crypto).flatMap { result =>
            Future.successful(false)
          }
        }
        else{
          totpSeq(System.currentTimeMillis / 300000, 6, crypto).flatMap{ tokens =>
            if((tokens.contains(otp.trim)))
              Future.successful(true)
            else{
              wrongAttempt(crypto).flatMap { result =>
                Future.successful(false)
              }
            }
          }
        }
      case None => Future.successful(false)
    }
  }

  override def validateSecurityQuestion(user: User, question: String, answer: String): Future[Boolean] = {
    if(user.securityQuestion.question == question && user.securityQuestion.answer == answer) {
      Future.successful(true)
    }
    else Future.successful(false)
  }

  override def findTrustedDevice(trustedDevice: String) = trustedDevicesCollection.flatMap(_.find(BSONDocument("trustedDevice" -> trustedDevice))
     .one[TrustedDevices])

  override def removeTrustedDevice(trustedDevice: String) = {
     trustedDevicesCollection.flatMap(_.delete.one(BSONDocument("trustedDevice" -> trustedDevice))).flatMap(_ => Future.successful(()))
  }

   override def findTrustedDeviceExpired(dateTime: DateTime) =
     trustedDevicesCollection.flatMap(_.find(BSONDocument.empty).cursor[TrustedDevices]().collect[Seq](Int.MaxValue, Cursor.FailOnError[Seq[TrustedDevices]]()))
       .flatMap(trustedDevices => Future.successful(trustedDevices.filter(trustedDevice => trustedDevice.expiry.isBefore(dateTime))))

  override def removeExpiredTrustedDevice() = {
    findTrustedDeviceExpired(clock.now.withZone(DateTimeZone.UTC)).flatMap { trustedDevices =>
      Future.sequence(trustedDevices.map { trustedDevice =>
        removeTrustedDevice(trustedDevice.trustedDevice).map(_ => trustedDevice)
      })  
    }
  }

  override def addTrustedDevice(loginInfo: LoginInfo): Future[TrustedDevices] = {
    val crypto = UUID.randomUUID().toString
    val expiry = 30
    var expiresOn = clock.now.withZone(DateTimeZone.UTC).plusDays(expiry)
    val trustedDevice = TrustedDevices(loginInfo, crypto, expiresOn)
    trustedDevicesCollection.flatMap(_.insert.one(trustedDevice)).flatMap(_ => Future.successful(trustedDevice))
  }

  override def validateTrustedDevice(loginInfo: LoginInfo, trust: Option[String]): Future[Boolean] = {
    trust match {
      case Some(trust) =>
        findTrustedDevice(trust).flatMap { 
          case Some(trustedDevice) =>
            if(!trustedDevice.expiry.isBefore(clock.now.withZone(DateTimeZone.UTC))){
              if(trust == trustedDevice.trustedDevice && loginInfo == trustedDevice.userLoginInfo)
                Future.successful(true)
              else Future.successful(false)
            }
            else{
              removeExpiredTrustedDevice().flatMap { removed => 
                Future.successful(false)
              }
            }
          case None => Future.successful(false)
        }
      case None => Future.successful(false)
    }
  }
}
