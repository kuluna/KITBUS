package net.pfx.s5.kuluna.kitwhitebus.timetable

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_alltimetable.*
import net.pfx.s5.kuluna.kitwhitebus.R
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.model.ScheduleType
import net.pfx.s5.kuluna.kitwhitebus.model.Timetable
import net.pfx.s5.kuluna.kitwhitebus.util.TimeConvert
import java.util.*

/**
 * 全時刻表を表示するFragment
 */
class AllTimetableFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_alltimetable, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 英語の場合、「xx号館」の説明文を表示
        if (Locale.getDefault() == Locale.JAPAN) {
            text_explain_buildno.visibility = View.GONE
        }

        val pref = Preference(context)
        val o: List<Timetable> = Gson().fromJson(pref.timetableOgigaokaSerialize, object : TypeToken<List<Timetable>>() {}.type)
        val y: List<Timetable> = Gson().fromJson(pref.timetableYatsukahoSerialize, object : TypeToken<List<Timetable>>() {}.type)

        // 扇が丘と八束穂、それぞれ平日と土曜日のグループに分ける
        val ogigaoka = o.groupBy { it.scheduleType }
        val yatsukaho = y.groupBy { it.scheduleType }

        // 扇が丘平日
        cardtable_otoy_week.run {
            findViewById(R.id.bar_saturday).visibility = View.GONE
            (findViewById(R.id.table_title) as TextView).text = "${getString(R.string.ogigaoka)} ${getString(R.string.weekday)}"
            val table = findViewById(R.id.table_timetables) as TableLayout
            createTableRows(ogigaoka[ScheduleType.WEEKDAY]!!).forEach { table.addView(it) }
        }
        // 八束穂平日
        cardtable_ytoo_week.run {
            findViewById(R.id.bar_saturday).visibility = View.GONE
            (findViewById(R.id.table_title) as TextView).text = "${getString(R.string.yatsukaho)} ${getString(R.string.weekday)}"
            val table = findViewById(R.id.table_timetables) as TableLayout
            createTableRows(yatsukaho[ScheduleType.WEEKDAY]!!).forEach { table.addView(it) }
        }
        // 扇が丘土曜
        cardtable_otoy_saturday.run {
            findViewById(R.id.bar_weekday).visibility = View.GONE
            (findViewById(R.id.table_title) as TextView).text = "${getString(R.string.ogigaoka)} ${getString(R.string.saturday)}"
            val table = findViewById(R.id.table_timetables) as TableLayout
            createTableRows(ogigaoka[ScheduleType.SATURDAY]!!).forEach { table.addView(it) }
        }
        // 八束穂平日
        cardtable_ytoo_saturday.run {
            findViewById(R.id.bar_weekday).visibility = View.GONE
            (findViewById(R.id.table_title) as TextView).text = "${getString(R.string.yatsukaho)} ${getString(R.string.saturday)}"
            val table = findViewById(R.id.table_timetables) as TableLayout
            createTableRows(yatsukaho[ScheduleType.SATURDAY]!!).forEach { table.addView(it) }
        }
    }

    /**
     * 時刻表データからテーブルセルを生成します。
     * @param timetables 時刻表データ
     * @return TableRow
     */
    private fun createTableRows(timetables: List<Timetable>): List<TableRow> {
        val rows = ArrayList<TableRow>()

        // テーブルのヘッダー行
        val headerRow = TableRow(context).apply {
            // 便名
            addView(createTableTextView(context, getString(R.string.busid), Gravity.CENTER_HORIZONTAL))
            // 乗車・降車場所
            timetables.first().points.forEach { addView(createTableTextView(context, it, Gravity.CENTER_HORIZONTAL)) }
        }

        // テーブルのデータ行
        val dataRows = timetables.mapIndexed { index, timetable -> TableRow(context).apply {
            setPadding(0, 8, 0, 8)
            // テーブルの各行に縞々模様をつける
            if (index % 2 == 0) {
                setBackgroundColor(ContextCompat.getColor(context, R.color.table_gray))
            }

            // 便名を登録
            addView(createTableTextView(context, timetable.timetableName, GravityCompat.END))
            // バス到着順にテーブルへ登録
            for (buildNo in timetable.points) {
                // 時刻フォーマットに変換して登録
                val timetableText = TimeConvert.attachTime(timetable.timetables[buildNo]!!)
                addView(createTableTextView(context, timetableText, GravityCompat.END))
            }
        }}

        rows.add(headerRow)
        rows.addAll(dataRows)
        return rows
    }

    /**
     * テーブルに表示するために修飾したTextViewを生成します。
     * @param context Context
     * @param setText 表示するテキスト
     * @param setGravity テーブルセル内の表示位置
     * @return TextView
     */
    private fun createTableTextView(context: Context, setText: String, setGravity: Int): TextView {
        return TextView(context).apply {
            textSize = 18f
            gravity = setGravity
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            text = setText
        }
    }
}
