package br.com.receitasairfryer.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "cooking-timers"
        val notifications = context.getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            notifications.createNotificationChannel(
                NotificationChannel(channelId, "Timers de cozinha", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val step = intent.getIntExtra(STEP, 0) + 1
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Timer concluído")
            .setContentText("A etapa $step da receita terminou.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notifications.notify(intent.getStringExtra(RECIPE_ID).orEmpty().hashCode(), notification)
    }

    companion object {
        const val RECIPE_ID = "recipe_id"
        const val STEP = "step"
    }
}
