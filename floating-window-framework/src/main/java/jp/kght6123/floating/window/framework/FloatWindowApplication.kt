package jp.kght6123.floating.window.framework

import android.app.Service
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetProviderInfo
import android.content.*
import android.os.*
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import jp.kght6123.floating.window.framework.context.res.Resources
import jp.kght6123.floating.window.framework.context.res.ResourcesImpl
import jp.kght6123.floating.window.framework.utils.UnitUtils
import jp.kght6123.floating.window.framework.utils.*

/**
 * マルチウィンドウサービスの実装を簡易化するクラス
 * 
 * @author    kght6123
 * @copyright 2017/07/30 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
abstract class FloatWindowApplication : Service() {

    val multiWindowContext by lazy { MultiFloatWindowContext() }

    protected var sharedContext: Context? = null
    fun attachBaseContext(sharedContext: Context, applicationContext: Context) {
        attachBaseContext(applicationContext)
        this.sharedContext = sharedContext
    }

    private var manager: MultiFloatWindowManagerUpdater? = null
    fun attachManager(manager: Any) {
        this.manager = MultiFloatWindowManagerUpdaterImpl(manager)
    }

    protected fun createContentView(layoutResId: Int): View {
        val parser = sharedContext!!.resources.getLayout(layoutResId)

        // applicationContextだと、styleに定義されているdimensionが取得できずに、Viewの初期化時にTypedArray.getDimensionPixelSizeでエラー
        //    Caused by: java.lang.UnsupportedOperationException: Failed to resolve attribute at index 17: TypedValue{t=0x2/d=0x7f020063 a=-1}
        //    ContextのobtainStyledAttributesで取得したTypedArrayについて、dimensionが取得できない
        //    SharedContextなら行けるはずだけど、ContextWrapperでラップしてもobtainStyledAttributesの定義がfinalなので上書きできない
        //

        // 渡すContextを下記に変えたけど無理ぽ
        //    this.baseContext, sharedContext, SharedContextWrapper(sharedContext!!, applicationContext), this
        //       val configContext = applicationContext.createConfigurationContext(sharedContext!!.getResources().getConfiguration())
        //      sharedContext系は、Context.getSystemServiceでぬるぽ
        //

        // cloneInContextとか・・・ContextThemeWrapperを使ってる？？
        //   LayoutInflater.from(applicationContext).cloneInContext(sharedContext!!).inflate(parser, null)
        //      同じくContext.getSystemServiceでぬるぽ
        //

        // LayoutInflater.Factory2を使ってみるか？
        //    この「LayoutInflater.parseInclude:964, LayoutInflater (android.view)」が既に暗黙的に呼び出されており、
        //    Factory2では割り込みできない
        //     -> 簡単には無理かな・・・
        //

        val inflater = LayoutInflater.from(applicationContext)
//        inflater.factory2 = Factory2()
        return inflater.inflate(parser, null)
    }

//    inner class Factory2: LayoutInflater.Factory2 {
//
//        private fun createIncludeView(attrs: AttributeSet, root: ViewGroup?): View? {
//
//            //
//            // includeタグの本来の処理はViewStub？
//            // include先(include.xml)のFrameLayoutのリソースIDに"includer"が割り当てられるので真似る
//            //    return android.view.ViewStub(context, attrs).inflate()
//            //
//
//            val id = attrs.getIdAttributeResourceValue(0)
//            val layoutResId = attrs.getAttributeResourceValue(null, "layout", 0)
//
//            val parser = sharedContext!!.resources.getLayout(layoutResId)
//            val includeView = LayoutInflater.from(applicationContext).inflate(parser, root)
//
//            includeView.id = id // IDを設定
//
//            return includeView
//        }
//        override fun onCreateView(name: String?, context: Context?, attrs: AttributeSet?): View? {
//            return onCreateView(null, name, context, attrs)
//        }
//        override fun onCreateView(parent: View?, name: String?/*include*/, context: Context?, attrs: AttributeSet?): View? {
//            return if(name == "include" && attrs != null) {
//                createIncludeView(attrs, null)
//            } else {
//                null
//            }
//        }
//    }
//    class SharedContextWrapper(private val sharedContext: Context, applicationContext: Context): ContextWrapper(applicationContext) {
//        override fun getPackageResourcePath(): String {
//            return sharedContext.packageResourcePath
//        }
//        override fun getResources(): android.content.res.Resources {
//            return sharedContext.resources
//        }
//        override fun getTheme(): android.content.res.Resources.Theme {
//            return sharedContext.theme
//        }
//
//    }

    abstract fun onCreateFactory(index: Int): MultiFloatWindowViewFactory
    abstract fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory

    inner class MultiFloatWindowContext(val sharedContext: Context = this@FloatWindowApplication.sharedContext!!,
                                        val manager: MultiFloatWindowManagerUpdater = this@FloatWindowApplication.manager!!,
                                        val resourcesImpl: ResourcesImpl = ResourcesImpl(sharedContext.resources))

    override fun onBind(p0: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class MultiFloatWindowInitSettings(
            var x: Int = 0,
            var y: Int = 0,
            var width: Int,
            var height: Int,
            var theme: MultiFloatWindowConstants.Theme = MultiFloatWindowConstants.Theme.Dark,
            var anchor: MultiFloatWindowConstants.Anchor = MultiFloatWindowConstants.Anchor.Edge,
            var miniMode: Boolean = false,
            var active: Boolean = false
    ) {
        @Suppress("unused") fun getThemeName(): String {
            return theme.name
        }
        @Suppress("unused") fun getAnchorName(): String {
            return anchor.name
        }
    }
    class MultiFloatWindowFactory(
            val classObj: Class<*>,
            private val windowViewFactory: Any,
            private val windowSettingsFactory: Any) {

        private val windowViewFactoryClass            = windowViewFactory.javaClass
        private val createWindowViewMethod            = windowViewFactoryClass.getMethod("createWindowView",            Int::class.java)
        private val createMinimizedViewMethod         = windowViewFactoryClass.getMethod("createMinimizedView",         Int::class.java)
        private val createWindowLayoutParamsMethod    = windowViewFactoryClass.getMethod("createWindowLayoutParams",    Int::class.java)
        private val createMinimizedLayoutParamsMethod = windowViewFactoryClass.getMethod("createMinimizedLayoutParams", Int::class.java)
        private val startMethod                       = windowViewFactoryClass.getMethod("start",                       Intent::class.java)
        private val updateMethod                      = windowViewFactoryClass.getMethod("update",                      Intent::class.java, Int::class.java, String::class.java)
        private val onActiveMethod                    = windowViewFactoryClass.getMethod("onActive")
        private val onDeActiveMethod                  = windowViewFactoryClass.getMethod("onDeActive")
        private val onDeActiveAllMethod               = windowViewFactoryClass.getMethod("onDeActiveAll")
        private val onChangeMiniModeMethod            = windowViewFactoryClass.getMethod("onChangeMiniMode")
        private val onChangeWindowModeMethod          = windowViewFactoryClass.getMethod("onChangeWindowMode")

        private val windowSettingsFactoryClass        = windowSettingsFactory.javaClass
        private val createInitSettingsMethod          = windowSettingsFactoryClass.getMethod("createInitSettings", Int::class.java)

        fun onActive() {
            onActiveMethod.invoke(windowViewFactory)
        }
        fun onDeActive() {
            onDeActiveMethod.invoke(windowViewFactory)
        }
        fun onDeActiveAll() {
            onDeActiveAllMethod.invoke(windowViewFactory)
        }
        fun onChangeMode(miniMode: Boolean) {
            if(miniMode)
                onChangeMiniModeMethod.invoke(windowViewFactory)
            else
                onChangeWindowModeMethod.invoke(windowViewFactory)
        }
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
        fun update(intent: Intent?, index: Int, positionName: String) {
            updateMethod.invoke(windowViewFactory, intent, index, positionName)
        }
        fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
            val settings = createInitSettingsMethod.invoke(windowSettingsFactory, arg)
            val settingsClass = settings.javaClass
            val x = settingsClass.getMethod("getX").invoke(settings) as Int
            val y = settingsClass.getMethod("getY").invoke(settings) as Int
            val width = settingsClass.getMethod("getWidth").invoke(settings) as Int
            val height = settingsClass.getMethod("getHeight").invoke(settings) as Int
            val themeName = settingsClass.getMethod("getThemeName").invoke(settings) as String
            val anchorName = settingsClass.getMethod("getAnchorName").invoke(settings) as String
            val miniMode = settingsClass.getMethod("getMiniMode").invoke(settings) as Boolean
            val active = settingsClass.getMethod("getActive").invoke(settings) as Boolean

            //val windowSettingsMethod = windowViewFactoryClass.getMethod("getWindowSettings")
            //val windowSettings = windowSettingsMethod.invoke(windowViewFactory)

            return MultiFloatWindowInitSettings(x, y, width, height, MultiFloatWindowConstants.Theme.valueOf(themeName), MultiFloatWindowConstants.Anchor.valueOf(anchorName), miniMode, active)
        }
    }
    abstract class MultiFloatWindowViewFactory(private val context: MultiFloatWindowContext): Resources by context.resourcesImpl, MultiFloatWindowManagerUpdater by context.manager {

        abstract fun createWindowView(arg: Int): View
        abstract fun createMinimizedView(arg: Int): View

        open fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                    UnitUtils.convertDp2Px(75f, context.sharedContext).toInt(),
                    UnitUtils.convertDp2Px(75f, context.sharedContext).toInt())
        }
        open fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
        }
        open fun start(intent: Intent?) {}
        open fun update(intent: Intent?, index: Int, positionName: String) {}

        open fun onActive() {}
        open fun onDeActive() {}
        open fun onDeActiveAll() {}
        open fun onChangeMiniMode() {}
        open fun onChangeWindowMode() {}
    }
    abstract class MultiFloatWindowSettingsFactory(private val context: MultiFloatWindowContext): Resources by context.resourcesImpl {
        abstract fun createInitSettings(arg: Int): MultiFloatWindowInitSettings
    }
