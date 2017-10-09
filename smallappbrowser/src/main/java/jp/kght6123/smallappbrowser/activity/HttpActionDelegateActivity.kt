package jp.kght6123.smallappbrowser.activity

import android.content.Intent
import android.net.Uri
import jp.kght6123.multiwindow.MultiFloatWindowBaseActivity
import jp.kght6123.multiwindowframework.MultiWindowOpenType
import jp.kght6123.smallappbrowser.application.SharedDataApplication


/**
 * Activity経由でスモールブラウザを起動するためのデリゲーター
 *
 * Created by kght6123 on 2017/08/04.
 */
class HttpActionDelegateActivity : MultiFloatWindowBaseActivity() {

    private val initIntent by lazy {
        val intent = Intent()
        intent.data = Uri.parse("http://google.co.jp/")
        intent
    }

    override fun onCheckOverlayPermissionResult(result: Boolean) {
        if (result) {
            // FIXME 作成中・・・ブラウザを起動する、アプリ一覧から呼び出すため
            val application = this.application as SharedDataApplication

            launcher.openWindow(++application.windowIndex, MultiWindowOpenType.NEW)
            launcher.startWindow(application.windowIndex, initIntent)

            finish()
        }
    }
}