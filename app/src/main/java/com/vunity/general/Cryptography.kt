package com.vunity.general

import android.annotation.SuppressLint
import android.util.Base64
import com.vunity.general.Constants.ALGORITHM
import com.vunity.general.Constants.KEY
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@SuppressLint("GetInstance")
@Throws(Exception::class)
fun encrypt(value: String): String {
    val key = generateKey()
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val encryptedByteValue = cipher.doFinal(value.toByteArray(charset("utf-8")))
    return Base64.encodeToString(encryptedByteValue, Base64.DEFAULT)
}

@SuppressLint("GetInstance")
@Throws(Exception::class)
fun decrypt(value: String): String {
    val key = generateKey()
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, key)
    val decryptedValue64 = Base64.decode(value, Base64.DEFAULT)
    val decryptedByteValue = cipher.doFinal(decryptedValue64)
    return String(decryptedByteValue, charset("utf-8"))

}

@Throws(Exception::class)
private fun generateKey(): Key {
    return SecretKeySpec(KEY.toByteArray(), ALGORITHM)
}