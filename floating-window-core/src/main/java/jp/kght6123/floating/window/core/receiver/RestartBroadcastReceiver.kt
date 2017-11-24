package jp.kght6123.floating.window.core.receiver

import android.content.*
import android.util.Log
import android.widget.Toast
import android.content.Intent
import jp.kght6123.floating.window.core.FloatWindowMainActivity


/**
 * MultiFloatWindowApplicationServiceの自動再起動、関連アプリの追加・削除を通知する
 *
 * @author    kght6123
 * @copyright 2017/09/17 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class RestartBroadcastReceiver : BroadcastReceiver() {

    private val tag = this::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {

        //val myApp = (intent?.dataString == "package:" + context?.packageName)

        if(context == null || intent == null)
            return
        else if(Intent.ACTION_BOOT_COMPLETED == intent.action){
            Log.i(tag, "端末起動 packageName=${context.packageName} intent.dataString=${intent.dataString} ")
            startMainActivity(context)
        }
        else if (Intent.ACTION_PACKAGE_ADDED == intent.action
                && !intent.extras.getBoolean(Intent.EXTRA_REPLACING)) {
            Log.i(tag, "インストール packageName=${context.packageName} intent.dataString=${intent.dataString} ")
            startMainActivity(context)
        }
        else if (Intent.ACTION_PACKAGE_REMOVED == intent.action) {

            if (intent.extras.getBoolean(Intent.EXTRA_DATA_REMOVED)
                    && intent.extras.getBoolean(Intent.EXTRA_REPLACING)) {
                Log.i(tag, "アップデートのアンインストール packageName=${context.packageName} intent.dataString=${intent.dataString} ")
                startMainActivity(context)
            }
            if (!intent.extras.getBoolean(Intent.EXTRA_DATA_REMOVED)
                    && intent.extras.getBoolean(Intent.EXTRA_REPLACING)) {
                Log.i(tag, "更新 packageName=${context.packageName} intent.dataString=${intent.dataString} ")
                startMainActivity(context)
            }
        }
        else if (Intent.ACTION_PACKAGE_FULLY_REMOVED == intent.action) {
            Log.i(tag, "アンインストール packageName=${context.packageName} intent.dataString=${intent.dataString} ")
            startMainActivity(context)
        }
        Log.i(tag, "intent.action=${intent.action}")

        Toast.makeText(context, "intent.action=${intent.action}", Toast.LENGTH_SHORT).show()
    }

    private fun startMainActivity(context: Context)/* = launch(CommonPool)*/ {
        Log.i(tag,"MainActivity起動")

        val intent = Intent(context, FloatWindowMainActivity::class.java)
        intent.putExtra("foo", "bar")

        context.startActivity(intent)

//        val launcher = FloatWindowLauncher(context)
//
//        async(CommonPool){
//            delay(100)
//            launcher.bind()
//
//            delay(100)
//            launcher.hello()
//
//            delay(100)
//            launcher.unbind()
//
//        }.await()
    }
}