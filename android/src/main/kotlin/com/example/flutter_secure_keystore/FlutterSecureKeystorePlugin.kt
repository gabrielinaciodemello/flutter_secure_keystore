package com.example.flutter_secure_keystore

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/** FlutterSecureKeystorePlugin */
class FlutterSecureKeystorePlugin: FlutterPlugin, MethodCallHandler {

  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_secure_keystore")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
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
        authenticateUser(
          this.context,
          onSuccess = {
            try {
              val encrypted = encrypt(alias, data)
              result.success(encrypted)
            } catch (e: Exception) {
              result.error("ENCRYPTION_FAILED", e.message, e)
            }
          },
          onFailure = {
            result.error("ENCRYPTION_FAILED", "User is not authenticated", null)
          }
        )
      }
      "decrypt" -> {
        val alias = call.argument<String>("alias")
        val encryptedData = call.argument<String>("data")
        if(alias == null || encryptedData == null){
          result.error("INVALID_INPUT", "Alias or encrypted data was not provided.", null)
          return
        }
        authenticateUser(
          this.context,
          onSuccess = {
            try {
              val decrypted = decrypt(alias, encryptedData)
              result.success(decrypted)
            } catch (e: Exception) {
              result.error("DECRYPTION_FAILED", e.message, e)
            }
          },
          onFailure = {
            result.error("DECRYPTION_FAILED", "User is not authenticated", null)
          }
        )
      }
      else -> result.notImplemented()
    }
  }

  private fun createKey(alias: String) {
      val keyGenerator = KeyGenerator.getInstance(
              KeyProperties.KEY_ALGORITHM_AES,
              "AndroidKeyStore"
      )
      val keyGenParameterSpec = KeyGenParameterSpec
              .Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
              .setUserAuthenticationRequired(true)
              .setUserAuthenticationValidityDurationSeconds(30)
              .build()

      keyGenerator.init(keyGenParameterSpec)
      val secretKey = keyGenerator.generateKey()
  }

  private fun retrieveKey(alias: String): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    return keyStore.getKey(alias, null) as SecretKey
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
    val iv = combined.sliceArray(0 until 16) // 16 bytes for IV
    val encryptedData = combined.sliceArray(16 until combined.size)
    val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
    return String(cipher.doFinal(encryptedData))
  }

  fun authenticateUser(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
    val biometricPrompt = BiometricPrompt.Builder(context)
            .setTitle("Authentication required")
            .setSubtitle("Please auth to proceed.")
            .setNegativeButton("Cancel", context.mainExecutor, DialogInterface.OnClickListener { _, _ ->
              onFailure()
            })
            .build()
    val cancellationSignal = CancellationSignal()
    cancellationSignal.setOnCancelListener {
      onFailure()
    }
    biometricPrompt.authenticate(cancellationSignal, context.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        onSuccess()
      }
      override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        super.onAuthenticationError(errorCode, errString)
        onFailure()
      }
      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        onFailure()
      }
    })
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
