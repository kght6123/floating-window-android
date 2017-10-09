package jp.kght6123.multiwindow

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import jp.kght6123.multiwindowframework.MultiWindowControlCommand
import jp.kght6123.multiwindowframework.MultiWindowControlParam
import jp.kght6123.multiwindowframework.MultiWindowOpenType

/**
 * MultiFloatWindowApplicationを初期化し、起動／停止を行う
 *
 * Created by kght6123 on 2017/10/01.
 */
open class MultiFloatWindowLauncher(val context: Context) {
    private val tag = MultiFloatWindowLauncher::class.java.simpleName

    private val serviceIntent: Intent by lazy {
        Intent(context, MultiFloatWindowService::class.java)
    }
    private var mService : Messenger? = null
    private val mConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
        }
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if(binder != null) {
                mService = Messenger(binder)
                // FIXME 接続できた時に処理するコールバックが必要！！、onServiceConnectedはすぐに発火しない。
                this@MultiFloatWindowLauncher.onServiceConnected(name, binder)
            }
        }
    }

    init {
        Log.i(tag, "Start Service")
        context.startService(serviceIntent)
    }

    //private fun sendMessage(command: MultiWindowControlCommand, index: Int, arg2: Int) {
    //mService?.send(Message.obtain(null, command.ordinal, index, arg2))
    //}
    private fun sendMessage(command: MultiWindowControlCommand, index: Int, openType: MultiWindowOpenType) {
        mService?.send(Message.obtain(null, command.ordinal, index, openType.ordinal))
    }

    fun sendMessage(command: MultiWindowControlCommand, index: Int, arg2: Int, obj: Any) {
        mService?.send(Message.obtain(null, command.ordinal, index, arg2, obj))
    }

    fun bind() {
        if(!isBind()) {
            Log.i(tag, "Bind Service")
            context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }
    fun hello() {
        Log.i(tag, "Hello")
        sendMessage(MultiWindowControlCommand.HELLO, 0, MultiWindowOpenType.NEW)
    }
    fun openWindow(index: Int, openType: MultiWindowOpenType) {
        Log.i(tag, "Open Window")
        sendMessage(MultiWindowControlCommand.OPEN, index, openType)
    }
    fun startWindow(index: Int, intent: Intent) {
        Log.i(tag, "Start Window")
        // FIXME WINDOW_INDEX渡しは不要、args1として渡せばOK、Intentを渡せるか検証のため
        intent.putExtra(MultiWindowControlParam.WINDOW_INDEX.name, index)
        sendMessage(MultiWindowControlCommand.START, index, 0, intent)
    }
    fun closeWindow(index: Int, intent: Intent) {
        Log.i(tag, "Close Window")
        sendMessage(MultiWindowControlCommand.CLOSE, index, 0, intent)
    }
    fun unbind() {
        if(isBind()) {
            Log.i(tag, "Unbind Service")
            context.unbindService(mConnection)
            mService = null
        }
    }
    fun stop() {
        Log.i(tag, "Stop Service")
        context.stopService(serviceIntent)
    }
    private fun isBind(): Boolean {
        return mService != null
    }
    open fun onServiceConnected(name: ComponentName?, binder: IBinder?) {

    }
}