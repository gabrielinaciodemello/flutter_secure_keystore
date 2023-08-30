package com.example.flutter_secure_keystore

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

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
    var cryptUtils = CryptUtils()
    var authUtils = AuthUtils()
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "createKey" -> {
        val alias = call.argument<String>("alias")
        if(alias == null){
          result.error("INVALID_INPUT", "Alias was not provided.", null)
          return
        }
        try {
          cryptUtils.createKey(alias)
          result.success(null)
        } catch (e: Exception) { 
          result.error("CREATE_FAILED", e.message, null)
        }
      }
      "encrypt" -> {
        val alias = call.argument<String>("alias")
        val data = call.argument<String>("data")
        if (alias == null || data == null) {
          result.error("INVALID_INPUT", "Alias or data was not provided.", null)
          return
        }
        authUtils.authenticateUser(
          this.context,
          onSuccess = {
            try {
              val encrypted = cryptUtils.encrypt(alias, data)
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
        authUtils.authenticateUser(
          this.context,
          onSuccess = {
            try {
              val decrypted = cryptUtils.decrypt(alias, encryptedData)
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

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
