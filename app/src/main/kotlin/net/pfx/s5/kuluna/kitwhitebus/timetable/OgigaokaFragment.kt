package net.pfx.s5.kuluna.kitwhitebus.timetable

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.pfx.s5.kuluna.kitwhitebus.R
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.model.Timetable

/**
 *  扇が丘の今日の時刻表を表示するFragment
 */
class OgigaokaFragment: TimetableFragment() {

    override var resId: Int = R.string.ogigaoka

    override val timetables: List<Timetable>
        get() = Gson().fromJson(Preference(context).timetableOgigaokaSerialize, object : TypeToken<List<Timetable>>() {}.type)

    override val departure: String
        get() = Preference(context).pointOgigaoka!!

    override val arrival: String
        get() = Preference(context).pointYatsukaho!!
}
