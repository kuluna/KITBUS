package net.pfx.s5.kuluna.kitwhitebus.model

import android.content.Context
import net.pfx.s5.kuluna.kitwhitebus.R
import java.util.*

/**
 * 変更スケジュール
 */
data class Schedule(
        /** 日付 */
        val date: Date,
        /** 運行タイプ */
        val type: ScheduleType
)

/**
 * 運行スケジュールタイプ
 */
enum class ScheduleType {
    /** 平日スケジュール */
    WEEKDAY,
    /** 土曜スケジュール */
    SATURDAY,
    /** 運休 */
    SUNDAY;

    fun toString(context: Context): String {
        return when (this) {
            WEEKDAY -> context.getString(R.string.weekday)
            SATURDAY -> context.getString(R.string.saturday)
            SUNDAY -> context.getString(R.string.sunday)
        }
    }
}
