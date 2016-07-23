package net.pfx.s5.kuluna.kitwhitebus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.android.synthetic.main.activity_schedule.*
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.model.Schedule
import net.pfx.s5.kuluna.kitwhitebus.model.ScheduleType
import net.pfx.s5.kuluna.kitwhitebus.util.TimeConvert
import java.util.*

/**
 * 変更のあるスケジュール一覧を表示するActivity
 */
class ScheduleActivity : AppCompatActivity() {

    companion object {
        /**
         * 日付にあったスケジュールタイプを返します。
         * @param schedules 変更スケジュール一覧
         * @param calendar 日付
         * @return スケジュールタイプ
         */
        fun calcScheduleType(schedules: List<Schedule>, calendar: Calendar): ScheduleType {
            return schedules.firstOrNull { TimeConvert.isSameDay(it.date, calendar.time) }
                    // あれば変更スケジュールを返す
                    ?.let { it.type }
                    // なければその日のスケジュールを返す
                    ?: TimeConvert.getNormalScheduleType(calendar)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // カレンダーの日付フォーマットを設定
        calendar.setTitleFormatter { DateFormat.format(getString(R.string.cal_month_format), it.date) }

        // 運行スケジュール取得
        val schedules: List<Schedule> = Gson().fromJson(Preference(this).scheduleSerialize, object : TypeToken<List<Schedule>>() {}.type)
        // カレンダーに表示
        addDeco(schedules)
        calendar.setOnDateChangedListener { materialCalendarView, day, selected ->
            showCard(schedules, day.calendar)
        }
        // カレンダーを今日にセット
        val now = Calendar.getInstance()
        calendar.setDateSelected(now, true)
        showCard(schedules, now)

        // カレンダーボタンが押されたら予定の登録をする
        button_calendar.setOnClickListener {
            val setCal = calendar.selectedDate.calendar
            sendCalendar(setCal, calcScheduleType(schedules, setCal))
        }

        // 閉じるボタンが押されたらカードの表示を消す
        button_close.setOnClickListener {
            card_schedule.visibility = View.GONE
            button_calendar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // カレンダーの大きさを設定
        val size = resources.getDimensionPixelSize(R.dimen.calendar_tilesize)
        calendar.tileSize = size
    }

    /**
     * スケジュールに合わせて運行タイプをカレンダーに表示する
     * @param schedules 運行スケジュール
     */
    private fun addDeco(schedules: List<Schedule>) {
        // 平日
        calendar.addDecorator(ScheduleDecorator(schedules, ScheduleType.WEEKDAY, Color.BLACK))
        // 土曜
        calendar.addDecorator(ScheduleDecorator(schedules, ScheduleType.SATURDAY, Color.BLUE))
        // 運休
        calendar.addDecorator(ScheduleDecorator(schedules, ScheduleType.SUNDAY, Color.RED))
    }

    /**
     * a
     * @param schedules 運行スケジュール
     * @param calendar Calendarクラス
     */
    private fun showCard(schedules: List<Schedule>, calendar: Calendar) {
        val format = DateFormat.getDateFormat(this)
        text_schedule.text = getString(R.string.a_is_b, format.format(calendar.time), calcScheduleType(schedules, calendar).toString(this))
        card_schedule.visibility = View.VISIBLE
        button_calendar.visibility = View.VISIBLE
    }

    /**
     * スケジュールをカレンダーに登録します。
     * @param calendar 日付
     * @param scheduleType 運行タイプ
     */
    private fun sendCalendar(calendar: Calendar, scheduleType: ScheduleType) {
        val intent = Intent(Intent.ACTION_EDIT).apply {
            type = "vnd.android.cursor.item/event"
            // 開始日時
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
            // 終了日時
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis)
            // 終日
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            // タイトル
            putExtra(CalendarContract.Events.TITLE, getString(R.string.app_name) + " " + scheduleType.toString(this@ScheduleActivity))
        }
        startActivity(intent)
    }

    /**
     * 日付下に運行スケジュールタイプを表示するためのデコレーション設定クラス
     */
    private class ScheduleDecorator(val schedules: List<Schedule>, val decorateScheduleType: ScheduleType, val decorateColor: Int) : DayViewDecorator {
        /** 日付がデコ対象か判定する */
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return calcScheduleType(schedules, day.calendar) == decorateScheduleType
        }

        /** デコ対象の日付に色をつける */
        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(4f, decorateColor))
        }
    }
}
