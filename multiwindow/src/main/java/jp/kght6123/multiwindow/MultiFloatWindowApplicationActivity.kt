package jp.kght6123.multiwindow

import android.app.Activity
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

    protected fun startMultiFloatWindowService() {
        Log.i(TAG, "Start Service")
        startService(serviceIntent)
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
    }
    protected fun openMultiFloatWindowView(index: Int) {
        Log.i(TAG, "Open Window")
        sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.OPEN, index)
    }
    protected fun startMultiFloatWindowView(index: Int, intent: Intent) {
        Log.i(TAG, "Start Window")
        // FIXME WINDOW_INDEX渡しは不要、args1として渡せばOK、Intentを渡せるか検証のため
        intent.putExtra(MultiFloatWindowApplication.MultiWindowControlParam.WINDOW_INDEX.name, index)
        sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.START, index, intent)
    }
    protected fun closeMultiFloatWindowView(index: Int, intent: Intent) {
        Log.i(TAG, "Close Window")
        sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.CLOSE, index, intent)
    }
    protected fun stopMultiFloatWindowService() {
        Log.i(TAG, "Stop Service")
        unbindService(mConnection)
        stopService(serviceIntent)
    }

    private fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int) {
        mService?.send(Message.obtain(null, command.ordinal, index, 0))
    }
    private fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int, obj: Any) {
        mService?.send(Message.obtain(null, command.ordinal, index, 0, obj))
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
                    REQUEST_CODE_SYSTEM_OVERLAY ->
                        // もう一度権限を確認して、権限があれば処理をする
                        judgeOverlayPermission()
                }
            }
            RESULT_CANCELED -> {
                Toast.makeText(applicationContext, "RESULT_CANCELED.", Toast.LENGTH_SHORT).show()
                when (requestCode) {
                    REQUEST_CODE_SYSTEM_OVERLAY ->
                        // もう一度権限を確認して、権限があれば処理をする
                        judgeOverlayPermission()
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