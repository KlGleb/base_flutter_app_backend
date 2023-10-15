package at.gleb.reviewmagic.utils

import java.math.BigInteger
import java.security.MessageDigest


fun  String.hashPassword() = "review magic 13A3%@Q $this".md5()

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    md.update(this.toByteArray())
    val digest = md.digest()

    val bigInt = BigInteger(1, digest)
    var hashtext: String = bigInt.toString(16)
    while (hashtext.length < 32) {
        hashtext = "0$hashtext"
    }

    return hashtext
}

fun getRandomString(length: Int): String {
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getRandomCode(): String {
    val p1 = "aeio"
    val p2 = "bcdfghklmnpqrstvxz"
    val nums = "01234567890"

    return p2.random().toString() + p1.random().toString() + p2.random().toString() + p1.random()
        .toString() + p2.random().toString() + p1.random().toString() + nums.random().toString() + nums.random()
        .toString() + nums.random()
        .toString()
}