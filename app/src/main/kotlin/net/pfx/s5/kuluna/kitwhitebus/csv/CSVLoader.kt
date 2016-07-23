package net.pfx.s5.kuluna.kitwhitebus.csv

import android.content.Context
import android.os.Bundle
import android.support.v4.content.AsyncTaskLoader
import net.pfx.s5.kuluna.kitwhitebus.R
import net.pfx.s5.kuluna.kitwhitebus.model.Preference

/**
 * CSVを読み込み時刻表の更新処理を行うLoader
 */
class CSVLoader(context: Context) : AsyncTaskLoader<Bundle>(context) {

    override fun loadInBackground(): Bundle? {
        val result = Bundle()
        val pref = Preference(context)

        // 時刻表CSVを更新
        val timetableUrl = {
            if (pref.debugCsv) {
                pref.debugTimetableUrl
            } else {
                context.getString(R.string.timetable_url)
            }
        }
        val timetableResult = updateCsv(TimetableCSV(context, timetableUrl.invoke()), pref.timetableVersion)

        // 運行表CSVを更新
        val scheduleUrl = {
            if (pref.debugCsv) {
                pref.debugScheduleUrl
            } else {
                context.getString(R.string.schedule_url)
            }
        }
        val scheduleResult = updateCsv(ScheduleCSV(context, scheduleUrl.invoke()), pref.scheduleVersion)

        // 両方のデータがあるならtrue
        result.putBoolean("csv", pref.timetableVersion > 0 && pref.scheduleVersion > 0)
        // ネットワークは問題ないが、CSVのパースに失敗していたらtrue
        result.putBoolean("parseError", (timetableResult == UpdateResult.ParseError || scheduleResult == UpdateResult.ParseError))
        return result
    }

    /**
     *  CSVを取得し、最新バージョンであれば更新します
     *  @return 成功ならtrue
     */
    private fun updateCsv(csv: CSVExecuter, oldVersion: Int): UpdateResult {
        if (csv.load()) {
            try {
                if (csv.version > oldVersion) {
                    csv.save()
                }
                return UpdateResult.OK

            } catch (e: Exception) {
                return UpdateResult.ParseError
            }

        } else {
            return UpdateResult.NetworkError
        }
    }

    private enum class UpdateResult {
        OK, NetworkError, ParseError
    }
}
