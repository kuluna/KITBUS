package net.pfx.s5.kuluna.kitwhitebus.model

import android.content.Context
import net.pfx.s5.kuluna.kitwhitebus.R

data class Timetable(
        val scheduleType: ScheduleType,
        val timetableName: String,
        val departure: Departure,
        val points: List<String>,
        val timetables: Map<String, Int>
) {
    /** 出発時間 */
    val departureTime: Int?
        get() = timetables[points.first()]
}

enum class Departure {
    Ogigaoka, Yatsukaho;

    fun toString(context: Context): String {
        return when (this) {
            Ogigaoka -> context.getString(R.string.ogigaoka)
            Yatsukaho -> context.getString(R.string.yatsukaho)
        }
    }
}
