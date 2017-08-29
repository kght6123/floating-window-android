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
 * マルチウィンドウサービスの実装を簡易化するクラス
 * FIXME リソースIDベースのsetterも作らなきゃ!!
 *
 * Created by kght6123 on 2017/07/30.
 */
abstract class MultiFloatWindowApplication : Service() {

    val manager: MultiFloatWindowManager by lazy { MultiFloatWindowManager(applicationContext) }
    val factoryMap: MutableMap<Int, MultiFloatWindowFactory> by lazy { HashMap<Int, MultiFloatWindowFactory>() }

    enum class MultiWindowControlCommand {
        HELLO,
        START,
        OPEN,
        CLOSE,
        EXIT
    }
    enum class MultiWindowControlParam {
        WINDOW_INDEX,
    }
    enum class MultiWindowOpenType {
        UPDATE,
        NEW,
    }

    private val handler: Handler by lazy {
        object: Handler() {
            override fun handleMessage(msg: Message?) {
                if(msg != null) {
                    val command = MultiWindowControlCommand.values()[msg.what]
                    when (command) {
                        MultiWindowControlCommand.HELLO -> {
                            Toast.makeText(applicationContext, "hello!! multi window framework.", Toast.LENGTH_SHORT).show()
                        }
                        MultiWindowControlCommand.OPEN -> {
                            val command = MultiWindowOpenType.values()[msg.arg2]
                            when (command) {
                                MultiWindowOpenType.NEW -> {
                                    openNewWindow(msg)
                                }
                                MultiWindowOpenType.UPDATE -> {
                                    if(factoryMap.containsKey(msg.arg1))
                                        openUpdateWindow(msg)
                                    else
                                        openNewWindow(msg)
                                }
                            }
                        }
                        MultiWindowControlCommand.START -> {
                            val intent = msg.obj as Intent
                            val index = intent.getIntExtra(MultiFloatWindowApplication.MultiWindowControlParam.WINDOW_INDEX.name, 0)
                            factoryMap.getValue(index).windowViewFactory.start(intent)
                        }
                        MultiWindowControlCommand.CLOSE -> {
                            manager.remove(msg.arg1)
                            factoryMap.remove(msg.arg1)
                        }
                        else -> {
                            super.handleMessage(msg)
                        }
                    }
                } else {
                    super.handleMessage(msg)
                }
            }
            private fun openNewWindow(msg: Message) {
                val factory = MultiFloatWindowFactory(onCreateFactory(msg.arg1), onCreateSettingsFactory(msg.arg1))
                factoryMap.put(msg.arg1, factory)

                val initSettings = factory.windowSettingsFactory.createInitSettings(msg.arg1)
                val info = manager.add(
                        msg.arg1,
                        initSettings.miniMode,
                        initSettings.x,
                        initSettings.y,
                        initSettings.backgroundColor,
                        initSettings.width,
                        initSettings.height,
                        initSettings.active)

                val windowViewFactory = factory.windowViewFactory
                info.windowInlineFrame.addView(
                        windowViewFactory.createWindowView(msg.arg1),
                        windowViewFactory.createWindowLayoutParams(msg.arg1))
                info.miniWindowFrame.addView(
                        windowViewFactory.createMinimizedView(msg.arg1),
                        windowViewFactory.createMinimizedLayoutParams(msg.arg1))
            }
            private fun openUpdateWindow(msg: Message) {
                val factory = MultiFloatWindowFactory(onCreateFactory(msg.arg1), onCreateSettingsFactory(msg.arg1))
                factoryMap.put(msg.arg1, factory)

                val initSettings = factory.windowSettingsFactory.createInitSettings(msg.arg1)
                val info = manager.update(
                        msg.arg1,
                        initSettings.miniMode,
                        initSettings.backgroundColor,
                        initSettings.width,
                        initSettings.height,
                        initSettings.active)

                info.windowInlineFrame.removeAllViews()
                info.miniWindowFrame.removeAllViews()

                val windowViewFactory = factory.windowViewFactory
                info.windowInlineFrame.addView(
                        windowViewFactory.createWindowView(msg.arg1),
                        windowViewFactory.createWindowLayoutParams(msg.arg1))
                info.miniWindowFrame.addView(
                        windowViewFactory.createMinimizedView(msg.arg1),
                        windowViewFactory.createMinimizedLayoutParams(msg.arg1))
            }
        }
    }
    private val mMessenger by lazy { Messenger(handler) }

    override fun onCreate() {
        super.onCreate()
    }
    override fun onBind(intent: Intent?): IBinder {
        return mMessenger.binder
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notificationSettings = onCreateNotificationSettings()
        // 前面で起動する
        val notification = Notification.Builder(this)
                .setContentTitle(notificationSettings.title)
                .setContentText(notificationSettings.text)
                .setContentIntent(notificationSettings.pendingIntent)
                .setSmallIcon(notificationSettings.icon)
                .build()

        startForeground(startId, notification)

        return START_NOT_STICKY // 強制終了後に再起動されない
    }
    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
    private fun finish() {
        manager.finish()
    }

    abstract fun onCreateNotificationSettings(): MultiFloatWindowNotificationSettings
    abstract fun onCreateFactory(index: Int): MultiFloatWindowViewFactory
    abstract fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory

    data class MultiFloatWindowInitSettings(
            val x: Int,
            val y: Int,
            val width: Int,
            val height: Int,
            val backgroundColor: Int = MultiFloatWindowConstants.Theme.Light.rgb,
            val miniMode: Boolean = false,
            val active: Boolean = false
    )
    data class MultiFloatWindowNotificationSettings(
            val title: String = "Multi Window Application",
            val text: String = "Processing",
            val icon: Icon,
            val pendingIntent: PendingIntent
    )
    data class MultiFloatWindowFactory(
            val windowViewFactory: MultiFloatWindowViewFactory,
            val windowSettingsFactory: MultiFloatWindowSettingsFactory
    )
    interface MultiFloatWindowViewFactory {
        fun createWindowView(arg: Int): View
        fun createMinimizedView(arg: Int): View
        fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams
        fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams

        fun start(intent: Intent?)
    }
    interface MultiFloatWindowSettingsFactory {
        fun createInitSettings(arg: Int): MultiFloatWindowInitSettings
    }
}