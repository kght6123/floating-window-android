package jp.kght6123.multiwindow

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import jp.kght6123.multiwindowframework.*

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
    private val mSelfMessenger: Messenger = Messenger(Handler(ResponseHandler()))
    private var mServiceMessenger: Messenger? = null
    private val mConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceMessenger = null
        }
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if(binder != null) {
                mServiceMessenger = Messenger(binder)
                // 接続できた時に処理するコールバックが必要！！、onServiceConnectedはすぐに発火しない。
                this@MultiFloatWindowLauncher.onServiceConnected(name, binder)
            }
        }
    }

    init {
        Log.i(tag, "Start Service")
        context.startService(serviceIntent)
    }

    private fun sendMessage(command: MultiWindowControlCommand, index: Int, arg2: Int) {
        val message = Message.obtain(null, command.ordinal, index, arg2)
        message.replyTo = mSelfMessenger
        mServiceMessenger?.send(message)
    }
    private fun sendMessage(command: MultiWindowControlCommand, index: Int, openType: MultiWindowOpenType, data: Bundle?) {
        val message = Message.obtain(null, command.ordinal, index, openType.ordinal)
        message.replyTo = mSelfMessenger
        message.data = data
        mServiceMessenger?.send(message)
    }

    fun sendMessage(command: MultiWindowControlCommand, index: Int, arg2: Int, obj: Any) {
        val message = Message.obtain(null, command.ordinal, index, arg2, obj)
        message.replyTo = mSelfMessenger
        mServiceMessenger?.send(message)
    }

    fun bind() {
        if(!isBind()) {
            Log.i(tag, "Bind Service")
            context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }
    fun hello() {
        Log.i(tag, "Hello")
        sendMessage(MultiWindowControlCommand.HELLO, 0, MultiWindowOpenType.NEW, null)
    }
    fun openWindow(index: Int, openType: MultiWindowOpenType, serviceClass: Class<*>) {
        Log.i(tag, "Open Window serviceClass=${serviceClass.name}")
        val data = Bundle()
        data.putString(MultiWindowControlParam.APP_PACKAGE_NAME.name, serviceClass.`package`.name)
        data.putString(MultiWindowControlParam.APP_SERVICE_CLASS_NAME.name, serviceClass.name)

        sendMessage(MultiWindowControlCommand.OPEN, index, openType, data)
    }
    fun startWindow(index: Int, intent: Intent) {
        Log.i(tag, "Start Window")
        // WINDOW_INDEX渡しは不要、args1として渡せばOK、Intentを渡せるか検証のため
        intent.putExtra(MultiWindowControlParam.WINDOW_INDEX.name, index)
        sendMessage(MultiWindowControlCommand.START, index, 0, intent)
    }
    fun closeWindow(index: Int, intent: Intent) {
        Log.i(tag, "Close Window")
        sendMessage(MultiWindowControlCommand.CLOSE, index, 0, intent)
    }
    fun nextIndex(returnCommand: Int) {
        Log.i(tag, "Next Index")
        sendMessage(MultiWindowControlCommand.NEXT_INDEX, 0, returnCommand)
    }
    fun prevIndex(returnCommand: Int) {
        Log.i(tag, "Prev Index")
        sendMessage(MultiWindowControlCommand.PREV_INDEX, 0, returnCommand)
    }
    fun unbind() {
        if(isBind()) {
            Log.i(tag, "Unbind Service")
            context.unbindService(mConnection)
            mServiceMessenger = null
        }
    }
    fun stop() {
        Log.i(tag, "Stop Service")
        context.stopService(serviceIntent)
    }
    private fun isBind(): Boolean {
        return mServiceMessenger != null
    }
    open fun onServiceConnected(name: ComponentName?, binder: IBinder?) {

    }
    open fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {

    }
    open fun onFindPrevIndex(prevIndex: Int, returnCommand: Int) {

    }
    private inner class ResponseHandler : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            val command = MultiWindowControlCommand.values()[msg.what]
            return when (command) {
                MultiWindowControlCommand.NEXT_INDEX -> {
                    val nextIndex: Int = msg.arg1
                    onFindNextIndex(nextIndex, msg.arg2)
                    true
                }
                MultiWindowControlCommand.PREV_INDEX -> {
                    val prevIndex: Int = msg.arg1
                    onFindPrevIndex(prevIndex, msg.arg2)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }
}