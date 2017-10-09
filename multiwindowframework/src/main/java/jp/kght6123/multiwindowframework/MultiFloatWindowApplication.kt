package jp.kght6123.multiwindowframework

import android.app.Service
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import jp.kght6123.multiwindowframework.utils.UnitUtils

/**
 * マルチウィンドウサービスの実装を簡易化するクラス
 *
 * Created by kght6123 on 2017/07/30.
 */
abstract class MultiFloatWindowApplication : Service() {

    var sharedContext: Context? = null

    override fun onBind(p0: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Suppress("unused")
    fun attachBaseContext(sharedContext: Context, applicationContext: Context) {
        attachBaseContext(applicationContext)
        this.sharedContext = sharedContext
    }
    protected fun createContentView(layoutResId: Int): View {
        val parser = sharedContext!!.resources.getLayout(layoutResId)
        return LayoutInflater.from(applicationContext).inflate(parser, null)
    }
    abstract fun onCreateFactory(index: Int): MultiFloatWindowViewFactory
    abstract fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory

    class MultiFloatWindowInitSettings(
            val x: Int,
            val y: Int,
            val width: Int,
            val height: Int,
            val backgroundColor: Int = MultiFloatWindowConstants.Theme.Light.rgb,
            val miniMode: Boolean = false,
            val active: Boolean = false
    )

    class MultiFloatWindowFactory(val classObj: Class<*>, private val windowViewFactory: Any, private val windowSettingsFactory: Any) {

        private val windowViewFactoryClass = windowViewFactory.javaClass
        private val createWindowViewMethod = windowViewFactoryClass.getMethod("createWindowView", Int::class.java)
        private val createMinimizedViewMethod = windowViewFactoryClass.getMethod("createMinimizedView", Int::class.java)
        private val createWindowLayoutParamsMethod = windowViewFactoryClass.getMethod("createWindowLayoutParams", Int::class.java)
        private val createMinimizedLayoutParamsMethod = windowViewFactoryClass.getMethod("createMinimizedLayoutParams", Int::class.java)
        private val startMethod = windowViewFactoryClass.getMethod("start", Intent::class.java)

        private val windowSettingsFactoryClass = windowSettingsFactory.javaClass
        private val createInitSettingsMethod = windowSettingsFactoryClass.getMethod("createInitSettings", Int::class.java)

        fun createWindowView(arg: Int): View {
            return createWindowViewMethod.invoke(windowViewFactory, arg) as View
        }
        fun createMinimizedView(arg: Int): View {
            return createMinimizedViewMethod.invoke(windowViewFactory, arg) as View
        }
        fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return createWindowLayoutParamsMethod.invoke(windowViewFactory, arg) as LinearLayout.LayoutParams
        }
        fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return createMinimizedLayoutParamsMethod.invoke(windowViewFactory, arg) as LinearLayout.LayoutParams
        }
        fun start(intent: Intent?) {
            startMethod.invoke(windowViewFactory, intent)
        }
        fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
            val settings = createInitSettingsMethod.invoke(windowSettingsFactory, arg)
            val settingsClass = settings.javaClass
            val x = settingsClass.getMethod("getX").invoke(settings) as Int
            val y = settingsClass.getMethod("getY").invoke(settings) as Int
            val width = settingsClass.getMethod("getWidth").invoke(settings) as Int
            val height = settingsClass.getMethod("getHeight").invoke(settings) as Int
            val backgroundColor = settingsClass.getMethod("getBackgroundColor").invoke(settings) as Int
            val miniMode = settingsClass.getMethod("getMiniMode").invoke(settings) as Boolean
            val active = settingsClass.getMethod("getActive").invoke(settings) as Boolean

            //val windowSettingsMethod = windowViewFactoryClass.getMethod("getWindowSettings")
            //val windowSettings = windowSettingsMethod.invoke(windowViewFactory)

            return MultiFloatWindowInitSettings(x, y, width, height, backgroundColor, miniMode, active)
        }
    }
    abstract class MultiFloatWindowViewFactory {

//        val windowSettings = MultiFloatWindowSettings()

        abstract fun createWindowView(arg: Int): View
        abstract fun createMinimizedView(arg: Int): View
        abstract fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams
        abstract fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams

        abstract fun start(intent: Intent?)

//        inner class MultiFloatWindowSettings {
//            var x: Int = 100
//            var y: Int = 100
//            var width: Int = 800
//            var height: Int = 600
//            var backgroundColor: Int = MultiFloatWindowConstants.Theme.Light.rgb
//            var miniMode: Boolean = false
//            var active: Boolean = true
//        }
    }
    interface MultiFloatWindowSettingsFactory {
        fun createInitSettings(arg: Int): MultiFloatWindowInitSettings
    }
    class MultiFloatWindowViewRemoteViewFactory(
            val context: Context,
            private val remoteWindowViews: RemoteViews,
            private val remoteMiniViews: RemoteViews,
            val windowInlineFrame: ViewGroup,
            private val miniWindowFrame: ViewGroup): MultiFloatWindowViewFactory() {

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
    class MultiFloatWindowSettingsRemoteViewFactory(val context: Context): MultiFloatWindowSettingsFactory {
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
    class MultiFloatWindowViewAppWidgetFactory(
            val context: Context,
            private val appWidgetHost: AppWidgetHost,
            private val appWidgetId: Int,
            private val appWidgetProviderInfo: AppWidgetProviderInfo): MultiFloatWindowViewFactory() {

        override fun createWindowView(arg: Int): View {
            val appWidgetHostView = appWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo)
            appWidgetHostView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, //appWidgetProviderInfo.minWidth,
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
    class MultiFloatWindowSettingsAppWidgetFactory(
            private val appWidgetProviderInfo: AppWidgetProviderInfo): MultiFloatWindowSettingsFactory {
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