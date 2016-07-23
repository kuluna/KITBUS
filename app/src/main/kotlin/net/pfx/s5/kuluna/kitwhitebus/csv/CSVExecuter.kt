package net.pfx.s5.kuluna.kitwhitebus.csv

import android.util.Log
import com.opencsv.CSVReader
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * KITBUSのCSVを読み込む共通クラス
 */
abstract class CSVExecuter(private val url: String) {
    lateinit var csv: List<Array<String>>
    /** CSVバージョン */
    val version: Int
        get() = csv[0][1].toInt()

    /**
     * CSVをダウンロードします。
     * @return ダウンロードに成功ならtrue
     */
    fun load(): Boolean {
        try {
            // HTTP通信の設定
            // タイムアウトは10秒
            val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.code() != 200) {
                Log.e("KITBUS", "時刻表URLのリクエストコードが${response.code()}を返しました")
                return false
            }
            // 時刻表データ(Shift-JISで読み込む)
            CSVReader(InputStreamReader(response.body().byteStream(), "shift-jis")).use {
                csv = it.readAll()
            }
            return true

        } catch (e: Exception) {
            Log.e("KITBUS", "CSV取得時にエラーが発生しました", e)
            return false
        }
    }

    /**
     * CSVからモデルオブジェクトを生成します。
     */
    fun save() {
        try {
            saveExecute(csv)
        } catch (e: Exception) {
            throw IOException()
        }
    }

    /**
     * CSVからモデルオブジェクトを生成させる処理をこのメソッドに実装します。
     * @param csv 改行で区切られたCSV
     */
    protected abstract fun saveExecute(csv: List<Array<String>>)
}
