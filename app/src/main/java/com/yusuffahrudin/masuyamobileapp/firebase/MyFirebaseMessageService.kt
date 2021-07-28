package com.yusuffahrudin.masuyamobileapp.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yusuffahrudin.masuyamobileapp.R
import com.yusuffahrudin.masuyamobileapp.listtimbangan.ListTimbanganActivity
import com.yusuffahrudin.masuyamobileapp.salesorder.SalesOrderActivity
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseMessageService: FirebaseMessagingService() {
    private val TAG = "FirebaseMessageService"
    private val channelID = "1"
    private val channelName = "Notif Channel"
    private val notifID = 101

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d(TAG, "New token: $s")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if(remoteMessage.data.isNotEmpty()){
            val json = JSONObject(remoteMessage.data.toString())
            sendPushNotification(json)
        }
    }

    private fun sendPushNotification(json: JSONObject) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON $json")
        try {
            //getting the json data
            val data = json.getJSONObject("data")
            val title = data.getString("title")
            val message = data.getString("message")
            val topic = data.getString("topic")
            var intent: Intent? = null

            if(topic.equals("SO", false)){
                intent = Intent(applicationContext, SalesOrderActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            } else if(topic.equals("Timbangan", false)) {
                intent = Intent(applicationContext, ListTimbanganActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            }

            val notifManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val largeIcon = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.masuyalogo)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, notifID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notifBuilder = NotificationCompat.Builder(applicationContext, channelID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.masuyalogo)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setSound(ringtone)
                    .setAutoCancel(true)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notifBuilder.priority = NotificationCompat.PRIORITY_HIGH
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                notifBuilder.setChannelId(channelID)
                notifManager.createNotificationChannel(channel)
            }

            notifManager.notify(notifID, notifBuilder.build())

        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }

    }
}