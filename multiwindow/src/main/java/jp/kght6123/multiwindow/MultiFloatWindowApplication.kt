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
import jp.kght6123.multiwindow.utils.UnitUtils

/**
 * マルチウィンドウサービスの実装を簡易化するクラス
 * FIXME リソースIDベースのsetterも作らなきゃ
 *
 * Created by kght6123 on 2017/07/30.
 */
abstract class MultiFloatWindowApplication : Service() {

    val manager: MultiFloatWindowManager by lazy { MultiFloatWindowManager(applicationContext) }

    var title: String = ""
    var initSettings: MultiFloatWindowInitSettings? = null
    var notificationSettings: MultiFloatWindowNotificationSettings? = null

    var minimizedViewFactory: MultiFloatWindowViewFactory? = null
    var minimizedLayoutParamFactory: MultiFloatWindowLayoutParamFactory? = null
    var windowViewFactory: MultiFloatWindowViewFactory? = null
    var windowLayoutParamFactory: MultiFloatWindowLayoutParamFactory? = null

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
                            if(initSettings == null)
                                initSettings = MultiFloatWindowInitSettings(
                                        UnitUtils.convertDp2Px(25f, applicationContext).toInt(),
                                        UnitUtils.convertDp2Px(25f, applicationContext).toInt(),
                                        UnitUtils.convertDp2Px(300f, applicationContext).toInt(),
                                        UnitUtils.convertDp2Px(450f, applicationContext).toInt()
                                )

                            val info = manager.add(
                                    msg.arg1,
                                    initSettings!!.x,
                                    initSettings!!.y,
                                    initSettings!!.miniMode,
                                    initSettings!!.backgroundColor,
                                    initSettings!!.width,
                                    initSettings!!.height,
                                    title)

                            if(minimizedViewFactory != null && minimizedLayoutParamFactory != null)
                                info.miniWindowFrame.addView(minimizedViewFactory!!.create(), minimizedLayoutParamFactory!!.create())

                            if(windowViewFactory != null && windowLayoutParamFactory != null)
                                info.windowInlineFrame.addView(windowViewFactory!!.create(), windowLayoutParamFactory!!.create())
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

        if(notificationSettings != null) {
            // 前面で起動する
            val notification = Notification.Builder(this)
                    .setContentTitle(notificationSettings!!.title)
                    .setContentText(notificationSettings!!.text)
                    .setContentIntent(notificationSettings!!.pendingIntent)
                    .setSmallIcon(notificationSettings!!.icon)
                    .build()

            startForeground(startId, notification)
        }
        return START_NOT_STICKY // 強制終了後に再起動されない
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    private fun finish() {
        manager.finish()
    }

    data class MultiFloatWindowInitSettings(
            val x: Int,
            val y: Int,
            val width: Int,
            val height: Int,
            val backgroundColor: Int = MultiFloatWindowConstants.Theme.Light.rgb,
            val miniMode: Boolean = false
    )
    data class MultiFloatWindowNotificationSettings(
            val title: String = "Multi Window Application",
            val text: String = "Processing",
            val icon: Icon,
            val pendingIntent: PendingIntent
    )
    interface MultiFloatWindowViewFactory {
        fun create(): View
    }
    interface MultiFloatWindowLayoutParamFactory {
        fun create(): LinearLayout.LayoutParams
    }
}