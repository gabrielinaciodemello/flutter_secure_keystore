package com.example.flutter_secure_keystore

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AuthUtils {
    fun authenticateUser(activity: FragmentActivity, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val biometricManager = BiometricManager.from(activity)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
            val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        onFailure()
                    }

                    override fun onAuthenticationFailed() {
                        onFailure()
                    }
                })

            // Configuração do prompt de autenticação
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticação")
                .setDescription("Por favor, autentique-se para continuar.")
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            // Exibir o prompt
            biometricPrompt.authenticate(promptInfo)
        } else {
            onFailure()
        }
    }
}