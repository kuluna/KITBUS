package net.pfx.s5.kuluna.kitwhitebus

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import net.pfx.s5.kuluna.kitwhitebus.model.Preference
import net.pfx.s5.kuluna.kitwhitebus.timetable.AllTimetableFragment
import net.pfx.s5.kuluna.kitwhitebus.timetable.OgigaokaFragment
import net.pfx.s5.kuluna.kitwhitebus.timetable.YatsukahoFragment

/**
 * 時刻表を表示するActivity
 */
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tabと密着させるためActionBarの影を消す
        supportActionBar?.elevation = 0f
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layout_tab.elevation = 4f
        }

        // TabとViewPagerの紐づけ
        layout_main.adapter = TimetablePagerAdapter(this, supportFragmentManager)
        layout_tab.setupWithViewPager(layout_main)
    }

    override fun onResume() {
        super.onResume()
        // CSVのリセットがかかった場合は終了
        if (Preference(this).timetableVersion < 0 || Preference(this).scheduleVersion < 0) {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_schedule -> startActivity(Intent(this, ScheduleActivity::class.java))
            R.id.menu_setting -> startActivity(Intent(this, PreferenceActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    /** ViewPager表示用Adapter*/
    private class TimetablePagerAdapter(context: Context, fm: FragmentManager): FragmentPagerAdapter(fm) {
        /** タブのタイトル */
        private val titles = arrayOf(context.getString(R.string.ogigaoka), context.getString(R.string.yatsukaho), context.getString(R.string.all), context.getString(R.string.schedule))

        /** ページ数 */
        override fun getCount(): Int = 3

        /** 各ページに表示するFragmentの設定 */
        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> OgigaokaFragment()
                1 -> YatsukahoFragment()
                2 -> AllTimetableFragment()
                else -> null
            }
        }

        /** タブに表示するタイトル */
        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }
}
