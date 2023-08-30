package com.example.flutter_secure_keystore

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.flutter.Log
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

/** FlutterSecureKeystorePlugin */
class FlutterSecureKeystorePlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_secure_keystore")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "createKey" -> {
        val alias = call.argument<String>("alias")
        if(alias == null){
          result.error("INVALID_INPUT", "Alias was not provided.", null)
          return;
        }
        try {
          createKey(alias)
          result.success(null)
        } catch (e: Exception) { 
          result.error("CREATEKEY_FAILED", e.message, null)
        }
      }
      "encrypt" -> {
        val alias = call.argument<String>("alias")
        val data = call.argument<String>("data")
        if (alias == null || data == null) {
          result.error("INVALID_INPUT", "Alias or data was not provided.", null)
          return;
        }
        try {
          val encrypted = encrypt(alias, data)
          result.success(encrypted)
        } catch (e: Exception) {
          result.error("ENCRYPTION_FAILED", e.message, e)
        }
      }
      "decrypt" -> {
        val alias = call.argument<String>("alias")
        val encryptedData = call.argument<String>("data")
        if (alias != null && encryptedData != null) {
          try {
            val decrypted = decrypt(alias, encryptedData)
            result.success(decrypted)
          } catch (e: Exception) {
            result.error("DECRYPTION_FAILED", e.message, null)
          }
        } else {
          result.error("INVALID_INPUT", "Alias or encrypted data was not provided.", null)
        }
      }
      else -> result.notImplemented()
    }
  }

  private fun createKey(alias: String) {
      val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
      val keyGenParameterSpec = KeyGenParameterSpec
              .Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
              .setUserAuthenticationRequired(true)
              .build()

      keyGenerator.init(keyGenParameterSpec)
      val secretKey = keyGenerator.generateKey()
  }

  private fun encrypt(alias: String, data: String): String {
    val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
    val secretKey: SecretKey = retrieveKey(alias)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    val ivBytes = cipher.iv
    val combined = ivBytes + encryptedBytes
    return android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)
  }


  private fun decrypt(alias: String, encryptedData: String): String {
      val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
      val secretKey: SecretKey = retrieveKey(alias)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, encryptedData.toByteArray(Charsets.UTF_8)))
      val decryptedBytes = cipher.doFinal(android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT))
      return String(decryptedBytes, Charsets.UTF_8)
  }

  private fun retrieveKey(alias: String): SecretKey {
    val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    return keyStore.getKey(alias, null) as SecretKey
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
