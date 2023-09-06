package com.example.flutter_secure_keystore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptUtils {
    private val alias = "secure_keystore"

    private fun createKey(alias: String) {
        if(retrieveKey(alias) == null) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec
                .Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(30)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun retrieveKey(alias: String): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        try {
            return keyStore.getKey(alias, null) as SecretKey
        }
        catch (e: Exception){
            return null
        }
    }

    private fun encrypt(alias: String, data: String): String {
        val secretKey = retrieveKey(alias)
        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(data.toByteArray())
        val combined = cipher.iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    private fun decrypt(alias: String, encryptedData: String): String {
        val secretKey = retrieveKey(alias)
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until 16)
        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return String(cipher.doFinal(combined.sliceArray(16 until combined.size)))
    }

    fun save(context: Context, key: String, value: String){
        createKey(alias)
        val encryptValue = encrypt(alias, value)
        val sharedPreferences = context.getSharedPreferences(alias, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, encryptValue)
        editor.apply()
    }

    fun get(context: Context, key: String): String? {
        val sharedPreferences = context.getSharedPreferences(alias, Context.MODE_PRIVATE)
        val encryptValue = sharedPreferences.getString(key, null) ?: return null
        return decrypt(
            alias,
            encryptValue
        )
    }

    fun delete(context: Context, key: String): Boolean? {
        val sharedPreferences = context.getSharedPreferences(alias, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
        return true
    }
    
}