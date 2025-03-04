package com.shamimhossen.abdullah_jahangir_books.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.shamimhossen.abdullah_jahangir_books.MyApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("notification", "boot complete")
            MyApp().scheduleNotifications(context)
        }
    }
}