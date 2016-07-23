package net.pfx.s5.kuluna.kitwhitebus.csv

import android.content.Context
import com.google.gson.Gson
import net.pfx.s5.kuluna.kitwhitebus.model.Departure
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.model.ScheduleType
import net.pfx.s5.kuluna.kitwhitebus.model.Timetable
import net.pfx.s5.kuluna.kitwhitebus.util.TimeConvert
import java.util.*

/**
 * 時刻表CSVをモデルに変換させる処理を行うクラス
 */
class TimetableCSV(val context: Context, url: String) : CSVExecuter(url) {
    // ポインタとかいう古の技術を使う
    private var pointer: Int = 0

    /**
     * CSVからモデルオブジェクトを生成する
     * @param csv 改行で区切られたCSV
     */
    override fun saveExecute(csv: List<Array<String>>) {
        val timetables = ArrayList<Timetable>()
        nextPointer(csv)
        timetables.addAll(create(csv, ScheduleType.WEEKDAY, Departure.Ogigaoka))
        nextPointer(csv)
        timetables.addAll(create(csv, ScheduleType.WEEKDAY, Departure.Yatsukaho))
        nextPointer(csv)
        timetables.addAll(create(csv, ScheduleType.SATURDAY, Departure.Ogigaoka))
        nextPointer(csv)
        timetables.addAll(create(csv, ScheduleType.SATURDAY, Departure.Yatsukaho))

        // 扇が丘と八束穂に分ける
        val grouping = timetables.groupBy { it.departure }
        val ogigaoka = grouping[Departure.Ogigaoka]!!
        val yatsukaho = grouping[Departure.Yatsukaho]!!
        // データをJSONにして保存
        val gson = Gson()
        Preference(context).run {
            timetableOgigaokaSerialize = gson.toJson(ogigaoka)
            timetableYatsukahoSerialize = gson.toJson(yatsukaho)
            // それぞれの停車順番を保存
            pointsOgigaoka = ogigaoka.first().points
            pointsYatsukaho = yatsukaho.first().points
            // バージョン番号を更新
            timetableVersion = version
        }
    }

    /**
     * CSVの参照している行番号を次の時刻表がある番号まで進める
     * @param csv CSV
     */
    private fun nextPointer(csv: List<Array<String>>) {
        while (csv[pointer][0] != "便名" && pointer <= csv.size) {
            pointer++
        }
    }

    /**
     * モデルオブジェクトの生成
     * @param csv CSV
     * @param scheduleType 運行スケジュール
     * @param departure 出発地
     * @return モデルオブジェクト
     */
    private fun create(csv: List<Array<String>>, scheduleType: ScheduleType, departure: Departure): ArrayList<Timetable> {
        val timetables = ArrayList<Timetable>()

        // 建物名を取得(2列目以降にはいっている)
        val columnNames = csv[pointer]
        pointer++
        // 建物名だけを抽出
        val buildNames = columnNames.slice(1..columnNames.size - 1)

        // 時刻表データを取得(endがくるまで)
        var line = csv[pointer]
        while (line[0] != "end") {
            // 便名
            val timetableName = line[0]
            // 建物名と時刻を取得
            val times = HashMap<String, Int>()
            for (i in 1..line.size - 1) {
                times.put(columnNames[i], TimeConvert.splitTime(line[i]))
            }

            val timetable = Timetable(scheduleType, timetableName, departure, buildNames, times)
            timetables.add(timetable)
            // 次のデータ
            line = csv[++pointer]
        }

        return timetables
    }
}
