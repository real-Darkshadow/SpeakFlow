package com.app.speak.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.widget.Toast
import com.app.speak.R

class NetworkStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "Network connectivity change")
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni: NetworkInfo? = manager.activeNetworkInfo
        online = if (ni == null || ni.state !== NetworkInfo.State.CONNECTED) {
            Log.d(TAG, "There's no network connectivity")
            if (online) // don't show the message if already offline
                Toast.makeText(context, R.string.noInternet, Toast.LENGTH_SHORT).show()
            false
        } else {
            Log.d(TAG, "Network " + ni.typeName.toString() + " connected")
            if (!online) // don't show the message if already online
                Toast.makeText(context, R.string.backOnline, Toast.LENGTH_SHORT).show()
            true
        }
    }

    companion object {
        private var online = true // we expect the app being online when starting
        val TAG = "tag"
    }
}