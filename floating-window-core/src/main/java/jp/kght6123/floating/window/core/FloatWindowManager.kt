package jp.kght6123.floating.window.core

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import jp.kght6123.floating.window.core.recycler.CardAppListRecyclerView
import jp.kght6123.floating.window.core.recycler.IconAppListRecyclerView
import jp.kght6123.floating.window.core.utils.DisplayUtils
import jp.kght6123.floating.window.core.viewgroup.FloatWindowOverlayLayout
import jp.kght6123.floating.window.framework.FloatWindowApplication
import jp.kght6123.floating.window.framework.MultiFloatWindowConstants
import jp.kght6123.floating.window.framework.MultiFloatWindowManagerUpdater
import jp.kght6123.floating.window.framework.utils.UnitUtils
import java.util.*
import kotlin.concurrent.withLock

/**
 * マルチウィンドウ情報のコントロールを行う
 *
 * @author    kght6123
 * @copyright 2017/07/11 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowManager(val context: Context): MultiFloatWindowManagerUpdater {

    private val tag = this.javaClass.name

    private enum class ControlViewMode {
        MINI_ICON, APP_LIST,
    }

    val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val overlayView: FloatWindowOverlayLayout by lazy {
        val overlayView = View.inflate(context, R.layout.window_overlay_layer, null) as FloatWindowOverlayLayout

        overlayView.onDispatchTouchEventListener = View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(tag, "overlayView onDispatchTouchEventListener onTouch MotionEvent.ACTION_DOWN changeActiveEvent. updateDeActive. _activeFlag")
                    return@OnTouchListener changeActiveEvent(event)// Activeがある時のみ。伝搬を止める
                }
            }
            false
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
                MultiFloatWindowConstants.WINDOW_MANAGER_OVERLAY_TYPE,
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
                MultiFloatWindowConstants.WINDOW_MANAGER_OVERLAY_TYPE,
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
                MultiFloatWindowConstants.WINDOW_MANAGER_OVERLAY_TYPE,
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
                View.inflate(context, R.layout.control_layer, null).findViewById(R.id.controlLayer) as FloatWindowOverlayLayout

        val controlIconLayer = controlLayer.findViewById(R.id.controlIconLayer) as LinearLayout

        val controlAppListLayer = controlLayer.findViewById(R.id.controlAppListLayer) as LinearLayout
        controlAppListLayer.addView(iconAppListView, iconAppListParams)
        controlAppListLayer.addView(cardAppListView, cardAppListParams)
        controlAppListLayer.visibility = View.GONE

        controlLayer.onDispatchTouchEventListener = object: View.OnTouchListener {

            val gestureDetector: GestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    Log.d(tag, "controlLayer SimpleOnGestureListener onSingleTapConfirmed")
                    this@FloatWindowManager.changeForciblyActiveEvent()
                    return false
                }

                val lock = java.util.concurrent.locks.ReentrantLock()

                var initialX = 0
                var initialY = 0

                override fun onDown(e: MotionEvent?): Boolean {
                    Log.d(tag, "controlLayer SimpleOnGestureListener onDown")
                    initialX = ctrlIconParams.x
                    initialY = ctrlIconParams.y
                    return false
                }

                override fun onLongPress(e: MotionEvent?) {
                    Log.d(tag, "controlLayer SimpleOnGestureListener onLongPress")
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
                    Log.d(tag, "controlLayer SimpleOnGestureListener onDoubleTapEvent")
                    lock.withLock {
                        mode = ControlViewMode.APP_LIST

                        controlLayer.setBackgroundResource(R.color.app_list_background_color)
                        controlIconLayer.visibility = View.GONE
                        controlAppListLayer.visibility = View.VISIBLE
                        controlAppListLayer.minimumWidth = UnitUtils.convertDp2Px(200f, context).toInt()

                        windowManager.updateViewLayout(controlLayer, ctrlListParams)

                        // 切り替え時にリストを更新する
                        cardAppListView.adapter?.notifyDataSetChanged()
                        iconAppListView.adapter?.notifyDataSetChanged()
                    }
                    return false
                }

                override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                    Log.d(tag, "controlLayer SimpleOnGestureListener onScroll distanceX=$distanceX, distanceY=$distanceY")
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
                        Log.d(tag, "controlLayer onDispatchTouchEventListener onTouch MotionEvent.ACTION_OUTSIDE")
                        return false
                    }
                }
                Log.d(tag, "controlLayer onDispatchTouchEventListener onTouch .action=${event.action}, .rawX,rawY=${event.rawX},${event.rawY} .x,y=${event.x},${event.y}")

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

    val overlayWindowMap: MutableMap<Int, FloatWindowInfo> = LinkedHashMap()
    val factoryMap: MutableMap<Int, FloatWindowApplication.MultiFloatWindowFactory> = LinkedHashMap()

    private val appWidgetHost by lazy {
        val appWidgetHost = AppWidgetHost(context, MultiFloatWindowConstants.APP_WIDGET_HOST_ID)
        appWidgetHost.startListening()
        appWidgetHost
    }

    init {
        windowManager.addView(overlayView, getActiveParams())   // WindowManagerに追加
        windowManager.addView(controlLayer, ctrlIconParams)
    }
    private fun add(index: Int, initSettings: FloatWindowApplication.MultiFloatWindowInitSettings, className: String): FloatWindowInfo {
        Log.d(tag, "index=$index, miniMode=${initSettings.miniMode}, initActive=${initSettings.active}")
        val overlayWindowInfo = FloatWindowInfo(context, this, index, initSettings, className)
        put(index, overlayWindowInfo, initSettings.active)
        moveFixed(index,
                if (initSettings.x > 0) initSettings.x else (DisplayUtils.defaultDisplaySize(context).x - initSettings.width) / 2,
                if (initSettings.y > 0) initSettings.y else (DisplayUtils.defaultDisplaySize(context).y - initSettings.height) / 2)
        return overlayWindowInfo
    }
    private fun update(index: Int, initSettings: FloatWindowApplication.MultiFloatWindowInitSettings, className: String): FloatWindowInfo {
        val overlayInfo = FloatWindowInfo(context, this, index, initSettings, className)

        val overlayInfoOld = overlayWindowMap.getValue(index)
        overlayWindowMap.put(index, overlayInfo)  // 管理を上書き

        if(overlayInfoOld.miniMode) {
            overlayInfo.updateMiniMode()
        } else {
            overlayView.removeView(overlayInfoOld.windowOutlineFrame)
            overlayView.addView(overlayInfo.windowOutlineFrame)
        }
        if(initSettings.active)
            changeActiveIndex(index)

        return overlayInfo
    }
    private fun put(index: Int, overlayInfo: FloatWindowInfo, active: Boolean) {
        if(overlayWindowMap[index] != null)
            return

        overlayWindowMap.put(index, overlayInfo)  // 管理に追加

        if(overlayInfo.miniMode) {
            overlayInfo.addMiniMode(true)
        } else {
            overlayView.addView(overlayInfo.windowOutlineFrame)
        }
        overlayInfo.addAnchor()

        if(active)
            changeActiveIndex(index)
    }
    private fun moveFixed(index: Int, x: Int, y: Int) {
        val overlayInfo = overlayWindowMap[index]
        if(overlayInfo != null) {
            if(overlayInfo.miniMode) {
                overlayInfo.updateMiniModePosition(x, y)
                overlayInfo.updateMiniMode()
            } else {
                val params = overlayInfo.getWindowLayoutParams()
                params.leftMargin = x
                params.topMargin = y
                overlayInfo.windowOutlineFrame.layoutParams = params
                overlayInfo.updateAnchorLayerPosition()
            }
        }
    }
    fun remove(index: Int, factory: Boolean) {
         val overlayInfo = overlayWindowMap[index]
        if(overlayInfo != null){
            Log.d(tag, "remove is not null. index=$index")
            overlayWindowMap.remove(index)

            if(overlayInfo.miniMode) {
                overlayInfo.removeMiniMode(true)
            } else {
                overlayView.removeView(overlayInfo.windowOutlineFrame)
            }
            overlayInfo.removeAnchor()

            if(factory)
                factoryMap.remove(index)
        } else
            Log.d(tag, "remove is null. index=$index")

        Log.d(tag, "remove. updateDeActive. index=$index, _activeFlag")
        updateDeActive(index)
    }
    fun changeMode(index: Int, miniMode: Boolean) {
        val overlayInfo = overlayWindowMap[index]
        if(overlayInfo != null){
            remove(index, false)
            overlayInfo.miniMode = miniMode // 現在のウィンドウの状態（最大化、最小化）を保持
            put(index, overlayInfo, true)

            // ウィンドウの状態変更（最大化、最小化）のイベントトリガーを作成
            factoryMap[index]?.onChangeMode(overlayInfo.miniMode)
        }
    }
    fun nextIndex(): Int {
        val indexList = ArrayList(factoryMap.keys)

        return if(indexList.isEmpty())
            (0 + 1)
        else {
            indexList.reverse()
            (indexList.first() + 1)
        }
    }
    fun changeActiveIndex(index: Int) {
        Log.d(tag, "changeActiveIndex. index=$index") // TODO changeForciblyActiveEvent index=1 or 2

        val overlayInfo = overlayWindowMap[index]
        if(overlayInfo != null) {
            // 対象のViewを一番上へ移動する
            if(overlayInfo.miniMode) {
                overlayInfo.removeMiniMode(false)
                overlayInfo.addMiniMode(false)
            } else {
                overlayView.removeView(overlayInfo.windowOutlineFrame)
                overlayView.addView(overlayInfo.windowOutlineFrame)
            }
            Log.d(tag, "changeActiveIndex. updateActive. index=$index, _activeFlag")
            updateActive(index)  // 追加したWindowをActiveに

            Log.d(tag, "changeActiveIndex. updateOtherDeActive. index!=$index, _activeFlag")
            updateOtherDeActive(index)  // 追加したWindow以外をDeActiveに
        }
    }
    private fun updateActive(index: Int) {
        Log.d(tag, "updateActive. index=$index")
        val overlayInfo = overlayWindowMap[index]
        Log.d(tag, "updateActive. index=$index, _activeFlag=true")
        overlayInfo?.activeFlag = true
        overlayInfo?.let { onActive(index, it) }
    }
    private fun updateDeActive(index: Int) {
        Log.d(tag, "updateDeActive. index=$index")
        val overlayInfo = overlayWindowMap[index]
        Log.d(tag, "updateDeActive. index=$index, _activeFlag=false")
        overlayInfo?.activeFlag = false
        overlayInfo?.let { onDeActive(index, it) }
    }
    private fun updateOtherDeActive(index: Int) {
        Log.d(tag, "updateOtherDeActive. index=$index")
        overlayWindowMap.forEach { entry ->
            if(entry.key != index) {
                Log.d(tag, "updateOtherDeActive. index=${entry.key}, _activeFlag=false")
                entry.value.activeFlag = false
                onDeActive(entry.key, entry.value)
            }
        }
    }
    fun changeForciblyActiveEvent() {
        Log.d(tag, "changeForciblyActiveEvent.")
        if(!overlayWindowMap.isEmpty()) {
            run exit@ {
                overlayWindowMap.entries.reversed().forEach { (index, overlayInfo) ->
                    if(!overlayInfo.miniMode) {
                        changeActiveIndex(index)
                        changeActiveOverlayView()
                        return@exit
                    }
                }
            }
        }
    }
    fun getThumb(seq: Int) :Bitmap? {
        val windowInlineFrame = getMultiFloatWindowInfo(seq)?.windowInlineFrame
        windowInlineFrame?.isDrawingCacheEnabled = true
        windowInlineFrame?.destroyDrawingCache()
        // Copyして返す（いつの間にかrecycleされてしまう為、java.lang.RuntimeException: Canvas: trying to use a recycled bitmap の原因に）
        return windowInlineFrame?.drawingCache?.copy(windowInlineFrame.drawingCache.config, true)
    }
    fun changeActiveSeq(seq: Int) {
        Log.d(tag, "changeActiveSeq. seq=$seq")
        getMultiFloatWindowInfo(seq)?.index?.let { changeActiveIndex(it) }
    }
    fun removeSeq(seq: Int) {
        getMultiFloatWindowInfo(seq)?.index?.let { remove(it, true) }
    }
    override fun setMinWidth(seq: Int, value: Int) {
        getMultiFloatWindowInfo(seq)?.minWidth = value
    }
    override fun setMinHeight(seq: Int, value: Int) {
        getMultiFloatWindowInfo(seq)?.minHeight = value
    }
    override fun setMaxWidth(seq: Int, value: Int) {
        getMultiFloatWindowInfo(seq)?.maxWidth = value
    }
    override fun setMaxHeight(seq: Int, value: Int) {
        getMultiFloatWindowInfo(seq)?.maxHeight = value
    }
    override fun setResize(seq: Int, value: Boolean) {
        getMultiFloatWindowInfo(seq)?.resize = value
    }
    private fun getMultiFloatWindowInfo(seq: Int) : FloatWindowInfo? {
        if(seq < overlayWindowMap.size) {
            return overlayWindowMap.values.toList()[seq]
        }
        return null
    }

    private var prevActiveIndex: Int = -2
    private fun changeActiveEvent(event: MotionEvent) :Boolean {
        val deActiveAll = isDeActiveAll()
        var changeActiveIndex: Int = -2
        for ((overlayIndex, overlayInfo) in overlayWindowMap) {
            if(!overlayInfo.miniMode) {
                val onTouch = overlayInfo.isOnTouchEvent(event)
                if (!onTouch/* || changeActiveName != null*/) {
                    // タッチされていない、他をActive済
                    Log.d(tag, "changeActiveEvent. 1-1 onTouch=$onTouch, overlayInfo.activeFlag=${overlayInfo.activeFlag}, overlayIndex=$overlayIndex")
                    if (overlayInfo.activeFlag)
                        prevActiveIndex = overlayIndex  // ActiveだったIndexを保持

                    Log.d(tag, "changeActiveEvent. updateDeActive. index=$overlayIndex, _activeFlag")
                    updateDeActive(overlayIndex)
                } else if (!overlayInfo.activeFlag) {
                    // タッチされ、Active以外、他をActiveにしていない
                    Log.d(tag, "changeActiveEvent. 1-2 onTouch=$onTouch, overlayInfo.activeFlag=${overlayInfo.activeFlag}, overlayIndex=$overlayIndex")
                    changeActiveIndex = overlayIndex
                } else if (overlayInfo.activeFlag) {
                    // タッチされ、Active、他をActiveにしていない
                    Log.d(tag, "changeActiveEvent. 1-3 onTouch=$onTouch, overlayInfo.activeFlag=${overlayInfo.activeFlag}, overlayIndex=$overlayIndex")
                    changeActiveIndex = -1
                }
            }
        }
        when {
            changeActiveIndex == -2 && deActiveAll -> {
                // すでに全体がinActive
                return false
            }
            changeActiveIndex == -2 -> {
                // nullの時、何もタッチされてないので、全体をinActiveへ
                Log.d(tag, "changeActiveEvent. 1-4 changeActiveIndex=$changeActiveIndex")
                windowManager.updateViewLayout(overlayView, getInActiveParams())
                onDeActiveAll()
                if(prevActiveIndex != -2)
                // 復帰のために、Anchorだけ表示する
                    overlayWindowMap[prevActiveIndex]?.addAnchor()
                return false
            }
            changeActiveIndex != -1 -> {
                // 他のinActiveなウィンドウをタッチされた時、Activeへ
                Log.d(tag, "changeActiveEvent. 1-5 changeActiveIndex=$changeActiveIndex")
                changeActiveIndex(changeActiveIndex)
                changeActiveOverlayView()
                return true
            }
            else -> return true
        }
    }
    private fun isDeActiveAll() :Boolean {
        if (overlayWindowMap.isEmpty())
            return false // Mapが空のときは、全て非アクティブとしない

        for ((_, overlayInfo) in overlayWindowMap)
            if (overlayInfo.activeFlag)
                return false

        return true
    }
    fun changeActiveOverlayView() {
        windowManager.updateViewLayout(overlayView, getActiveParams())
    }
    fun finish() {
        for ((overlayName, _) in overlayWindowMap.toList()) {
            this.remove(overlayName, true)
        }
        windowManager.removeViewImmediate(overlayView)
        windowManager.removeViewImmediate(controlLayer)
    }

    private var onActiveEvent: ChangeActiveEventListener? = null
    private var onDeActiveEvent: ChangeActiveEventListener? = null
    private var onDeActiveAllEvent: AllChangeActiveEventListener? = null

    private fun onActive(index: Int, info: FloatWindowInfo) {
        Log.d(tag, "onActive. index=$index")

        if(mode == ControlViewMode.MINI_ICON)
            controlLayer.setBackgroundResource(R.drawable.shape_rounded_corners)
        else
            cardAppListView.adapter?.notifyDataSetChanged()

        onActiveEvent?.onChange(info)

        // ウィンドウへのフォーカスイベントをトリガー
        factoryMap[index]?.onActive()
    }
    private fun onDeActive(index: Int, info: FloatWindowInfo) {
        Log.d(tag, "onDeActive. index=$index")

        if(mode == ControlViewMode.APP_LIST)
            cardAppListView.adapter?.notifyDataSetChanged()

        onDeActiveEvent?.onChange(info)

        // ウィンドウへのフォーカスイベントをトリガー
        factoryMap[index]?.onDeActive()
    }
    private fun onDeActiveAll() {
        Log.d(tag, "onDeActiveAll.")

        if(mode == ControlViewMode.MINI_ICON)
            controlLayer.setBackgroundResource(R.drawable.shape_rounded_corners_accent)
        else
            cardAppListView.adapter?.notifyDataSetChanged()

        onDeActiveAllEvent?.onChangeAll()

        // ウィンドウへのフォーカスイベントをトリガー
        for ((_, factory) in factoryMap.toList()) {
            factory.onDeActiveAll()
        }
    }

    interface ChangeActiveEventListener {
        fun onChange(info: FloatWindowInfo)
    }

    interface AllChangeActiveEventListener {
        fun onChangeAll()
    }

    fun openWindow(windowIndex: Int, packageName: String, serviceClassName: String, update: Boolean, paramMap: Map<String, Any>) {

        val sharedContext = context.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
        val classObj = Class.forName(serviceClassName, true, sharedContext.classLoader)
        val application = classObj.newInstance()

        val attachBaseContextMethod =
                classObj.getMethod("attachBaseContext", Context::class.java, Context::class.java)
        val attachManagerMethod =
                classObj.getMethod("attachManager", Any::class.java)
        val onCreateFactoryMethod =
                classObj.getMethod("onCreateFactory", Int::class.java)
        val onCreateSettingsFactoryMethod =
                classObj.getMethod("onCreateSettingsFactory", Int::class.java)

        attachBaseContextMethod.invoke(application, sharedContext, context)
        attachManagerMethod.invoke(application, this)

        val factory = FloatWindowApplication.MultiFloatWindowFactory(
                classObj,
                onCreateFactoryMethod.invoke(application, windowIndex),
                onCreateSettingsFactoryMethod.invoke(application, windowIndex)
        )
        factoryMap.put(windowIndex, factory)

        val initSettings = factory.createInitSettings(windowIndex)
        initSettings.updateParamMap(paramMap)

        val info =
                if(update)
                    update(
                            windowIndex,
                            initSettings,
                            classObj.name)
                else
                    add(
                            windowIndex,
                            initSettings,
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
    fun openAppWidgetWindow(windowIndex: Int, appWidgetId: Int, update: Boolean) {

        val appWidgetProviderInfo =
                AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId)

        val application = object: FloatWindowApplication(){
            override fun onBind(p0: Intent?): IBinder {
                TODO("not implemented")
            }
            override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
                TODO("not implemented")
            }
            override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
                TODO("not implemented")
            }
        }
        application.attachBaseContext(context, context)
        application.attachManager(this)

        val windowViewFactory =
                FloatWindowApplication.MultiFloatWindowViewAppWidgetFactory(
                        application.multiWindowContext,
                        appWidgetHost,
                        appWidgetId,
                        appWidgetProviderInfo)

        val windowSettingsFactory =
                FloatWindowApplication.MultiFloatWindowSettingsAppWidgetFactory(
                        application.multiWindowContext,
                        appWidgetProviderInfo)
        val initSettings = windowSettingsFactory.createInitSettings(windowIndex)

        val factory = FloatWindowApplication.MultiFloatWindowFactory(
                appWidgetProviderInfo.javaClass,
                windowViewFactory,
                windowSettingsFactory)

        factoryMap.put(windowIndex, factory)

        val info = if(update)
            update(
                    windowIndex,
                    initSettings,
                    appWidgetProviderInfo.provider.className)
        else
            add(
                    windowIndex,
                    initSettings,
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