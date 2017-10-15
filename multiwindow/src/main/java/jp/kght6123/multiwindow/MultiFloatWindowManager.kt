package jp.kght6123.multiwindow

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import jp.kght6123.multiwindow.recycler.CardAppListRecyclerView
import jp.kght6123.multiwindow.recycler.IconAppListRecyclerView
import jp.kght6123.multiwindow.viewgroup.MultiFloatWindowOverlayLayout
import jp.kght6123.multiwindowframework.MultiFloatWindowApplication
import jp.kght6123.multiwindowframework.MultiFloatWindowConstants
import jp.kght6123.multiwindowframework.utils.UnitUtils
import java.util.*
import kotlin.concurrent.withLock

/**
 * マルチウィンドウ情報のコントロールを行う
 *
 * Created by kght6123 on 2017/07/11.
 */
class MultiFloatWindowManager(val context: Context) {

    private val tag = this.javaClass.name

    private enum class ControlViewMode {
        MINI_ICON, APP_LIST,
    }

    val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val overlayView: MultiFloatWindowOverlayLayout by lazy {
        val overlayView = View.inflate(context, R.layout.multiwindow_overlay_layer, null) as MultiFloatWindowOverlayLayout

        overlayView.onDispatchTouchEventListener = object: View.OnTouchListener {

            val TAG = this.javaClass.name
            val gestureDetector: GestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapUp(event: MotionEvent): Boolean {
                    Log.d(TAG, "SimpleOnGestureListener onSingleTapUp")

                    // 単純にタッチした時のみ、Active判定
                    changeActiveEvent(event)
                    //return super.onSingleTapUp(event)
                    return true
                }
            })
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_OUTSIDE -> {
                        Log.d(TAG, "onDispatchTouchEventListener onTouch MotionEvent.ACTION_OUTSIDE")
                        changeActiveEvent(event)	// OUTSIDEの時もActive判定
                        return true
                    }
                }
                Log.d(TAG, "onDispatchTouchEventListener onTouch .action=${event.action}, .rawX,rawY=${event.rawX},${event.rawY} .x,y=${event.x},${event.y}")

                gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う
                return false
            }
        }
        overlayView
    }

    private val activeFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or //座標系をスクリーンに合わせる
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or // Viewの外のタッチイベントにも反応する？
            //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or  // タッチイベントを拾わない。ロック画面を邪魔しない
            //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  // ウィンドウにフォーカスが当たった時だけ、無効にしたい
            //WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM  // ウィンドウがどのように対話するかに関して、FLAG_NOT_FOCUSABLEの状態を反転させる？？
            WindowManager.LayoutParams.FLAG_DIM_BEHIND or  // 後ろのすべてが淡色表示されます。
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or // 境界外のタッチイベントが受け付けられ、サポートする他ウィンドウにも送信
            //WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or   // 半透明のナビゲーションバー？
            //WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS   // 半透明のステータスバー？
            //WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

    private val activeDimAmount = 0.09f
    private val activeAlpha = 0.95f

    private val inactiveFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE// or
    //WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR

    private val inactiveAlpha = 0.5f
    private val inactiveDimAmount = 0.0f

    // WindowManagerに設定するレイアウトパラメータ、オーバーレイViewの設定をする
    private val params: WindowManager.LayoutParams by lazy {
        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0, // X
                0, // Y
                //WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // なるべく上の階層で表示
                //WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Android-O以降
                activeFlags,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START// or Gravity.LEFT
        params.dimAmount = activeDimAmount
        //params.windowAnimations = android.R.style.Animation//Animation_Translucent//Animation_Activity//Animation_Toast//Animation_Dialog
        //params.windowAnimations = android.R.style.Animation_Translucent
        params.alpha = activeAlpha
        params
    }

    private val ctrlActiveFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

    val ctrlIconParams: WindowManager.LayoutParams by lazy {
        val params = WindowManager.LayoutParams(
                UnitUtils.convertDp2Px(75f, context).toInt(),
                UnitUtils.convertDp2Px(75f, context).toInt(),
                100, // X
                150, // Y
                //WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // なるべく上の階層で表示
                //WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Android-O以降
                ctrlActiveFlags,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START// or Gravity.LEFT
        params.dimAmount = 0.0f
        params.alpha = 0.75f
        params
    }

    val ctrlListParams: WindowManager.LayoutParams by lazy {
        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,//UnitUtils.convertDp2Px(150f, context).toInt(),
                WindowManager.LayoutParams.MATCH_PARENT,
                0, // X
                0, // Y
                //WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // なるべく上の階層で表示
                //WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Android-O以降
                ctrlActiveFlags,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.END//Gravity.RIGHT// RIGHTをLEFTにすると左寄せ
        params.dimAmount = 0.0f
        params.alpha = 0.90f
        params
    }

    private var mode: ControlViewMode = ControlViewMode.MINI_ICON

    private val cardAppListParams by lazy {
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        params
    }
    private val cardAppListView: CardAppListRecyclerView by lazy { createCardAppListView() }

    private val iconAppListParams by lazy {
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UnitUtils.convertDp2Px(95f, context).toInt())
        params
    }
    private val iconAppListView: IconAppListRecyclerView by lazy { IconAppListRecyclerView(context, this) }

    private val controlLayer by lazy {
        val controlLayer =
                View.inflate(context, R.layout.multiwindow_control_layer, null).findViewById(R.id.controlLayer) as MultiFloatWindowOverlayLayout

        val controlIconLayer = controlLayer.findViewById(R.id.controlIconLayer) as LinearLayout

        val controlAppListLayer = controlLayer.findViewById(R.id.controlAppListLayer) as LinearLayout
        controlAppListLayer.addView(iconAppListView, iconAppListParams)
        controlAppListLayer.addView(cardAppListView, cardAppListParams)
        controlAppListLayer.visibility = View.GONE

        controlLayer.onDispatchTouchEventListener = object: View.OnTouchListener {

            val TAG = this.javaClass.name

            val gestureDetector: GestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    Log.d(TAG, "controlLayer SimpleOnGestureListener onSingleTapConfirmed")
                    this@MultiFloatWindowManager.changeForciblyActiveEvent()
                    return false
                }

                val lock = java.util.concurrent.locks.ReentrantLock()

                var initialX = 0
                var initialY = 0

                override fun onDown(e: MotionEvent?): Boolean {
                    Log.d(TAG, "controlLayer SimpleOnGestureListener onDown")
                    initialX = ctrlIconParams.x
                    initialY = ctrlIconParams.y
                    return false
                }

                override fun onLongPress(e: MotionEvent?) {
                    Log.d(TAG, "controlLayer SimpleOnGestureListener onLongPress")
                    lock.withLock {
                        mode = ControlViewMode.MINI_ICON

                        controlLayer.setBackgroundResource(R.drawable.shape_rounded_corners)
                        controlIconLayer.visibility = View.VISIBLE
                        controlAppListLayer.visibility = View.GONE
                        controlAppListLayer.minimumWidth = 0

                        windowManager.updateViewLayout(controlLayer, ctrlIconParams)
                    }
                }

                override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                    Log.d(TAG, "controlLayer SimpleOnGestureListener onDoubleTapEvent")
                    lock.withLock {
                        mode = ControlViewMode.APP_LIST

                        controlLayer.setBackgroundResource(R.color.app_list_background_color)
                        controlIconLayer.visibility = View.GONE
                        controlAppListLayer.visibility = View.VISIBLE
                        controlAppListLayer.minimumWidth = UnitUtils.convertDp2Px(200f, context).toInt()

                        windowManager.updateViewLayout(controlLayer, ctrlListParams)
                    }
                    return false
                }

                override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                    Log.d(TAG, "controlLayer SimpleOnGestureListener onScroll distanceX=$distanceX, distanceY=$distanceY")
                    lock.withLock {
                        if (mode == ControlViewMode.MINI_ICON) {
                            ctrlIconParams.x = initialX + (e2!!.rawX - e1!!.rawX).toInt()
                            ctrlIconParams.y = initialY + (e2.rawY - e1.rawY).toInt()
                            windowManager.updateViewLayout(controlLayer, ctrlIconParams)
                        }
                    }
                    return false
                }
            })

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_OUTSIDE -> {
                        Log.d(TAG, "controlLayer onDispatchTouchEventListener onTouch MotionEvent.ACTION_OUTSIDE")
                        return false
                    }
                }
                Log.d(TAG, "controlLayer onDispatchTouchEventListener onTouch .action=${event.action}, .rawX,rawY=${event.rawX},${event.rawY} .x,y=${event.x},${event.y}")

                gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う
                return false
            }
        }
        controlLayer
    }

    private fun createCardAppListView(): CardAppListRecyclerView {
        return CardAppListRecyclerView(context, this)
    }

    private fun getActiveParams(): WindowManager.LayoutParams {
        params.flags = activeFlags
        params.dimAmount = activeDimAmount
        params.alpha = activeAlpha
        return params
    }

    private fun getInActiveParams(): WindowManager.LayoutParams {
        params.flags = inactiveFlags
        params.dimAmount = inactiveDimAmount
        params.alpha = inactiveAlpha
        return params
    }

    val overlayWindowMap: MutableMap<Int, MultiFloatWindowInfo> = LinkedHashMap()
    val factoryMap: MutableMap<Int, MultiFloatWindowApplication.MultiFloatWindowFactory> = LinkedHashMap()

    private val appWidgetHost by lazy {
        val appWidgetHost = AppWidgetHost(context, MultiFloatWindowConstants.APP_WIDGET_HOST_ID)
        appWidgetHost.startListening()
        appWidgetHost
    }

    init {
        windowManager.addView(overlayView, getActiveParams())   // WindowManagerに追加
        windowManager.addView(controlLayer, ctrlIconParams)
    }
    fun add(index: Int, miniMode: Boolean, x: Int?, y: Int?, backgroundColor: Int, initWidth: Int, initHeight: Int, initActive: Boolean, className: String): MultiFloatWindowInfo {
        val overlayWindowInfo = MultiFloatWindowInfo(context, this, index, miniMode, backgroundColor, initWidth, initHeight, className)
        put(index, overlayWindowInfo, initActive)
        moveFixed(index, x!!, y!!)
        return overlayWindowInfo
    }
    private fun update(index: Int, miniMode: Boolean, backgroundColor: Int, initWidth: Int, initHeight: Int, initActive: Boolean, className: String): MultiFloatWindowInfo {
        val overlayInfo = MultiFloatWindowInfo(context, this, index, miniMode, backgroundColor, initWidth, initHeight, className)

        val overlayInfoOld = overlayWindowMap.getValue(index)
        overlayWindowMap.put(index, overlayInfo)  // 管理を上書き

        overlayView.removeView(overlayInfoOld.getActiveOverlay())
        overlayView.addView(overlayInfo.getActiveOverlay())

        if(initActive)
            changeActiveIndex(index)

        return overlayInfo
    }
    private fun put(index: Int, overlayInfo: MultiFloatWindowInfo, active: Boolean) {
        if(overlayWindowMap[index] != null)
            return

        overlayWindowMap.put(index, overlayInfo)  // 管理に追加
        overlayView.addView(overlayInfo.getActiveOverlay())

        if(active)
            changeActiveIndex(index)
    }
    private fun moveFixed(index: Int, x: Int, y: Int) {
        val overlayInfo = overlayWindowMap[index]
        if(overlayInfo != null) {
            val params = overlayInfo.getActiveWindowLayoutParams()
            params.leftMargin = x
            params.topMargin = y
            overlayInfo.getActiveOverlay().layoutParams = params
        }
    }
    fun remove(index: Int) {
         val overlayInfo = overlayWindowMap[index]
        if(overlayInfo != null){
            Log.d(tag, "remove is not null. index=$index")
            overlayWindowMap.remove(index)
            overlayView.removeView(overlayInfo.getActiveOverlay())
            factoryMap.remove(index)
        } else
            Log.d(tag, "remove is null. index=$index")

        updateDeActive(index)
    }
    fun changeMode(index: Int, miniMode: Boolean) {
        val overlayInfo = overlayWindowMap[index]
        if(overlayInfo != null){
            remove(index)
            overlayInfo.miniMode = miniMode
            put(index, overlayInfo, true)
        }
    }
    fun nextIndex(): Int {
        val indexList = ArrayList(factoryMap.keys)
        val nextIndex: Int =
                if(indexList.isEmpty())
                    (0 + 1)
                else {
                    Collections.reverse(indexList)
                    (indexList.first() + 1)
                }
        return nextIndex
    }
    private fun changeActiveIndex(index: Int) {
        val overlayInfo = overlayWindowMap[index]

        if(overlayInfo != null) {
            // 対象のViewを一番上へ移動する
            overlayView.removeView(overlayInfo.getActiveOverlay())
            overlayView.addView(overlayInfo.getActiveOverlay())

            updateActive(index)  // 追加したWindowをActiveに
            updateOtherDeActive(index)  // 追加したWindow以外をDeActiveに
        }
    }
    private fun updateActive(index: Int) {
        val overlayInfo = overlayWindowMap[index]
        overlayInfo?.activeFlag = true
        onActive(overlayInfo!!)
    }
    private fun updateDeActive(index: Int) {
        overlayWindowMap.forEach { entry ->
            if(entry.key == index) {
                entry.value.activeFlag = false
                onDeActive(entry.value)
            }
        }
    }
    private fun updateOtherDeActive(index: Int) {
        overlayWindowMap.forEach { entry ->
            if(entry.key != index) {
                entry.value.activeFlag = false
                onDeActive(entry.value)
            }
        }
    }
    fun changeForciblyActiveEvent() {
        if(!overlayWindowMap.isEmpty()) {
            changeActiveIndex(overlayWindowMap.keys.last())
            windowManager.updateViewLayout(overlayView, getActiveParams())
        }
    }
    fun getThumb(seq: Int) :Bitmap? {
        val windowInlineFrame = getMultiFloatWindowInfo(seq)?.windowInlineFrame
        windowInlineFrame?.isDrawingCacheEnabled = true
        windowInlineFrame?.destroyDrawingCache()
        return windowInlineFrame?.drawingCache
    }
    fun changeActiveSeq(seq: Int) {
        getMultiFloatWindowInfo(seq)?.index?.let { changeActiveIndex(it) }
    }
    fun removeSeq(seq: Int) {
        getMultiFloatWindowInfo(seq)?.index?.let { remove(it) }
    }
    private fun getMultiFloatWindowInfo(seq: Int) :MultiFloatWindowInfo? {
        if(seq < overlayWindowMap.size) {
            return overlayWindowMap.values.toList()[seq]
        }
        return null
    }
    private fun changeActiveEvent(event: MotionEvent) :Boolean {
        var changeActiveIndex: Int = -2
        for ((overlayIndex, overlayInfo) in overlayWindowMap) {
            val onTouch = overlayInfo.isOnTouchEvent(event)
            if (!onTouch/* || changeActiveName != null*/) {
                // タッチされていない、他をActive済
                updateDeActive(overlayIndex)
            } else if (!overlayInfo.activeFlag) {
                // タッチされ、Active以外、他をActiveにしていない
                changeActiveIndex = overlayIndex
            } else if (overlayInfo.activeFlag) {
                // タッチされ、Active、他をActiveにしていない
                changeActiveIndex = -1
            }
        }
        if(changeActiveIndex == -2) {
            // nullの時、何もタッチされてないので、全体をinActiveへ
            windowManager.updateViewLayout(overlayView, getInActiveParams())
            onDeActiveAll()
        }
        else if(changeActiveIndex != -1) {
            // 他のinActiveなウィンドウをタッチされた時、Activeへ
            changeActiveIndex(changeActiveIndex)
            windowManager.updateViewLayout(overlayView, getActiveParams())
        }
        return false
    }
    fun finish() {
        for ((overlayName, _) in overlayWindowMap.toList()) {
            this.remove(overlayName)
        }
        windowManager.removeViewImmediate(overlayView)
        windowManager.removeViewImmediate(controlLayer)
    }

    private var onActiveEvent: ChangeActiveEventListener? = null
    private var onDeActiveEvent: ChangeActiveEventListener? = null
    private var onDeActiveAllEvent: AllChangeActiveEventListener? = null

    private fun onActive(info: MultiFloatWindowInfo) {
        if(mode == ControlViewMode.MINI_ICON)
            controlLayer.setBackgroundResource(R.drawable.shape_rounded_corners)
        else
            cardAppListView.adapter?.notifyDataSetChanged()

        onActiveEvent?.onChange(info)
    }
    private fun onDeActive(info: MultiFloatWindowInfo) {
        if(mode == ControlViewMode.APP_LIST)
            cardAppListView.adapter?.notifyDataSetChanged()

        onDeActiveEvent?.onChange(info)
    }
    private fun onDeActiveAll() {
        if(mode == ControlViewMode.MINI_ICON)
            controlLayer.setBackgroundResource(R.drawable.shape_rounded_corners_accent)
        else
            cardAppListView.adapter?.notifyDataSetChanged()

        onDeActiveAllEvent?.onChangeAll()
    }

    interface ChangeActiveEventListener {
        fun onChange(info: MultiFloatWindowInfo)
    }

    interface AllChangeActiveEventListener {
        fun onChangeAll()
    }

    fun openWindow(windowIndex: Int, packageName: String, serviceClassName: String, update: Boolean) {

        val sharedContext = context.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE)
        val classObj = Class.forName(serviceClassName, true, sharedContext.classLoader)
        val application = classObj.newInstance()// as MultiFloatWindowApplication

        val attachBaseContextMethod =
                classObj.getMethod("attachBaseContext", Context::class.java, Context::class.java)
        val onCreateFactoryMethod =
                classObj.getMethod("onCreateFactory", Int::class.java)
        val onCreateSettingsFactoryMethod =
                classObj.getMethod("onCreateSettingsFactory", Int::class.java)

        //application.attachBaseContext(sharedContext, context)
        attachBaseContextMethod.invoke(application, sharedContext, context)

        val factory = MultiFloatWindowApplication.MultiFloatWindowFactory(
                classObj,
                //application.onCreateFactory(windowIndex),
                onCreateFactoryMethod.invoke(application, windowIndex),// as MultiFloatWindowApplication.MultiFloatWindowViewFactory,
                //application.onCreateSettingsFactory(windowIndex)
                onCreateSettingsFactoryMethod.invoke(application, windowIndex)// as MultiFloatWindowApplication.MultiFloatWindowSettingsFactory
        )
        factoryMap.put(windowIndex, factory)

        val initSettings = factory.createInitSettings(windowIndex)
        val info =
                if(update)
                    update(
                            windowIndex,
                            initSettings.miniMode,
                            initSettings.backgroundColor,
                            initSettings.width,
                            initSettings.height,
                            initSettings.active,
                            classObj.name)
                else
                    add(
                            windowIndex,
                            initSettings.miniMode,
                            initSettings.x,
                            initSettings.y,
                            initSettings.backgroundColor,
                            initSettings.width,
                            initSettings.height,
                            initSettings.active,
                            classObj.name)

        if(update) {
            info.windowInlineFrame.removeAllViews()
            info.miniWindowFrame.removeAllViews()
        }
        info.windowInlineFrame.addView(
                factory.createWindowView(windowIndex),
                factory.createWindowLayoutParams(windowIndex))
        info.miniWindowFrame.addView(
                factory.createMinimizedView(windowIndex),
                factory.createMinimizedLayoutParams(windowIndex))
    }