//    class MultiFloatWindowViewRemoteViewFactory(
//            val context: Context,
//            private val remoteWindowViews: RemoteViews,
//            private val remoteMiniViews: RemoteViews,
//            val windowInlineFrame: ViewGroup,
//            private val miniWindowFrame: ViewGroup): MultiFloatWindowViewFactory() {
//
//        override fun createWindowView(arg: Int): View {
//            return remoteWindowViews.apply(context, windowInlineFrame)
//        }
//        override fun createMinimizedView(arg: Int): View {
//            return remoteMiniViews.apply(context, miniWindowFrame)
//        }
//        override fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams {
//            return LinearLayout.LayoutParams(
//                    UnitUtils.convertDp2Px(75f, context).toInt(),
//                    UnitUtils.convertDp2Px(75f, context).toInt())
//        }
//        override fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams {
//            return LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT)
//        }
//        override fun start(intent: Intent?, index: Int) {}
//        override fun update(intent: Intent?) {}
//    }
//    class MultiFloatWindowSettingsRemoteViewFactory(val context: Context): MultiFloatWindowSettingsFactory {
//        override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
//            return MultiFloatWindowInitSettings(
//                    x = 50,
//                    y = 50,
//                    width = UnitUtils.convertDp2Px(75f, context).toInt(),
//                    height = UnitUtils.convertDp2Px(75f, context).toInt(),
//                    backgroundColor = MultiFloatWindowConstants.Theme.Dark.rgb,
//                    active = false
//            )
//        }
//    }
    class MultiFloatWindowViewAppWidgetFactory(
        private val context: MultiFloatWindowContext,
        private val appWidgetHost: AppWidgetHost,
        private val appWidgetId: Int,
        private val appWidgetProviderInfo: AppWidgetProviderInfo): MultiFloatWindowViewFactory(context) {

        override fun createWindowView(arg: Int): View {
            val appWidgetHostView = appWidgetHost.createView(context.sharedContext, appWidgetId, appWidgetProviderInfo)
            appWidgetHostView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, //appWidgetProviderInfo.minWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT)//appWidgetProviderInfo.minHeight)

            appWidgetHostView.setAppWidget(appWidgetId, appWidgetProviderInfo)
            //appWidgetHostView.setBackgroundResource(R.color.float_window_background_color)

            // Widgetのリサイズ時のmax,min値を設定する
            if(appWidgetProviderInfo.isResizeHeight())
                setMinHeight(arg, appWidgetProviderInfo.minResizeHeight)
            if(appWidgetProviderInfo.isResizeWidth())
                setMinWidth(arg, appWidgetProviderInfo.minResizeWidth)

            return appWidgetHostView
        }
        override fun createMinimizedView(arg: Int): View {
            val iconView = ImageView(context.sharedContext)
            iconView.setImageDrawable(appWidgetProviderInfo.loadIcon(context.sharedContext, DisplayMetrics.DENSITY_HIGH))
            return iconView
        }
    }
    class MultiFloatWindowSettingsAppWidgetFactory(
            private val context: MultiFloatWindowContext,
            private val appWidgetProviderInfo: AppWidgetProviderInfo): MultiFloatWindowSettingsFactory(context) {

        override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
            return MultiFloatWindowInitSettings(
                    x = UnitUtils.convertDp2Px(50f, context.sharedContext).toInt(),
                    y = UnitUtils.convertDp2Px(50f, context.sharedContext).toInt(),
                    width = appWidgetProviderInfo.initMinWidth(UnitUtils.convertDp2Px(160f, context.sharedContext).toInt()),
                    height = appWidgetProviderInfo.initMinHeight(UnitUtils.convertDp2Px(160f, context.sharedContext).toInt()),
                    theme = MultiFloatWindowConstants.Theme.Dark,
                    active = false
            )
        }
    }
}