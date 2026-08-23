package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

data class CarrierNetworkStatus(
    val operatorName: String,
    val networkType: String,
    val isWifiConnected: Boolean,
    val isMobileDataConnected: Boolean,
    val isRcsAvailable: Boolean,
    val canSendGsmSms: Boolean
)

object CarrierSmsManager {

    /**
     * Get real-time cellular network and operator information (e.g. MTN, Moov, Orange, etc.)
     */
    fun getCarrierNetworkStatus(context: Context): CarrierNetworkStatus {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        // Detect real carrier operator name (e.g., "MTN", "Moov", "Orange", "Airtel", "SFR", "Free", etc.)
        val simOperator = telephonyManager?.simOperatorName?.takeIf { it.isNotBlank() }
        val netOperator = telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() }
        val operator = simOperator ?: netOperator ?: "Orange / MTN / Moov"

        var isWifi = false
        var isMobile = false

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (capabilities != null) {
                    isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    isMobile = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                }
            } else {
                @Suppress("DEPRECATION")
                val netInfo = connectivityManager.activeNetworkInfo
                if (netInfo != null && netInfo.isConnected) {
                    @Suppress("DEPRECATION")
                    isWifi = netInfo.type == ConnectivityManager.TYPE_WIFI
                    @Suppress("DEPRECATION")
                    isMobile = netInfo.type == ConnectivityManager.TYPE_MOBILE
                }
            }
        }

        val networkTypeStr = when {
            isWifi -> "Wi-Fi Haut Débit"
            isMobile -> "Données Mobiles 4G/5G"
            else -> "Réseau Cellulaire GSM"
        }

        return CarrierNetworkStatus(
            operatorName = operator,
            networkType = networkTypeStr,
            isWifiConnected = isWifi,
            isMobileDataConnected = isMobile,
            isRcsAvailable = isWifi || isMobile,
            canSendGsmSms = true
        )
    }

    /**
     * Send Real GSM SMS via Android SmsManager (Compatible API 23 to 34)
     */
    fun sendRealGsmSms(
        context: Context,
        destinationAddress: String,
        text: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (destinationAddress.isBlank()) {
            onError("Numéro de téléphone requis")
            return
        }

        try {
            @Suppress("DEPRECATION")
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            } else {
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(text)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(destinationAddress, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(destinationAddress, null, text, null, null)
            }
            onSuccess()
        } catch (e: SecurityException) {
            // Permission not granted or simulator environment
            onError("Envoi SMS via opérateur simulé / autorisation SMS requise")
        } catch (e: Exception) {
            onError("Erreur SMS opérateur : ${e.localizedMessage}")
        }
    }

    /**
     * Share Voice Note as Universal MMS / Audio File for Any External Messaging App
     */
    fun shareVoiceAsMms(
        context: Context,
        audioFile: File,
        phoneNumber: String = "",
        messageText: String = "Message vocal I3"
    ) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val audioUri: Uri = try {
                FileProvider.getUriForFile(context, authority, audioFile)
            } catch (e: Exception) {
                Uri.fromFile(audioFile)
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, audioUri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                if (phoneNumber.isNotBlank()) {
                    putExtra("address", phoneNumber)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Envoyer le message vocal MMS via..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Partage audio MMS : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
