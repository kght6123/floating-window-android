package jp.kght6123.multiwindow

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.widget.RemoteViews
import android.widget.Toast
import jp.kght6123.multiwindowframework.MultiWindowControlCommand
import jp.kght6123.multiwindowframework.MultiWindowControlParam
import jp.kght6123.multiwindowframework.MultiWindowOpenType

/**
 * マルチウィンドウアプリケーションの本体サービス
 *
 * Created by kght6123 on 2017/09/10.
 */
class MultiFloatWindowApplicationService : Service() {

    val manager: MultiFloatWindowManager by lazy { MultiFloatWindowManager(applicationContext) }

    private val handler: Handler by lazy {
        object: Handler() {
            override fun handleMessage(msg: Message?) {
                if(msg != null) {
                    val command = MultiWindowControlCommand.values()[msg.what]
                    when (command) {
                        MultiWindowControlCommand.HELLO -> {
                            Toast.makeText(applicationContext, "hello!! multi window framework.", Toast.LENGTH_SHORT).show()
                            manager // start Manager.
                        }
                        MultiWindowControlCommand.OPEN -> {
                            val packageName = msg.data.getString(MultiWindowControlParam.APP_PACKAGE_NAME.name)
                            val serviceClassName = msg.data.getString(MultiWindowControlParam.APP_SERVICE_CLASS_NAME.name)
                            val command = MultiWindowOpenType.values()[msg.arg2]
                            when (command) {
                                MultiWindowOpenType.NEW -> {
                                    manager.openWindow(msg.arg1, packageName, serviceClassName,false)
                                }
                                MultiWindowOpenType.UPDATE -> {
                                    if(manager.factoryMap.containsKey(msg.arg1))
                                        manager.openWindow(msg.arg1, packageName, serviceClassName,true)
                                    else
                                        manager.openWindow(msg.arg1, packageName, serviceClassName,false)
                                }
                            }
                        }
                        MultiWindowControlCommand.START -> {
                            val intent = msg.obj as Intent
                            val index = intent.getIntExtra(MultiWindowControlParam.WINDOW_INDEX.name, 0)
                            manager.factoryMap.getValue(index).start(intent)
                        }
                        MultiWindowControlCommand.CLOSE -> {
                            manager.remove(msg.arg1)
                            manager.factoryMap.remove(msg.arg1)
                        }
                        MultiWindowControlCommand.ADD_REMOTE_VIEWS -> {
                            val remoteWindowViews =
                                    msg.data.getParcelable<RemoteViews>(MultiWindowControlParam.REMOTE_WINDOW_VIEWS.name)
                            val remoteMiniViews =
                                    msg.data.getParcelable<RemoteViews>(MultiWindowControlParam.REMOTE_MINI_VIEWS.name)

                            if(manager.factoryMap.containsKey(msg.arg1))
                                manager.openRemoteWindow(msg.arg1, remoteWindowViews, remoteMiniViews,true)
                            else
                                manager.openRemoteWindow(msg.arg1, remoteWindowViews, remoteMiniViews,false)
                        }
                        MultiWindowControlCommand.ADD_APP_WIDGET -> {
                            if(manager.factoryMap.containsKey(msg.arg1))
                                manager.openAppWidgetWindow(msg.arg1, msg.arg2,true)
                            else
                                manager.openAppWidgetWindow(msg.arg1, msg.arg2,false)
                        }
                        else -> {
                            super.handleMessage(msg)
                        }
                    }
                } else {
                    super.handleMessage(msg)
                }
            }
        }
    }
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
                "マルチウィンドウアプリ",
                "起動中",
                Icon.createWithResource(applicationContext, R.mipmap.ic_launcher_round),
                PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this,
                                MultiFloatWindowApplicationService::class.java),
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