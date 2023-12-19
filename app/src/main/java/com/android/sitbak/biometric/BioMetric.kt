package com.android.sitbak.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.io.IOException
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.security.cert.CertificateException


class BioMetric(val activity: FragmentActivity, val bioMetricInterface: BioMetricInterface) {

    companion object {
        private const val KEY_NAME = "KeyName"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val FORWARD_SLASH = "/"
    }

    interface BioMetricInterface {
        fun isDeviceCompatible(isDeviceCompatible: Boolean)
        fun bioMetricSuccess(result: BiometricPrompt.AuthenticationResult)
        fun bioMetricFailed(errorCode: Int, errorString: String)
    }

    init {
        isBiometricCompatibleDevice()
    }


    fun onTouchIdClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            getCipher()?.let { BiometricPrompt.CryptoObject(it) }?.let { getBiometricPromptHandler().authenticate(getBiometricPrompt(), it) }
            getBiometricPromptHandler().authenticate(getBiometricPrompt());
        else
            bioMetricInterface.isDeviceCompatible(false)
    }

    private fun isBiometricCompatibleDevice(): Boolean {
        return if (BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                generateSecretKey()
                true
            } else {
                bioMetricInterface.isDeviceCompatible(false)
                false
            }
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun generateSecretKey() {
        var keyGenerator: KeyGenerator? = null
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(false)
            .build()
        try {
            keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE
            )
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }
        if (keyGenerator != null) {
            try {
                keyGenerator.init(keyGenParameterSpec)
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            }
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey? {
        var keyStore: KeyStore? = null
        var secretKey: Key? = null
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        }
        if (keyStore != null) {
            try {
                keyStore.load(null)
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            try {
                secretKey = keyStore.getKey(KEY_NAME, null)
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: UnrecoverableKeyException) {
                e.printStackTrace()
            }
        }
        return secretKey as SecretKey?
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCipher(): Cipher? {
        var cipher: Cipher? = null
        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + FORWARD_SLASH
                    .toString() + KeyProperties.BLOCK_MODE_CBC + FORWARD_SLASH
                    .toString() + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
            try {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }
        return cipher
    }

    private fun getBiometricPrompt(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("SitBak Login")
//            .setSubtitle("Login with your biometric credential")
            .setNegativeButtonText("cancel")
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }

    private fun getBiometricPromptHandler(): BiometricPrompt {
        return BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    bioMetricInterface.bioMetricFailed(errorCode, errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    bioMetricInterface.bioMetricSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    bioMetricInterface.bioMetricFailed(-1, "Authentication Failed")
                }
            }
        )
    }

}