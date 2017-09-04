package jp.kght6123.multiwindow

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.*
import jp.kght6123.multiwindow.utils.UnitUtils

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
        ADD_REMOTE_VIEWS,
        ADD_APP_WIDGET,
        CLOSE,
        EXIT,
    }
    enum class MultiWindowControlParam {
        WINDOW_INDEX,
        REMOTE_WINDOW_VIEWS,
        REMOTE_MINI_VIEWS,
    }
    enum class MultiWindowOpenType {
        UPDATE,
        NEW,
    }

    companion object {
        val APP_WIDGET_HOST_ID: Int = 798756856
    }
    val appWidgetHost by lazy {
        val appWidgetHost = AppWidgetHost(this, MultiFloatWindowApplication.APP_WIDGET_HOST_ID)
        appWidgetHost.startListening()
        appWidgetHost
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
                                    openWindow(msg, false)
                                }
                                MultiWindowOpenType.UPDATE -> {
                                    if(factoryMap.containsKey(msg.arg1))
                                        openWindow(msg, true)
                                    else
                                        openWindow(msg, false)
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
                        MultiWindowControlCommand.ADD_REMOTE_VIEWS -> {
                            if(factoryMap.containsKey(msg.arg1))
                                openRemoteWindow(msg, true)
                            else
                                openRemoteWindow(msg, false)
                        }
                        MultiWindowControlCommand.ADD_APP_WIDGET -> {
                            if(factoryMap.containsKey(msg.arg1))
                                openAppWidgetWindow(msg, true)
                            else
                                openAppWidgetWindow(msg, false)
                        }
                        else -> {
                            super.handleMessage(msg)
                        }
                    }
                } else {
                    super.handleMessage(msg)
                }
            }
            private fun openWindow(msg: Message, update: Boolean) {
                val factory = MultiFloatWindowFactory(onCreateFactory(msg.arg1), onCreateSettingsFactory(msg.arg1))
                factoryMap.put(msg.arg1, factory)

                val initSettings = factory.windowSettingsFactory.createInitSettings(msg.arg1)
                val info =
                    if(update)
                        manager.update(
                            msg.arg1,
                            initSettings.miniMode,
                            initSettings.backgroundColor,
                            initSettings.width,
                            initSettings.height,
                            initSettings.active)
                    else
                        manager.add(
                            msg.arg1,
                            initSettings.miniMode,
                            initSettings.x,
                            initSettings.y,
                            initSettings.backgroundColor,
                            initSettings.width,
                            initSettings.height,
                            initSettings.active)

                if(update) {
                    info.windowInlineFrame.removeAllViews()
                    info.miniWindowFrame.removeAllViews()
                }
                val windowViewFactory = factory.windowViewFactory
                info.windowInlineFrame.addView(
                        windowViewFactory.createWindowView(msg.arg1),
                        windowViewFactory.createWindowLayoutParams(msg.arg1))
                info.miniWindowFrame.addView(
                        windowViewFactory.createMinimizedView(msg.arg1),
                        windowViewFactory.createMinimizedLayoutParams(msg.arg1))
            }
            private fun openRemoteWindow(msg: Message, update: Boolean) {
                val remoteWindowViews =
                        msg.data.getParcelable<RemoteViews>(MultiFloatWindowApplication.MultiWindowControlParam.REMOTE_WINDOW_VIEWS.name)
                val remoteMiniViews =
                        msg.data.getParcelable<RemoteViews>(MultiFloatWindowApplication.MultiWindowControlParam.REMOTE_MINI_VIEWS.name)

                val windowSettingsFactory = MultiFloatWindowSettingsRemoteViewFactory(applicationContext)
                val initSettings = windowSettingsFactory.createInitSettings(msg.arg1)

                val info = if(update)
                    manager.update(
                        msg.arg1,
                        initSettings.miniMode,
                        initSettings.backgroundColor,
                        initSettings.width,
                        initSettings.height,
                        initSettings.active)
                else
                    manager.add(
                        msg.arg1,
                        initSettings.miniMode,
                        initSettings.x,
                        initSettings.y,
                        initSettings.backgroundColor,
                        initSettings.width,
                        initSettings.height,
                        initSettings.active)

                val windowViewFactory = MultiFloatWindowViewRemoteViewFactory(
                        applicationContext,
                        remoteWindowViews,
                        remoteMiniViews,
                        info.windowInlineFrame,
                        info.miniWindowFrame)

                if(update) {
                    info.windowInlineFrame.removeAllViews()
                    info.miniWindowFrame.removeAllViews()
                }
                info.windowInlineFrame.addView(
                        windowViewFactory.createWindowView(msg.arg1),
                        windowViewFactory.createWindowLayoutParams(msg.arg1))
                info.miniWindowFrame.addView(
                        windowViewFactory.createMinimizedView(msg.arg1),
                        windowViewFactory.createMinimizedLayoutParams(msg.arg1))

                val factory = MultiFloatWindowFactory(windowViewFactory, windowSettingsFactory)
                factoryMap.put(msg.arg1, factory)
            }
            private fun openAppWidgetWindow(msg: Message, update: Boolean) {
                val appWidgetId = msg.arg2
                val appWidgetProviderInfo =
                        AppWidgetManager.getInstance(applicationContext).getAppWidgetInfo(appWidgetId)

                val windowViewFactory =
                        MultiFloatWindowViewAppWidgetFactory(applicationContext, appWidgetHost, appWidgetId, appWidgetProviderInfo)

                val windowSettingsFactory =
                        MultiFloatWindowSettingsAppWidgetFactory(applicationContext, appWidgetProviderInfo)
                val initSettings = windowSettingsFactory.createInitSettings(msg.arg1)

                val factory = MultiFloatWindowFactory(windowViewFactory, windowSettingsFactory)
                factoryMap.put(msg.arg1, factory)

                val info = if(update)
                    manager.update(
                            msg.arg1,
                            initSettings.miniMode,
                            initSettings.backgroundColor,
                            initSettings.width,
                            initSettings.height,
                            initSettings.active)
                else
                    manager.add(
                            msg.arg1,
                            initSettings.miniMode,
                            initSettings.x,
                            initSettings.y,
                            initSettings.backgroundColor,
                            initSettings.width,
                            initSettings.height,
                            initSettings.active)

                if(update) {
                    info.windowInlineFrame.removeAllViews()
                    info.miniWindowFrame.removeAllViews()
                }
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
    private class MultiFloatWindowViewRemoteViewFactory(
            val context: Context,
            val remoteWindowViews: RemoteViews,
            val remoteMiniViews: RemoteViews,
            val windowInlineFrame: ViewGroup,
            val miniWindowFrame: ViewGroup): MultiFloatWindowViewFactory {

        override fun createWindowView(arg: Int): View {
            return remoteWindowViews.apply(context, windowInlineFrame)
        }
        override fun createMinimizedView(arg: Int): View {
            return remoteMiniViews.apply(context, miniWindowFrame)
        }
        override fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                    UnitUtils.convertDp2Px(75f, context).toInt(),
                    UnitUtils.convertDp2Px(75f, context).toInt())
        }
        override fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
        }
        override fun start(intent: Intent?) {}
    }
    private class MultiFloatWindowSettingsRemoteViewFactory(val context: Context): MultiFloatWindowSettingsFactory {
        override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
            return MultiFloatWindowInitSettings(
                    x = 50,
                    y = 50,
                    width = UnitUtils.convertDp2Px(75f, context).toInt(),
                    height = UnitUtils.convertDp2Px(75f, context).toInt(),
                    backgroundColor = MultiFloatWindowConstants.Theme.Dark.rgb,
                    active = false
            )
        }
    }
    private class MultiFloatWindowViewAppWidgetFactory(val context: Context, val appWidgetHost: AppWidgetHost, val appWidgetId: Int, val appWidgetProviderInfo: AppWidgetProviderInfo): MultiFloatWindowViewFactory {
        override fun createWindowView(arg: Int): View {
            val appWidgetHostView = appWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo)
            appWidgetHostView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,//appWidgetProviderInfo.minWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT)//appWidgetProviderInfo.minHeight)
            appWidgetHostView.setAppWidget(appWidgetId, appWidgetProviderInfo)
            //appWidgetHostView.setBackgroundResource(R.color.float_window_background_color)
            return appWidgetHostView
        }
        override fun createMinimizedView(arg: Int): View {
            val iconView = ImageView(context)
            iconView.setImageDrawable(appWidgetProviderInfo.loadIcon(context, DisplayMetrics.DENSITY_HIGH))
            return iconView
        }
        override fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                    UnitUtils.convertDp2Px(75f, context).toInt(),
                    UnitUtils.convertDp2Px(75f, context).toInt())
        }
        override fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
        }
        override fun start(intent: Intent?) {}
    }
    private class MultiFloatWindowSettingsAppWidgetFactory(val context: Context, val appWidgetProviderInfo: AppWidgetProviderInfo): MultiFloatWindowSettingsFactory {
        override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
            return MultiFloatWindowInitSettings(
                    x = 50,
                    y = 50,
                    width = appWidgetProviderInfo.minWidth,//UnitUtils.convertDp2Px(300f, context).toInt(),
                    height = appWidgetProviderInfo.minHeight,//UnitUtils.convertDp2Px(450f, context).toInt(),
                    backgroundColor = MultiFloatWindowConstants.Theme.Dark.rgb,
                    active = false
            )
        }
    }
}