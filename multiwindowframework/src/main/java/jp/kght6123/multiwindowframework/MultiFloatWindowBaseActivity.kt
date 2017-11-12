package jp.kght6123.multiwindowframework

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast

/**
 * スモールブラウザの起動／停止とOverlay権限のチェック結果イベント発行を行う、Activity
 *
 * Created by kght6123 on 2017/08/22.
 */
abstract class MultiFloatWindowBaseActivity : Activity() {

    companion object {
        private val REQUEST_CODE_SYSTEM_OVERLAY :Int = 1234
        private val REQUEST_CODE_PICK_APPWIDGET :Int = 5678
        private val REQUEST_CODE_CONFIGURE_APPWIDGET :Int = 9012
    }

    lateinit protected var launcher : MultiFloatWindowLauncher

    private val appWidgetHost by lazy {
        AppWidgetHost(this, MultiFloatWindowConstants.APP_WIDGET_HOST_ID)
    }
    private var appWidgetIndex :Int = -1

    private var permission = false
    private var serviceConnected = false

    protected fun startAppWidgetWindowView(index: Int) {
        // ウィジェット毎ユニークIDを取得
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        this.appWidgetIndex = index

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

    open fun onCheckOverlayPermissionResult(result: Boolean) {

    }

    private fun judgeOverlayPermission() {
        if (checkOverlayPermission()) {
            //Toast.makeText(applicationContext, "OverlayPermission OK.", Toast.LENGTH_SHORT).show()
            this.permission = true
            onCheckOverlayPermissionResult(this.permission)
            onChangePreparationStatus(this.permission, this.serviceConnected)

        } else {
            Toast.makeText(applicationContext, "OverlayPermission NG.", Toast.LENGTH_SHORT).show()
            onCheckOverlayPermissionResult(this.permission)
            requestOverlayPermission()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.launcher = object : MultiFloatWindowLauncher(this){
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                super.onServiceConnected(name, binder)
                this@MultiFloatWindowBaseActivity.onLauncherServiceConnected(name, binder)
            }
            override fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {
                super.onFindNextIndex(nextIndex, returnCommand)
                this@MultiFloatWindowBaseActivity.onFindNextIndex(nextIndex, returnCommand)
            }
            override fun onFindPrevIndex(prevIndex: Int, returnCommand: Int) {
                super.onFindPrevIndex(prevIndex, returnCommand)
                this@MultiFloatWindowBaseActivity.onFindPrevIndex(prevIndex, returnCommand)
            }
        }
        this.launcher.bind()// 必ずonCreateに！
    }
    override fun onDestroy() {
        super.onDestroy()
        launcher.unbind()// 必ずonDestroyに！
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                //Toast.makeText(applicationContext, "RESULT_OK.", Toast.LENGTH_SHORT).show()
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

                        launcher.sendMessage(MultiWindowControlCommand.ADD_APP_WIDGET, appWidgetIndex, appWidgetId, data)
                    }
                }
            }
            RESULT_CANCELED -> {
                //Toast.makeText(applicationContext, "RESULT_CANCELED.", Toast.LENGTH_SHORT).show()
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
    override fun onStart() {
        super.onStart()
        judgeOverlayPermission()
    }

    open fun onLauncherServiceConnected(name: ComponentName?, binder: IBinder?) {
        serviceConnected = true
        //Toast.makeText(applicationContext, "Service Connected OK.", Toast.LENGTH_SHORT).show()

        onChangePreparationStatus(this.permission, this.serviceConnected)
    }
    open fun onChangePreparationStatus(permission: Boolean, serviceConnected: Boolean) {
        if(permission && serviceConnected) {
            launcher.hello()
            onServiceConnected()
        }
    }
    open fun onServiceConnected() {

    }
    open fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {

    }
    open fun onFindPrevIndex(prevIndex: Int, returnCommand: Int) {

    }
}