//    fun openRemoteWindow(windowIndex: Int, remoteWindowViews: RemoteViews, remoteMiniViews: RemoteViews, update: Boolean) {
//
//        val windowSettingsFactory = MultiFloatWindowApplication.MultiFloatWindowSettingsRemoteViewFactory(context)
//        val initSettings = windowSettingsFactory.createInitSettings(windowIndex)
//
//        val info = if(update)
//            update(
//                    windowIndex,
//                    initSettings.miniMode,
//                    initSettings.backgroundColor,
//                    initSettings.width,
//                    initSettings.height,
//                    initSettings.active)
//        else
//            add(
//                    windowIndex,
//                    initSettings.miniMode,
//                    initSettings.x,
//                    initSettings.y,
//                    initSettings.backgroundColor,
//                    initSettings.width,
//                    initSettings.height,
//                    initSettings.active)
//
//        val windowViewFactory = MultiFloatWindowApplication.MultiFloatWindowViewRemoteViewFactory(
//                context,
//                remoteWindowViews,
//                remoteMiniViews,
//                info.windowInlineFrame,
//                info.miniWindowFrame)
//
//        if(update) {
//            info.windowInlineFrame.removeAllViews()
//            info.miniWindowFrame.removeAllViews()
//        }
//        info.windowInlineFrame.addView(
//                windowViewFactory.createWindowView(windowIndex),
//                windowViewFactory.createWindowLayoutParams(windowIndex))
//        info.miniWindowFrame.addView(
//                windowViewFactory.createMinimizedView(windowIndex),
//                windowViewFactory.createMinimizedLayoutParams(windowIndex))
//
//        val factory = MultiFloatWindowApplication.MultiFloatWindowFactory(windowViewFactory, windowSettingsFactory)
//        factoryMap.put(windowIndex, factory)
//    }
    fun openAppWidgetWindow(windowIndex: Int, appWidgetId: Int, update: Boolean) {

        val appWidgetProviderInfo =
                AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)

        val windowViewFactory =
                MultiFloatWindowApplication.MultiFloatWindowViewAppWidgetFactory(context, appWidgetHost, appWidgetId, appWidgetProviderInfo)

        val windowSettingsFactory =
                MultiFloatWindowApplication.MultiFloatWindowSettingsAppWidgetFactory(appWidgetProviderInfo)
        val initSettings = windowSettingsFactory.createInitSettings(windowIndex)

        val factory = MultiFloatWindowApplication.MultiFloatWindowFactory(appWidgetProviderInfo.javaClass, windowViewFactory, windowSettingsFactory)
        factoryMap.put(windowIndex, factory)

        val info = if(update)
            update(
                    windowIndex,
                    initSettings.miniMode,
                    initSettings.backgroundColor,
                    initSettings.width,
                    initSettings.height,
                    initSettings.active,
                    appWidgetProviderInfo.provider.className)
        else
            add(
                    windowIndex,
                    initSettings.miniMode,
                    initSettings.x,
                    initSettings.y,
                    initSettings.backgroundColor,
                    initSettings.width,
                    initSettings.height,
                    initSettings.active,
                    appWidgetProviderInfo.provider.className)

        if(update) {
            info.windowInlineFrame.removeAllViews()
            info.miniWindowFrame.removeAllViews()
        }
        info.windowInlineFrame.addView(
                windowViewFactory.createWindowView(windowIndex),
                windowViewFactory.createWindowLayoutParams(windowIndex))
        info.miniWindowFrame.addView(
                windowViewFactory.createMinimizedView(windowIndex),
                windowViewFactory.createMinimizedLayoutParams(windowIndex))
    }
}