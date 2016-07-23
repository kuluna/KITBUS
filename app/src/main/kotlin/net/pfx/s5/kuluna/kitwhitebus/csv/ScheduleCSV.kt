package net.pfx.s5.kuluna.kitwhitebus.csv

import android.content.Context
import com.google.gson.Gson
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.model.Schedule
import net.pfx.s5.kuluna.kitwhitebus.model.ScheduleType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 運行表CSVをモデルに変換させる処理を行うクラス
 */
class ScheduleCSV(val context: Context, url: String) : CSVExecuter(url) {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

    /**
     * CSVからモデルオブジェクトを生成する
     * @param csv 改行で区切られたCSV
     */
    override fun saveExecute(csv: List<Array<String>>) {
        // CSVから日付とスケジュールタイプを取得
        val schedules = ArrayList<Schedule>()
        for (i in 2..csv.size - 1) {
            val schedule = Schedule(dateFormat.parse(csv[i][0]), ScheduleType.valueOf(csv[i][1]))
            schedules.add(schedule)
        }

        // データを保存
        val pref = Preference(context)
        pref.scheduleSerialize = Gson().toJson(schedules)
        // バージョン番号を更新
        pref.scheduleVersion = this.version
    }
}
