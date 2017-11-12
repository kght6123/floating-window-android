package jp.kght6123.smallappbrowser.activity

import android.content.Intent
import android.net.Uri
import jp.kght6123.multiwindowframework.MultiFloatWindowBaseActivity
import jp.kght6123.multiwindowframework.MultiWindowOpenType
import jp.kght6123.smallappbrowser.SmallBrowserApplication

/**
 * Activity経由でスモールブラウザを起動するためのデリゲーター
 *
 * @author    kght6123
 * @copyright 2017/08/04 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class HttpActionDelegateActivity : MultiFloatWindowBaseActivity() {

    private val initIntent by lazy {
        val intent = Intent()
        intent.data = Uri.parse("http://google.co.jp/")
        intent
    }

    override fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {
        super.onFindNextIndex(nextIndex, returnCommand)

        if (returnCommand == 0) {
            // ブラウザを起動する
            launcher.openWindow(nextIndex, MultiWindowOpenType.NEW, SmallBrowserApplication::class.java)

            if(intent.dataString == null) {
                launcher.startWindow(nextIndex, initIntent) // アプリ一覧から呼び出すとき
            } else {
                launcher.startWindow(nextIndex, intent) // 他のアプリから呼び出されるとき
            }
            finish()
        }
    }

    override fun onServiceConnected() {
        launcher.nextIndex(0)
    }
}