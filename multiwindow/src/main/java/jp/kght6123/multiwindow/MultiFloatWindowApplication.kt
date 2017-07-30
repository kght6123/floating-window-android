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
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast

/**
 * Created by kogahirotaka on 2017/07/30.
 */
abstract class MultiFloatWindowApplication : Service() {

    val manager: MultiFloatWindowManager by lazy { MultiFloatWindowManager(applicationContext) }

    abstract fun getIconView() : View
    abstract fun getIconLayoutParam() : LinearLayout.LayoutParams
    abstract fun getWindowView() : View
    abstract fun getWindowLayoutParam() : LinearLayout.LayoutParams

    abstract fun getX() : Int
    abstract fun getY() : Int
    abstract fun getInitWidth() : Int
    abstract fun getInitHeight() : Int
    abstract fun getBackgroundColor() : Int

    abstract fun getNotificationTitle() : String
    abstract fun getNotificationText() : String
    abstract fun getNotificationIcon() : Icon
    abstract fun getNotificationPendingIntent() : PendingIntent

    enum class MultiWindowControlCommand {
        HELLO,
        OPEN,
        CLOSE,
        EXIT
    }

    val handler: Handler by lazy {
        object: Handler() {
            override fun handleMessage(msg: Message?) {
                if(msg != null) {
                    val command = MultiWindowControlCommand.values()[msg.what]
                    when (command) {
                        MultiWindowControlCommand.HELLO -> {
                            Toast.makeText(applicationContext, "hello!! multi window framework.", Toast.LENGTH_SHORT).show()
                        }
                        MultiWindowControlCommand.OPEN -> {
                            val info = manager.add(msg.arg1, getX(), getY(), false, getBackgroundColor(), getInitWidth(), getInitHeight())
                            info.miniWindowFrame.addView(getIconView(), getIconLayoutParam())
                            info.windowInlineFrame.addView(getWindowView(), getWindowLayoutParam())
                        }
                        MultiWindowControlCommand.CLOSE -> {
                            manager.remove(msg.arg1)
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

    val mMessenger = Messenger(handler)

    override fun onBind(intent: Intent?): IBinder {
        return mMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // 前面で起動する
        val notification = Notification.Builder(this)
                .setContentTitle(getNotificationTitle())
                .setContentText(getNotificationText())
                .setContentIntent(getNotificationPendingIntent())
                .setSmallIcon(getNotificationIcon())
                .build()

        startForeground(startId, notification)

        return START_NOT_STICKY // 強制終了後に再起動されない
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.finish()
    }
}