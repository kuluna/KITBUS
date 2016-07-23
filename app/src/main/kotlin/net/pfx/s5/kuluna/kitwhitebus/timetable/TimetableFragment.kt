package net.pfx.s5.kuluna.kitwhitebus.timetable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_timetable.*
import kotlinx.android.synthetic.main.list_timetable.view.*
import net.pfx.s5.kuluna.kitwhitebus.R
import net.pfx.s5.kuluna.kitwhitebus.alarm.AlarmReceiver
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.model.Schedule
import net.pfx.s5.kuluna.kitwhitebus.model.ScheduleType
import net.pfx.s5.kuluna.kitwhitebus.model.Timetable
import net.pfx.s5.kuluna.kitwhitebus.util.TimeConvert
import java.lang.ref.WeakReference

/**
 * 今日の時刻表を表示するFragment
 */
abstract class TimetableFragment : Fragment() {
    /** 時刻表表示用Adapter */
    private lateinit var timetableAdapter: TimetableAdapter
    /** 毎分更新用Hander */
    private lateinit var update: UpdateHandler

    /** 扇が丘と八束穂と区別させるためのID */
    open var resId: Int
        get() = arguments.getInt("resId")
        set(id) {
            arguments = Bundle().apply { putInt("resId", id) }
        }

    /** 今の運航スケジュールタイプ */
    val currentScheduleType: ScheduleType
        get() {
            val schedules: List<Schedule> = Gson().fromJson(Preference(context).scheduleSerialize, object : TypeToken<List<Schedule>>() {}.type)
            // 変更スケジュールがないか確認する
            return schedules.firstOrNull { DateUtils.isToday(it.date.time) }
                    // あれば変更スケジュールを返す
                    ?.let { it.type }
                    // なければ通常のスケジュールを返す
                    ?: TimeConvert.currentWeek
        }

    /** 表示するViewを選択 */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_timetable, container, false)
    }

    /** Fragmentの準備 */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 時刻表を設定
        timetableAdapter = TimetableAdapter(context, timetables, rootlayout)
        // 乗車場所と降車場所を設定
        timetableAdapter.departure = departure
        timetableAdapter.arrival = arrival
        // RecyclerViewの設定
        list_timetable.run {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = timetableAdapter
        }

        // 更新用Handlerの設定
        update = UpdateHandler(resId, this)
    }

    /**  Fragmentの準備ができた後の処理 */
    override fun onResume() {
        super.onResume()
        // 乗車場所と降車場所を設定
        timetableAdapter.departure = departure
        timetableAdapter.arrival = arrival
        // 1分毎に更新処理を行うHandlerを起動
        update.sendEmptyMessage(resId)
    }

    /** Fragmentが一時停止した時の処理 */
    override fun onPause() {
        super.onPause()
        // Handlerを停止
        update.removeMessages(resId)
    }

    /** 時刻表の表示を更新 */
    fun update() {
        timetableAdapter.update(currentScheduleType)
        // 表示する時刻表がない場合は営業終了メッセージを表示
        if (timetableAdapter.itemCount == 0) {
            list_timetable.visibility = View.GONE
            text_finished.visibility = View.VISIBLE
        } else {
            list_timetable.visibility = View.VISIBLE
            text_finished.visibility = View.GONE
        }
    }

    /**  1分おきにデータを更新するHandler*/
    private class UpdateHandler(val resId: Int, fragment: TimetableFragment) : Handler() {
        private val reference = WeakReference(fragment)

        override fun handleMessage(msg: Message) {
            // 1分後に再びHandler(このメソッドの処理)を実行
            sendEmptyMessageDelayed(resId, TimeConvert.justZeroSecond)
            reference.get().update()
            Log.i("Timetable", "Updated.")
        }
    }

    /** 時刻表表示用Adapter */
    private class TimetableAdapter(val context: Context, val timetables: List<Timetable>, val rootLayout: View) : RecyclerView.Adapter<ViewHolder>() {
        /** 今日の残り時刻表 */
        var filteredTimetable = emptyList<Timetable>()
        /** 乗車場所 */
        var departure: String? = null
        /** 降車場所 */
        var arrival: String? = null

        /**
         * 表示する時刻表を更新します。
         * @param scheduleType 今日の運行スケジュールタイプ
         */
        fun update(scheduleType: ScheduleType) {
            val timetableCount = filteredTimetable.count()
            filteredTimetable = timetables.filter { it.scheduleType == scheduleType && it.timetables[departure]!! >= TimeConvert.currentTime }
            val newTimetableCount = filteredTimetable.count()

            if (timetableCount > newTimetableCount) {
                // リストが減った時に表示アニメーションを行う
                notifyItemRangeRemoved(0, timetableCount - newTimetableCount)
            } else {
                notifyDataSetChanged()
            }
        }

        /**  リストの個数 */
        override fun getItemCount(): Int {
            return filteredTimetable.count()
        }

        /** リストに表示するViewにデータを反映 */
        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val timetable = filteredTimetable[position]
            holder?.bind(timetable, departure!!, arrival!!)
        }

        /** リストに表示するViewの生成 */
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
            val view = LayoutInflater.from(context).inflate(R.layout.list_timetable, parent, false)
            return ViewHolder(context, view, rootLayout)
        }
    }

    /** リストに表示するView */
    private class ViewHolder(val context: Context, itemView: View, rootLayout: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var timetable: Timetable

        init {
            // タッチした時にアラームをセットする
            itemView.setOnClickListener {
                // AlarmReceiverに情報を送信
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    val data = Gson().toJson(timetable)
                    putExtra("timetable", data)
                    putExtra("alarm", true)
                }
                context.sendBroadcast(intent)

                // SnackBarで通知
                Snackbar.make(rootLayout, context.getText(R.string.set_alarm), Snackbar.LENGTH_LONG)
                        .setAction(context.getString(R.string.cancel)) {
                            //  取り消すボタンが押されたらアラームを解除する
                            val cancelIntent = Intent(context, AlarmReceiver::class.java).apply {
                                putExtra("timetable", "")
                                putExtra("alarm", false)
                            }
                            context.sendBroadcast(cancelIntent)
                        }
                        .show()
            }
        }

        /**
         * リストにデータを反映させます。
         * @param timetable 時刻表データ
         * @param departure 乗車場所
         * @param arrival 降車場所
         */
        fun bind(timetable: Timetable, departure: String, arrival: String) {
            this.timetable = timetable
            // 運行タイプによって色を変更
            when (this.timetable.scheduleType) {
                ScheduleType.WEEKDAY -> itemView.bar_saturday.visibility = View.GONE
                ScheduleType.SATURDAY -> itemView.bar_weekday.visibility = View.GONE
                else -> {}
            }
            // 時刻表データを表示
            itemView.card_busno.text = timetable.timetableName
            itemView.card_departure.text = TimeConvert.attachTime(timetable.timetables[departure]!!)
            itemView.card_arrive.text = TimeConvert.attachTime(timetable.timetables[arrival]!!)
            // 出発までの残り時間を表示
            itemView.card_alarm.text = (timetable.timetables[departure]!! - TimeConvert.currentTime).toString() + context.getString(R.string.min_after)
        }
    }

    /** 時刻表 */
    abstract val timetables: List<Timetable>
    /** 乗車場所 */
    abstract val departure: String
    /** 降車場所 */
    abstract val arrival: String
}
