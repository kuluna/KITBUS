package net.pfx.s5.kuluna.kitwhitebus.util

import net.pfx.s5.kuluna.kitwhitebus.model.ScheduleType
import org.apache.commons.lang3.time.DateUtils
import java.util.*

/**
 * 現在の時刻・曜日や時刻データをプログラム内で使う値に変換するクラス
 */
object TimeConvert {

    /**
     * 現在時刻 単位(分)
     */
    val currentTime: Int
        get() {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            return hour * 60 + minute
        }

    /**
     * 現在の曜日
     */
    val currentWeek: ScheduleType
        get() = getNormalScheduleType(Calendar.getInstance())

    /**
     * 次の0秒になるまでの残りミリ秒
     */
    val justZeroSecond: Long
        get() {
            return 60000 - System.currentTimeMillis() % 60000
        }

    /**
     * データベース上の時刻データを数値に変換します。
     * @param timeData 時刻データ
     * @return 時刻(例えば1:10なら70)
     */
    fun splitTime(timeData: String): Int {
        val timeAry = timeData.split(":".toRegex())
        return Integer.parseInt(timeAry[0]) * 60 + Integer.parseInt(timeAry[1])
    }

    /**
     * 時刻データに:をつけてString値へと変換します。
     * @param timeData 時刻データ
     * @return 時刻(例えば70なら1:10)
     */
    fun attachTime(timeData: Int): String {
        val hour = timeData / 60
        val min = timeData % 60
        return String.format("%d:%02d", hour, min)
    }

    /**
     * 指定した日付の通常の運行タイプを返します。
     * @param calendar 日付
     * @return 運行タイプ
     */
    fun getNormalScheduleType(calendar: Calendar): ScheduleType {
        val week = calendar.get(Calendar.DAY_OF_WEEK)
        return when (week) {
            Calendar.SATURDAY -> ScheduleType.SATURDAY
            Calendar.SUNDAY   -> ScheduleType.SUNDAY
            else              -> ScheduleType.WEEKDAY
        }
    }

    /**
     * 2つの日付が同じ日であればtrueを返します。
     * @param date1 日付1
     * @param date2 日付2
     * @return 同じ日付であればtrue
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        return DateUtils.truncate(date1, Calendar.DAY_OF_MONTH) == DateUtils.truncate(date2, Calendar.DAY_OF_MONTH)
    }
}
