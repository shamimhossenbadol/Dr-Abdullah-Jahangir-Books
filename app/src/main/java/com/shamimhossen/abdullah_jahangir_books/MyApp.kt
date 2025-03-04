package com.shamimhossen.abdullah_jahangir_books

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.analytics.FirebaseAnalytics
import com.onesignal.OneSignal
import com.shamimhossen.abdullah_jahangir_books.receivers.NotificationReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        OneSignal.initWithContext(this, "d3ac7d37-df88-406a-b878-42b4e8ffd6f5")
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        scheduleNotifications(this)
    }

    @SuppressLint("SimpleDateFormat")
    fun scheduleNotifications(context: Context) {
        val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val notificationTimes = listOf("14:30", "20:30")
        var alarmCounter = 0
        for (time in notificationTimes) {
            alarmCounter++
            val splitTime = time.split(":")
            val hour = splitTime[0].toInt()
            val minute = splitTime[1].toInt()
            val calendar: Calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            val alarmIntent = Intent(context, NotificationReceiver::class.java).let { intent ->
                intent.putExtra("number", alarmCounter)
                PendingIntent.getBroadcast(
                    context, alarmCounter, intent, PendingIntent.FLAG_IMMUTABLE
                )
            }
            alarmMgr.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
            val dateFormat = SimpleDateFormat("h:mm:ss a")
            val formattedTime: String = dateFormat.format(calendar.timeInMillis)
            Log.d("notification", "Scheduled alarm $alarmCounter at $formattedTime")
        }
    }
}