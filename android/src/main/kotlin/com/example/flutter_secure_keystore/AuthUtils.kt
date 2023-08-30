package com.example.flutter_secure_keystore

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal

class AuthUtils {
    fun authenticateUser(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val biometricPrompt = BiometricPrompt.Builder(context)
            .setTitle("Authentication required")
            .setSubtitle("Please auth to proceed.")
            .setNegativeButton("Cancel", context.mainExecutor) { _, _ ->
                onFailure()
            }
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
}