package net.pfx.s5.kuluna.kitwhitebus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_feedback.*

/**
 * CSVのエラーを大学に報告するためのActivity
 */
class CSVFeedbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // 他所から飛んできた場合はInitActivityへご案内
        if (!intent.hasExtra("parseError")) {
            startActivity(Intent(this, InitActivity::class.java))
            finish()
            return
        }

        // フィードバックを送るためにメールIntentを投げる
        button_do_send.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(getString(R.string.mailto))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_body))
            }
            startActivity(intent)
            finish()
        }
        // 送らないならそのまま終了する
        button_donot_send.setOnClickListener { finish() }

        // 設定画面を開く
        button_open_preference.setOnClickListener {
            startActivity(Intent(this, PreferenceActivity::class.java))
            finish()
        }
    }
}
