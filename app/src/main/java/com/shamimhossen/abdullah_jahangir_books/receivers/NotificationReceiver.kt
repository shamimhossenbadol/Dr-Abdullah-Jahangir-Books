package com.shamimhossen.abdullah_jahangir_books.receivers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationManagerCompat
import com.shamimhossen.abdullah_jahangir_books.R
import com.shamimhossen.abdullah_jahangir_books.activities.MainActivity
import com.shamimhossen.abdullah_jahangir_books.models.NotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("notification", "onReceive")
        val mChannel = NotificationChannel("CHANNEL_ID", "name", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        CoroutineScope(Dispatchers.IO).launch {
            val list = mutableListOf<NotificationModel>()
            context.assets.open("notifications.json").use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        NotificationModel(
                            obj.getString("title"),
                            obj.getString("description")
                        )
                    )
                }
            }
            val random = list.indices.random()
            val flag = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val clickIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, flag)
            val builder = NotificationCompat.Builder(context, "CHANNEL_ID").apply {
                setSmallIcon(R.mipmap.ic_launcher_round)
                setContentTitle(list[random].title)
                setContentText(list[random].message)
                priority = NotificationCompat.PRIORITY_HIGH
                setContentIntent(pendingIntent)
                setAutoCancel(true)
                setStyle(BigTextStyle().bigText(list[random].message))
            }
            withContext(Dispatchers.Main) {
                with(NotificationManagerCompat.from(context)) {
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@with
                    }
                    notify(1, builder.build())
                }
            }
        }
    }
}