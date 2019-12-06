package models

import java.security.MessageDigest
import java.math.BigInteger


object md5HashString {

    def hashString(key: String): String = {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(key.getBytes)
        val bigInt = new BigInteger(1,digest)
        val hashedString = bigInt.toString(16)
        return hashedString
    }

}
