package jp.kght6123.multiwindow

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.provider.Settings
import android.util.Log
import android.widget.Toast

/**
 * スモールブラウザの起動／停止とOverlay権限のチェック結果イベント発行を行う、Activity
 *
 * Created by kght6123 on 2017/08/22.
 */
abstract class MultiFloatWindowApplicationActivity<in S: MultiFloatWindowApplication>(serviceClass: Class<S>) : Activity() {

    companion object {
        private val REQUEST_CODE_SYSTEM_OVERLAY :Int = 1234
        private val REQUEST_CODE_PICK_APPWIDGET :Int = 5678
        private val REQUEST_CODE_CONFIGURE_APPWIDGET :Int = 9012
    }

    private val TAG = MultiFloatWindowApplicationActivity::class.java.simpleName

    private val serviceIntent: Intent by lazy {
        Intent(this, serviceClass)
    }
    private var mService : Messenger? = null
    private val mConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
        }
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if(binder != null) {
                mService = Messenger(binder)
            }
        }
    }

    private val appWidgetHost by lazy {
        AppWidgetHost(this, MultiFloatWindowApplication.APP_WIDGET_HOST_ID)
    }
    private var appWidgetIndex :Int = -1

    protected fun startMultiFloatWindowService() {
        Log.i(TAG, "Start Service")
        startService(serviceIntent)
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
    }
    protected fun openMultiFloatWindowView(index: Int, openType: MultiFloatWindowApplication.MultiWindowOpenType) {
        Log.i(TAG, "Open Window")
        sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.OPEN, index, openType)
    }
    protected fun startMultiFloatWindowView(index: Int, intent: Intent) {
        Log.i(TAG, "Start Window")
        // FIXME WINDOW_INDEX渡しは不要、args1として渡せばOK、Intentを渡せるか検証のため
        intent.putExtra(MultiFloatWindowApplication.MultiWindowControlParam.WINDOW_INDEX.name, index)
        sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.START, index, 0, intent)
    }
    protected fun closeMultiFloatWindowView(index: Int, intent: Intent) {
        Log.i(TAG, "Close Window")
        sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.CLOSE, index, 0, intent)
    }
    protected fun stopMultiFloatWindowService() {
        Log.i(TAG, "Stop Service")
        unbindService(mConnection)
        stopService(serviceIntent)
    }

    protected fun startAppWidgetWindowView(index: Int) {
        // ウィジェット毎ユニークIDを取得
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        appWidgetIndex = index

        val appWidgetProviderInfoList = ArrayList<AppWidgetProviderInfo>()
        val bundleList = ArrayList<Bundle>()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra("EXTRA_APPWIDGET_INDEX", index)
                .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, appWidgetProviderInfoList)
                .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, bundleList)

        // ウィジェット一覧表示
        startActivityForResult(intent, REQUEST_CODE_PICK_APPWIDGET)
    }

    private fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int, arg2: Int) {
        mService?.send(Message.obtain(null, command.ordinal, index, arg2))
    }
    private fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int, openType: MultiFloatWindowApplication.MultiWindowOpenType) {
        mService?.send(Message.obtain(null, command.ordinal, index, openType.ordinal))
    }
    private fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int, arg2: Int, obj: Any) {
        mService?.send(Message.obtain(null, command.ordinal, index, arg2, obj))
    }

    abstract fun onCheckOverlayPermissionResult(result: Boolean)

    private fun judgeOverlayPermission() {
        if (checkOverlayPermission()) {
            Toast.makeText(applicationContext, "OverlayPermission OK.", Toast.LENGTH_SHORT).show()
            onCheckOverlayPermissionResult(true)

        } else {
            Toast.makeText(applicationContext, "OverlayPermission NG.", Toast.LENGTH_SHORT).show()
            onCheckOverlayPermissionResult(false)
            requestOverlayPermission()

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        judgeOverlayPermission()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                Toast.makeText(applicationContext, "RESULT_OK.", Toast.LENGTH_SHORT).show()
                when (requestCode) {
                    REQUEST_CODE_SYSTEM_OVERLAY -> {
                        // もう一度権限を確認して、権限があれば処理をする
                        judgeOverlayPermission()
                    }
                    REQUEST_CODE_PICK_APPWIDGET -> if(data != null) {
                        val appWidgetId: Int =
                                data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        val appWidgetProviderInfo =
                                AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)

                        if (appWidgetProviderInfo.configure != null) {
                            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                                    .setComponent(appWidgetProviderInfo.configure)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            startActivityForResult(intent, REQUEST_CODE_CONFIGURE_APPWIDGET)
                        } else {
                            onActivityResult(REQUEST_CODE_CONFIGURE_APPWIDGET, Activity.RESULT_OK, data)
                        }
                    }
                    REQUEST_CODE_CONFIGURE_APPWIDGET -> if(data != null) {
                        val appWidgetId: Int =
                                data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        val appWidgetIndex: Int = data.getIntExtra("EXTRA_APPWIDGET_INDEX", appWidgetIndex)

                        sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.ADD_APP_WIDGET, appWidgetIndex, appWidgetId, data)
                    }
                }
            }
            RESULT_CANCELED -> {
                Toast.makeText(applicationContext, "RESULT_CANCELED.", Toast.LENGTH_SHORT).show()
                when (requestCode) {
                    REQUEST_CODE_SYSTEM_OVERLAY -> {
                        // もう一度権限を確認して、権限があれば処理をする
                        judgeOverlayPermission()
                    }
                    REQUEST_CODE_PICK_APPWIDGET, REQUEST_CODE_CONFIGURE_APPWIDGET -> {
                        if (data != null) {
                            val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                            if (appWidgetId != -1)
                            // appWidgetIdを削除
                                appWidgetHost.deleteAppWidgetId(appWidgetId)
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }
    private fun requestOverlayPermission() {
        val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")) // packageNameは、getPackageName()を呼び出している
        this.startActivityForResult(intent, REQUEST_CODE_SYSTEM_OVERLAY)
    }
}