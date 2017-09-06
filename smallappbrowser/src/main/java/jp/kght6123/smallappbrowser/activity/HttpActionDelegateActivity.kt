package jp.kght6123.smallappbrowser.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import jp.kght6123.multiwindow.MultiFloatWindowApplication
import jp.kght6123.multiwindow.MultiFloatWindowApplicationActivity
import jp.kght6123.smallappbrowser.SmallBrowserApplicationService
import jp.kght6123.smallappbrowser.application.SharedDataApplication


/**
 * Activity経由でスモールブラウザを起動するためのデリゲーター
 *
 * Created by kght6123 on 2017/08/04.
 */
class HttpActionDelegateActivity : MultiFloatWindowApplicationActivity<SmallBrowserApplicationService>(SmallBrowserApplicationService::class.java) {

    private val initIntent by lazy {
        val intent = Intent()
        intent.data = Uri.parse("http://google.co.jp/")
        intent
    }

    override fun onCheckOverlayPermissionResult(result: Boolean) {
        if (result) {
            // FIXME 作成中・・・ブラウザを起動する、アプリ一覧から呼び出すため
            val application = this.application as SharedDataApplication

            startMultiFloatWindowService()

            openMultiFloatWindowView(++application.windowIndex, MultiFloatWindowApplication.MultiWindowOpenType.NEW)

            startMultiFloatWindowView(application.windowIndex, initIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val intent = Intent(intent)
//        intent.setClass(this, SmallBrowserApplication::class.java)
//
//        try {
//            SmallApplicationManager.startApplication(this, intent)
//        } catch (e: SmallAppNotFoundException) {
//            Toast.makeText(this, e.getMessage(),
//                    Toast.LENGTH_SHORT).show()
//        }
        finish()
    }
}