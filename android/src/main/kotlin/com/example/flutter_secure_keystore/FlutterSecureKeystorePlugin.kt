package com.example.flutter_secure_keystore

import android.content.Context
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FlutterSecureKeystorePlugin */
class FlutterSecureKeystorePlugin: FlutterPlugin, MethodCallHandler, ActivityAware {

  private lateinit var channel : MethodChannel
  private lateinit var context: Context
  private var activity: FragmentActivity? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_secure_keystore")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    val cryptUtils = CryptUtils()
    val authUtils = AuthUtils()
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "save" -> {
        val key = call.argument<String>("key")
        val value = call.argument<String>("value")
        if(key == null || value == null){
          result.error("INVALID_INPUT", "Key or Value was not provided.", null)
          return
        }
        authUtils.authenticateUser(
          this.activity!!,
          onSuccess = {
            try {
              cryptUtils.save(this.context, key, value)
              result.success(null)
            } catch (e: Exception) {
              result.error("SAVE_FAILED", e.message, null)
            }
          },
          onFailure = {
            result.error("SAVE_FAILED", "User is not authenticated", null)
          }
        )
      }
      "get" -> {
        val key = call.argument<String>("key")
        if(key == null){
          result.error("INVALID_INPUT", "Key was not provided.", null)
          return
        }
        authUtils.authenticateUser(
          this.activity!!,
          onSuccess = {
            try {
              result.success(cryptUtils.get(this.context, key))
            } catch (e: Exception) {
              result.error("GET_FAILED", e.message, null)
            }
          },
          onFailure = {
            result.error("GET_FAILED", "User is not authenticated", null)
          }
        )
      }
      "delete" -> {
        val key = call.argument<String>("key")
        if(key == null){
          result.error("INVALID_INPUT", "Key was not provided.", null)
          return
        }
        try {
          result.success(cryptUtils.delete(this.context, key))
        } catch (e: Exception) {
          result.error("DELETE_FAILED", e.message, null)
        }
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity as FragmentActivity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivity() {
    activity = null
  }
}
