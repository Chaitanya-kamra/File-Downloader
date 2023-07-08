package com.chaitanya.filedownloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import com.chaitanya.filedownloader.activities.MainActivity

class WifiReceiver(private val listener: MainActivity) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

            // Check if connected network is Wi-Fi
            if (networkInfo != null && networkInfo.isConnected && isWifiConnected(connectivityManager)) {
                listener.onWifiConnected()
            }
        }
    }

    private fun isWifiConnected(connectivityManager: ConnectivityManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } else {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return networkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }

    interface WifiConnectivityListener {
        fun onWifiConnected()
    }
}
