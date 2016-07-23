package net.pfx.s5.kuluna.kitwhitebus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v14.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.ListPreference
import net.pfx.s5.kuluna.kitwhitebus.model.Preference

class PreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, KITBusPreferenceFragment(), "preference").commit()
    }

    class KITBusPreferenceFragment : PreferenceFragment() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preference)

            // 八束穂の乗車場所一覧を設定できるようにする
            val pointsYatsukaho = Preference(activity).pointsYatsukaho
            val yatsukahoPreference = findPreference(Preference.POINT_YATSUKAHO) as ListPreference
            yatsukahoPreference.entries = pointsYatsukaho.take(pointsYatsukaho.size - 1).toTypedArray()
            yatsukahoPreference.entryValues = yatsukahoPreference.entries

            // 再読み込みが押された時にCSVバージョンをリセットして次回起動時に強制アップデートさせる
            findPreference("debug_reset_csv").setOnPreferenceClickListener {
                Preference(activity).timetableVersion = -1
                Preference(activity).scheduleVersion = -1
                // PreferenceActivityを終了
                activity.finish()
                // アプリを再起動
                Handler().postDelayed({ startActivity(Intent(activity, InitActivity::class.java)) }, 100)

                true
            }
        }
    }
}
