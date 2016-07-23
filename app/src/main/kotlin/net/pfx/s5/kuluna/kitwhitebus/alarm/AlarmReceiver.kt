package net.pfx.s5.kuluna.kitwhitebus.alarm

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.widget.Toast
import com.google.gson.Gson
import net.pfx.s5.kuluna.kitwhitebus.InitActivity
import net.pfx.s5.kuluna.kitwhitebus.R
import net.pfx.s5.kuluna.kitwhitebus.model.Departure
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.model.Timetable
import net.pfx.s5.kuluna.kitwhitebus.util.TimeConvert

/**
 * アラームを設定するためのレシーバー
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // 必要なデータがなければ終了
        if (intent == null || !intent.hasExtra("timetable") || !intent.hasExtra("alarm")) {
            Toast.makeText(context, "アラームを設定できませんでした。", Toast.LENGTH_LONG).show()
            return
        }

        if (intent.getBooleanExtra("alarm", false)) {
            // アラームセット
            // 時刻表データの取得
            val timetable = Gson().fromJson(intent.getStringExtra("timetable"), Timetable::class.java)
            // 出発時間の取得
            val busTimeMinutes = when (timetable.departure) {
                Departure.Ogigaoka -> timetable.timetables[Preference(context).pointOgigaoka]!!
                Departure.Yatsukaho -> timetable.timetables[Preference(context).pointYatsukaho]!!
            }

            // アラームセット
            setAlarm(context, busTimeMinutes)
            // 通知を表示
            NotificationManagerCompat.from(context).notify(R.string.app_name, createNotification(context, timetable, busTimeMinutes))
        } else {
            // アラーム解除
            val ringReceiver = PendingIntent.getBroadcast(context, R.string.app_name, Intent(context, RingReceiver::class.java), 0)
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(ringReceiver)
            // 通知解除
            NotificationManagerCompat.from(context).cancel(R.string.app_name)
        }
    }

    /**
     * アラームをセットします。
     * @param context Context
     * @param busTimeMinutes 出発時間
     */
    private fun setAlarm(context: Context, busTimeMinutes: Int) {
        // 出発時間にNotificationを表示するためのアラーム設定
        // アラームを鳴らす時間を計算
        val alarmTimeMinutes = (busTimeMinutes - TimeConvert.currentTime - Preference(context).alarmTime) * 60 * 1000
        // 00秒にきっかりアラームを鳴らすための調整
        val currentTime = System.currentTimeMillis()
        val alarmTimedMills = currentTime + alarmTimeMinutes - (currentTime % 60000)

        // アラームをセット
        val ringReceiver = PendingIntent.getBroadcast(context, R.string.app_name, Intent(context, RingReceiver::class.java), 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimedMills, ringReceiver)
    }

    /**
     * 通知を作成します。
     * @param context Context
     * @param timetable 時刻表データ
     * @oaram busTimeMinutes 出発時間
     * @return Android通知オブジェクト
     */
    private fun createNotification(context: Context, timetable: Timetable, busTimeMinutes: Int): Notification {
        return NotificationCompat.Builder(context).apply {
            // アイコン
            setSmallIcon(R.drawable.ic_directions_bus)
            // タイトル
            val departureTimeString = TimeConvert.attachTime(busTimeMinutes)
            mContentTitle = "$departureTimeString ${context.getString(R.string.departure)}"
            // テキスト
            val departure = when (timetable.departure) {
                Departure.Ogigaoka -> Preference(context).pointOgigaoka
                Departure.Yatsukaho -> Preference(context).pointYatsukaho
            }
            mContentText = context.getString(R.string.alarm_departure, departure)
            // ユーザーが消去不可の通知にする
            setOngoing(true)
            // 選択したらアプリを起動する
            val initActivity = Intent(context, InitActivity::class.java)
            setContentIntent(PendingIntent.getActivity(context, R.string.app_name + 1, initActivity, 0))
            // キャンセルボタンを作成
            val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("timetable", "")
                putExtra("alarm", false)
            }
            addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.cancel), PendingIntent.getBroadcast(context, 0, receiverIntent, 0))

        }.build()
    }
}
