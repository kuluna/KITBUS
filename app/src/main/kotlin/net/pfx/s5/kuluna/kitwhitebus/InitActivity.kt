package net.pfx.s5.kuluna.kitwhitebus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_init.*
import net.pfx.s5.kuluna.kitwhitebus.csv.CSVLoader
import net.pfx.s5.kuluna.kitwhitebus.model.Preference

/**
 * 時刻表を最新版にするActivity
 */
class InitActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Bundle> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)
        animation()

        // 時刻表の非同期更新を行う
        supportLoaderManager.initLoader(0, null, this)
    }

    /** 非同期準備 */
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Bundle>? {
        return CSVLoader(this).apply { forceLoad() }
    }

    /** 非同期処理完了後処理 */
    override fun onLoadFinished(loader: Loader<Bundle>?, result: Bundle?) {
        // CSVのパースエラーの場合は大学へ連絡
        if (result?.getBoolean("parseError", false) ?: false) {
            startActivity(Intent(this, CSVFeedbackActivity::class.java).apply { putExtra("parseError", true) })
            finish()
            return
        }

        if (result?.getBoolean("csv", false) ?: false) {
            // CSVの更新が完了したら1.5秒後にMainActivityへ遷移
            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1500)
        } else {

            if (Preference(this).debugCsv) {
                // もしデバッグURLの設定ミスによるエラーならデバッグを解除する
                Preference(this).debugCsv = false
                Toast.makeText(this, getString(R.string.error_debug), Toast.LENGTH_LONG).show()
            } else {
                // データがない場合はアプリを実行できないのでここで終了
                Toast.makeText(this, getString(R.string.error_update), Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }

    // 使わない
    override fun onLoaderReset(loader: Loader<Bundle>?) {
    }

    /**
     *  バスとKITカラーをアニメーションさせます
     */
    private fun animation() {
        // バスをズームインさせるアニメーションの設定
        image_bus.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoomin))
        // KITカラーを回転するアニメーションの設定
        val rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        rotateAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                // アニメーションが終了したら一定のディレイを設けて再アニメーション
                animation?.startOffset = 500
            }
        })

        // アニメーション開始
        view_kitblue.startAnimation(rotateAnim)
        view_kitred.startAnimation(rotateAnim)
        view_kitgreen.startAnimation(rotateAnim)
    }
}
