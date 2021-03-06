package jp.kght6123.floating.window.core

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.widget.Toast
import jp.kght6123.floating.window.framework.*
import jp.kght6123.floating.window.framework.MultiFloatWindowConstants.Companion.API_VERSION
import kotlin.collections.ArrayList

/**
 * マルチウィンドウアプリケーションの本体サービス
 *
 * @author    kght6123
 * @copyright 2017/09/10 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowService : Service() {

    private val manager: FloatWindowManager by lazy { FloatWindowManager(applicationContext) }

    private val callback: Handler.Callback by lazy {
        Handler.Callback { msg ->
            if(msg != null) {
                val command = MultiWindowControlCommand.values()[msg.what]
                when (command) {
                    MultiWindowControlCommand.HELLO -> {
                        Toast.makeText(applicationContext, "hello!! multi window framework ${API_VERSION.name}.", Toast.LENGTH_SHORT).show()
                        manager // startWindow Manager.
                        true
                    }
                    MultiWindowControlCommand.OPEN -> {
                        val packageName = msg.data.getString(MultiWindowControlParam.APP_PACKAGE_NAME.name)
                        val serviceClassName = msg.data.getString(MultiWindowControlParam.APP_SERVICE_CLASS_NAME.name)
                        val openType = MultiWindowOpenType.values()[msg.arg2]
                        when (openType) {
                            MultiWindowOpenType.NEW -> {
                                manager.openWindow(msg.arg1, packageName, serviceClassName,false, MultiWindowMetaDataName.getDefaultMap())
                            }
                            MultiWindowOpenType.UPDATE -> {
                                if(manager.factoryMap.containsKey(msg.arg1))
                                    manager.openWindow(msg.arg1, packageName, serviceClassName,true, MultiWindowMetaDataName.getDefaultMap())
                                else
                                    manager.openWindow(msg.arg1, packageName, serviceClassName,false, MultiWindowMetaDataName.getDefaultMap())
                            }
                        }
                        true
                    }
                    MultiWindowControlCommand.START -> {
                        val intent = msg.obj as Intent
                        val index = intent.getIntExtra(MultiWindowControlParam.WINDOW_INDEX.name, 0)
                        manager.factoryMap.getValue(index).start(intent)
                        true
                    }
                    MultiWindowControlCommand.UPDATE -> {
                        val intent = msg.obj as Intent
                        val index = intent.getIntExtra(MultiWindowControlParam.WINDOW_INDEX.name, -1)
                        val classNames = intent.getStringArrayExtra(MultiWindowControlParam.WINDOW_CLASS_NAMES.name)
                        if (index != -1){
                            // indexで更新
                            manager.factoryMap.getValue(index).update(intent, index, MultiWindowUpdatePosition.INDEX.name)

                        } else if (classNames != null && classNames.isNotEmpty()){
                            // classNamesで更新
                            val targetFactoryList = ArrayList<Map.Entry<Int, FloatWindowApplication.MultiFloatWindowFactory>>()
                            classNames.forEach { className ->
                                manager.factoryMap.forEach { entry ->
                                    if(entry.value.classObj.name == className) {
                                        targetFactoryList.add(entry)
                                    }
                                }
                            }
                            targetFactoryList.forEachIndexed { factoryIndex, entry ->
                                when (factoryIndex) {
                                    0 ->
                                        entry.value.update(intent, entry.key, MultiWindowUpdatePosition.FIRST.name)
                                    targetFactoryList.size - 1 ->
                                        entry.value.update(intent, entry.key, MultiWindowUpdatePosition.LAST.name)
                                    else ->
                                        entry.value.update(intent, entry.key, MultiWindowUpdatePosition.MIDDLE.name)
                                }
                            }
                        }
                        true
                    }
                    MultiWindowControlCommand.NEXT_INDEX -> {
                        msg.replyTo.send(Message.obtain(null, MultiWindowControlCommand.NEXT_INDEX.ordinal, manager.nextIndex(), msg.arg2, null))
                        true
                    }
                    MultiWindowControlCommand.PREV_INDEX -> {
                        msg.replyTo.send(Message.obtain(null, MultiWindowControlCommand.PREV_INDEX.ordinal, manager.nextIndex()-1, msg.arg2, null))
                        true
                    }
                    MultiWindowControlCommand.CLOSE -> {
                        manager.remove(msg.arg1, true)
                        true
                    }
//                        MultiWindowControlCommand.ADD_REMOTE_VIEWS -> {
//                            val remoteWindowViews =
//                                    msg.data.getParcelable<RemoteViews>(MultiWindowControlParam.REMOTE_WINDOW_VIEWS.key)
//                            val remoteMiniViews =
//                                    msg.data.getParcelable<RemoteViews>(MultiWindowControlParam.REMOTE_MINI_VIEWS.key)
//
//                            if(manager.factoryMap.containsKey(msg.arg1))
//                                manager.openRemoteWindow(msg.arg1, remoteWindowViews, remoteMiniViews,true)
//                            else
//                                manager.openRemoteWindow(msg.arg1, remoteWindowViews, remoteMiniViews,false)
//                        }
                    MultiWindowControlCommand.ADD_APP_WIDGET -> {
                        if(manager.factoryMap.containsKey(msg.arg1))
                            manager.openAppWidgetWindow(msg.arg1, msg.arg2,true)
                        else
                            manager.openAppWidgetWindow(msg.arg1, msg.arg2,false)

                        true
                    }
                    else -> {
                        false
                    }
                }
            } else {
                false
            }
        }
    }
    private val handler: Handler by lazy { Handler(callback) }
    private val mMessenger by lazy { Messenger(handler) }

    override fun onBind(intent: Intent?): IBinder {
        return mMessenger.binder
    }
    override fun onDestroy() {
        finish()
    }
    private fun finish() {
        manager.finish()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notificationSettings = onCreateNotificationSettings()
        // 前面で起動する
        val notification = Notification.Builder(applicationContext)
                .setContentTitle(notificationSettings.title)
                .setContentText(notificationSettings.text)
                .setContentIntent(notificationSettings.pendingIntent)
                .setSmallIcon(notificationSettings.icon)
                .build()

        startForeground(startId, notification)

        return START_NOT_STICKY // 強制終了後に再起動されない
    }

    private fun onCreateNotificationSettings(): MultiFloatWindowNotificationSettings {
        return MultiFloatWindowNotificationSettings(
                "フロートウィンドウ",
                "起動中",
                Icon.createWithResource(applicationContext, R.mipmap.ic_main_launcher),
                PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this,
                                FloatWindowStopActivity::class.java),
                        0
                )
        )
    }

    data class MultiFloatWindowNotificationSettings(
            val title: String = "Multi Window Application",
            val text: String = "Processing",
            val icon: Icon,
            val pendingIntent: PendingIntent
    )
}