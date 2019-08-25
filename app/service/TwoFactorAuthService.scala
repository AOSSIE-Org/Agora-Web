package service

import javax.crypto.Mac
import javax.crypto.SecretKey
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec
import scala.math.pow
import scala.math.BigInt
import org.joda.time.DateTime
import models.{TotpToken, TrustedDevices}
import com.mohiva.play.silhouette.api.LoginInfo
import java.security.Key;
import java.security.SecureRandom;
import models.security.User

import scala.concurrent.Future

trait TwoFactorAuthService {

  def totp(time: Long, returnDigits: Int, crypto: String): Future[String]

  def toByteArray(underlying : BigInt): Array[Byte]

  def save(token: TotpToken): Future[TotpToken] 

  def find(crypto: String): Future[Option[TotpToken]]

  def findExpired(dateTime: DateTime): Future[Seq[TotpToken]]

  def remove(crypto: String): Future[Unit]

  def removeExpired(): Future[Seq[TotpToken]]

  def createTotpToken(loginInfo: LoginInfo): Future[TotpToken]

  def findUser(loginInfo: LoginInfo): Future[Option[TotpToken]]

  def totpSeq(time: Long, returnDigits: Int, crypto: String, windowSize: Int = 3): Future[Seq[String]]

  def validate(otp: String, crypto: String): Future[Boolean]

  def wrongAttempt(crypto: String): Future[Boolean]

  def validateSecurityQuestion(user: User, question: String, answer: String): Future[Boolean]

  def findTrustedDevice(trustedDevice: String): Future[Option[TrustedDevices]] 

  def removeTrustedDevice(trustedDevice: String): Future[Unit]

  def removeExpiredTrustedDevice(): Future[Seq[TrustedDevices]]

  def findTrustedDeviceExpired(dateTime: DateTime): Future[Seq[TrustedDevices]]

  def addTrustedDevice(loginInfo: LoginInfo): Future[TrustedDevices]

  def validateTrustedDevice(loginInfo: LoginInfo, trust: Option[String]): Future[Boolean]

}
