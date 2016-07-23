package net.pfx.s5.kuluna.kitwhitebus.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import net.pfx.s5.kuluna.kitwhitebus.InitActivity
import net.pfx.s5.kuluna.kitwhitebus.R

/**
 * アラームを鳴らすReceiver
 */
class RingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // アラームを鳴らす
        val builder = NotificationCompat.Builder(context).apply {
            // アイコン
            setSmallIcon(R.drawable.ic_directions_bus)
            // タイトル
            mContentTitle = context.getString(R.string.departure_soon)

            // 通知音の設定を取得
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            when (am.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> {
                    // 通知音を鳴らす
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                }
                AudioManager.RINGER_MODE_VIBRATE -> {
                    // バイブを鳴らす
                    setVibrate(longArrayOf(0, 900, 200, 900))
                }
            }

            // 通知の重要度を高める
            setPriority(NotificationCompat.PRIORITY_HIGH)
            // 選択したら自動で通知削除
            setAutoCancel(true)
            // 選択したらアプリを起動する
            val initActivity = Intent(context, InitActivity::class.java)
            setContentIntent(PendingIntent.getActivity(context, R.string.app_name + 1, initActivity, 0))
        }

        // 通知を表示
        NotificationManagerCompat.from(context).notify(R.string.app_name, builder.build())
    }
}
