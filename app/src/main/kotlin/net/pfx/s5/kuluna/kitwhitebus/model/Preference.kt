package net.pfx.s5.kuluna.kitwhitebus.model

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SharedPreferenceにRWするデータモデル
 */
class Preference(context: Context) {
    // KEYS
    companion object {
        val TIMETABLE_VERSION = "timetable_version"
        val SCHEDULE_VERSION = "schedule_version"
        val TIMETABLE_OGIGAOKA = "timetable_ogigaoka"
        val TIMETABLE_YATSUKAHO = "timetable_yatsukaho"
        val SCHEDULE = "schedule"
        val POINT_OGIGAOKA = "point_ogigaoka"
        val POINTS_OGIGAOKA = "points_ogigaoka"
        val POINT_YATSUKAHO = "point_yatsukaho"
        val POINTS_YATSUKAHO = "points_yatsukaho"
        val ALARM_TIME = "alarm_time"
        val DEBUG_CSV= "debug_csv"
        val DEBUG_TIMETABLE_URL = "debug_timetable_csv"
        val DEBUG_SCHEDULE_URL = "debug_schedule_csv"
    }

    private val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /** 時刻表データのバージョン */
    var timetableVersion: Int
        get() = sp.getInt(TIMETABLE_VERSION, 0)
        set(version) {
            sp.edit().putInt(TIMETABLE_VERSION, version).apply()
        }

    /** 扇ヶ丘時刻表 */
    var timetableOgigaokaSerialize: String
        get() = sp.getString(TIMETABLE_OGIGAOKA, "[]")
        set(timetable) {
            sp.edit().putString(TIMETABLE_OGIGAOKA, timetable).apply()
        }

    /** 八束穂時刻表 */
    var timetableYatsukahoSerialize: String
        get() = sp.getString(TIMETABLE_YATSUKAHO, "[]")
        set(timetable) {
            sp.edit().putString(TIMETABLE_YATSUKAHO, timetable).apply()
        }

    /** 運行スケジュールデータのバージョン */
    var scheduleVersion: Int
        get() = sp.getInt(SCHEDULE_VERSION, 0)
        set(version) {
            sp.edit().putInt(SCHEDULE_VERSION, version).apply()
        }

    /** 運行スケジュール */
    var scheduleSerialize: String
        get() = sp.getString(SCHEDULE, "[]")
        set(schedule) {
            sp.edit().putString(SCHEDULE, schedule).apply()
        }

    /** 扇ヶ丘乗車場所 */
    var pointOgigaoka: String?
        get() = sp.getString(POINT_OGIGAOKA, null)
        set(buildNo) {
            sp.edit().putString(POINT_OGIGAOKA, buildNo).apply()
        }

    /** 扇が丘発停車順一覧 */
    var pointsOgigaoka: List<String>
        get() {
            val data = sp.getString(POINTS_OGIGAOKA, "[]")
            return Gson().fromJson(data, object : TypeToken<List<String>>() {}.type)
        }
        set(points) {
            sp.edit().putString(POINTS_OGIGAOKA, Gson().toJson(points)).apply()
            if (pointOgigaoka == null) {
                pointOgigaoka = points.first()
            }
        }

    /** 八束穂乗車場所 */
    var pointYatsukaho: String?
        get() = sp.getString(POINT_YATSUKAHO, null)
        set(buildNo) {
            sp.edit().putString(POINT_YATSUKAHO, buildNo).apply()
        }

    /** 八束穂発停車順一覧 */
    var pointsYatsukaho: List<String>
        get() {
            val data = sp.getString(POINTS_YATSUKAHO, "[]")
            return Gson().fromJson(data, object : TypeToken<List<String>>() {}.type)
        }
        set(points) {
            sp.edit().putString(POINTS_YATSUKAHO, Gson().toJson(points)).apply()
            if (pointYatsukaho == null) {
                pointYatsukaho = points.first()
            }
        }

    /** アラームを鳴らす開始時間 */
    var alarmTime: Int
        get() = sp.getString(ALARM_TIME, "10").toInt()
        set(alarmTime) {
            sp.edit().putString(ALARM_TIME, alarmTime.toString()).apply()
        }

    /** カスタムCSVを使用する */
    var debugCsv: Boolean
        get() = sp.getBoolean(DEBUG_CSV, false)
        set(enable) {
            sp.edit().putBoolean(DEBUG_CSV, enable).apply()
        }

    /** カスタム時刻表CSVのURL */
    var debugTimetableUrl: String
        get() = sp.getString(DEBUG_TIMETABLE_URL, "")
        set(url) {
            sp.edit().putString(DEBUG_TIMETABLE_URL, url).apply()
        }

    /** カスタムスケジュールCSVのURL */
    var debugScheduleUrl: String
        get() = sp.getString(DEBUG_SCHEDULE_URL, "")
        set(url) {
            sp.edit().putString(DEBUG_SCHEDULE_URL, url).apply()
        }
}
