package jp.kght6123.multiwindow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import jp.kght6123.multiwindow.recycler.CardAppListRecyclerView
import jp.kght6123.multiwindow.recycler.IconAppListRecyclerView
import jp.kght6123.multiwindow.utils.UnitUtils
import jp.kght6123.multiwindow.viewgroup.MultiFloatWindowOverlayLayout
import kotlin.concurrent.withLock

/**
 * マルチウィンドウ情報のコントロールを行う
 *
 * Created by kght6123 on 2017/07/11.
 */
class MultiFloatWindowManager(val context: Context) {

    val TAG = this.javaClass.name

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

    val activeFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or //座標系をスクリーンに合わせる
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

    val activeDimAmount = 0.09f
    val activeAlpha = 0.95f

    val inactiveFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE// or
    //WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR

    val inactiveAlpha = 0.5f
    val inactiveDimAmount = 0.0f

    // WindowManagerに設定するレイアウトパラメータ、オーバーレイViewの設定をする
    val params: WindowManager.LayoutParams by lazy {
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

    val ctrlActiveFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
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
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0, // X
                0, // Y
                //WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // なるべく上の階層で表示
                //WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Android-O以降
                ctrlActiveFlags,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.RIGHT// RIGHTをLEFTにすると左寄せ
        params.dimAmount = 0.0f
        params.alpha = 0.90f
        params
    }

    private var mode: ControlViewMode = ControlViewMode.MINI_ICON

    val cardAppListParams by lazy {
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        params
    }
    var cardAppListView: CardAppListRecyclerView = createCardAppListView()

    val iconAppListParams by lazy {
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                UnitUtils.convertDp2Px(75f, context).toInt())
        params
    }
    var iconAppListView: IconAppListRecyclerView = IconAppListRecyclerView(context)

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
        val cardAppListView = CardAppListRecyclerView(context, this)
        return cardAppListView
    }

    private fun getActiveParams(): WindowManager.LayoutParams {
        params.flags = activeFlags
        params.dimAmount = activeDimAmount
        params.alpha = activeAlpha
        return params
    }

    private fun getInActiveParams(): WindowManager.LayoutParams {
//        params.width = WindowManager.LayoutParams.WRAP_CONTENT
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.flags = inactiveFlags
        params.dimAmount = inactiveDimAmount
        params.alpha = inactiveAlpha
        return params
    }

    val overlayWindowMap: MutableMap<String, MultiFloatWindowInfo> = LinkedHashMap()

    init {
        windowManager.addView(overlayView, getActiveParams())   // WindowManagerに追加
        windowManager.addView(controlLayer, ctrlIconParams)
    }
    private fun indexToName(index: Int): String {
        return "${context.packageName}.$index"
    }
    fun add(index: Int, miniMode: Boolean, x: Int?, y: Int?, backgroundColor: Int, initWidth: Int, initHeight: Int, initActive: Boolean): MultiFloatWindowInfo {
        val name = indexToName(index)
        val overlayWindowInfo = MultiFloatWindowInfo(context, this, name, miniMode, backgroundColor, initWidth, initHeight)
        put(name, overlayWindowInfo, initActive)
        moveFixed(name, x!!, y!!)
        return overlayWindowInfo
    }
    fun update(index: Int, miniMode: Boolean, backgroundColor: Int, initWidth: Int, initHeight: Int, initActive: Boolean): MultiFloatWindowInfo {
        val name = indexToName(index)
        val overlayInfo = MultiFloatWindowInfo(context, this, name, miniMode, backgroundColor, initWidth, initHeight)

        val overlayInfoOld = overlayWindowMap.getValue(name)
        overlayWindowMap.put(name, overlayInfo)  // 管理を上書き

        overlayView.removeView(overlayInfoOld.getActiveOverlay())
        overlayView.addView(overlayInfo.getActiveOverlay())
        //overlayView.updateViewLayout(overlayInfo.getActiveOverlay(), FrameLayout.LayoutParams(overlayView.layoutParams))

        if(initActive)
        //updateActive(name)  // 追加したWindowをActiveに
        //updateOtherDeActive(name)  // 追加したWindow以外をDeActiveに
            changeActive(name)

        return overlayInfo
    }
    private fun put(name: String, overlayInfo: MultiFloatWindowInfo, active: Boolean) {
        if(overlayWindowMap[name] != null)
            return

        overlayWindowMap.put(name, overlayInfo)  // 管理に追加
        overlayView.addView(overlayInfo.getActiveOverlay())

        if(active)
            //updateActive(name)  // 追加したWindowをActiveに
            //updateOtherDeActive(name)  // 追加したWindow以外をDeActiveに
            changeActive(name)
    }
    private fun moveFixed(name: String, x: Int, y: Int) {
        val overlayInfo = overlayWindowMap[name]
        if(overlayInfo != null) {
            val params = overlayInfo.getActiveWindowLayoutParams()
            params.leftMargin = x
            params.topMargin = y
            overlayInfo.getActiveOverlay().layoutParams = params
        }
    }
    fun remove(index: Int) {
        remove(indexToName(index))
    }
    private fun remove(name: String) {
         val overlayInfo = overlayWindowMap[name]
        if(overlayInfo != null){
            Log.d(TAG, "remove is not null. name=$name")
            overlayWindowMap.remove(name)
            overlayView.removeView(overlayInfo.getActiveOverlay())
        } else
            Log.d(TAG, "remove is null. name=$name")
        updateDeActive(name)
    }
    fun changeMode(name: String, miniMode: Boolean) {
        val overlayInfo = overlayWindowMap[name]
        if(overlayInfo != null){
            remove(name)
            overlayInfo.miniMode = miniMode
            put(name, overlayInfo, true)
        }
    }

    private fun changeActive(name: String?) {
        if(name == null) return

        val overlayInfo = overlayWindowMap[name]
        if(overlayInfo != null) {
            //remove(name)
            //put(name, overlayInfo)
            updateActive(name)  // 追加したWindowをActiveに
            updateOtherDeActive(name)  // 追加したWindow以外をDeActiveに
        }
    }
    private fun updateActive(name: String) {
        val overlayInfo = overlayWindowMap[name]
        overlayInfo?.activeFlag = true
        onActive(overlayInfo!!)
    }
    private fun updateDeActive(name: String) {
        overlayWindowMap.forEach { entry ->
            if(entry.key == name) {
                entry.value.activeFlag = false
                onDeActive(entry.value)
            }
        }
    }
    private fun updateOtherDeActive(name: String) {
        overlayWindowMap.forEach { entry ->
            if(entry.key != name) {
                entry.value.activeFlag = false
                onDeActive(entry.value)
            }
        }
    }
    fun changeForciblyActiveEvent() {
        if(!overlayWindowMap.isEmpty()) {
            changeActive(overlayWindowMap.keys.last())
            windowManager.updateViewLayout(overlayView, getActiveParams())
        }
    }
//    fun nextForciblyActiveThumb() :Bitmap? {
//        if(!overlayWindowMap.isEmpty()) {
//            val windowInlineFrame = overlayWindowMap.values.last().windowInlineFrame
//            windowInlineFrame.isDrawingCacheEnabled = true
//            windowInlineFrame.destroyDrawingCache();
//            return windowInlineFrame.drawingCache
//        }
//        return null
//    }
    fun getThumb(seq: Int) :Bitmap? {
        val windowInlineFrame = getMultiFloatWindowInfo(seq)?.windowInlineFrame
        windowInlineFrame?.isDrawingCacheEnabled = true
        windowInlineFrame?.destroyDrawingCache()
        return windowInlineFrame?.drawingCache
    }
    fun changeActive(seq: Int) {
        changeActive(getMultiFloatWindowInfo(seq)?.name)
    }
    fun removeSeq(seq: Int) {
        getMultiFloatWindowInfo(seq)?.name?.let { remove(it) }
    }
    private fun getMultiFloatWindowInfo(seq: Int) :MultiFloatWindowInfo? {
        if(seq < overlayWindowMap.size) {
            return overlayWindowMap.values.toList()[seq]
        }
        return null
    }
    private fun changeActiveEvent(event: MotionEvent) :Boolean {
        var changeActiveName: String? = null
        for ((overlayName, overlayInfo) in overlayWindowMap) {
            val onTouch = overlayInfo.isOnTouchEvent(event)
            if (!onTouch/* || changeActiveName != null*/) {
                // タッチされていない、他をActive済
                updateDeActive(overlayName)
            } else if (!overlayInfo.activeFlag) {
                // タッチされ、Active以外、他をActiveにしていない
                changeActiveName = overlayName
            } else if (overlayInfo.activeFlag) {
                // タッチされ、Active、他をActiveにしていない
                changeActiveName = ""
            }
        }
        if(changeActiveName == null) {
            // nullの時、何もタッチされてないので、全体をinActiveへ
            windowManager.updateViewLayout(overlayView, getInActiveParams())
            onDeActiveAll()
        }
        else if(changeActiveName != "") {
            // 他のinActiveなウィンドウをタッチされた時、Activeへ
            changeActive(changeActiveName)
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

    var onActiveEvent: ChangeActiveEventListener? = null
    var onDeActiveEvent: ChangeActiveEventListener? = null
    var onDeActiveAllEvent: AllChangeActiveEventListener? = null

    fun onActive(info: MultiFloatWindowInfo) {
        if(mode == ControlViewMode.MINI_ICON)
            controlLayer.setBackgroundResource(R.drawable.shape_rounded_corners)
        else
            cardAppListView.adapter?.notifyDataSetChanged()

        onActiveEvent?.onChange(info)
    }
    fun onDeActive(info: MultiFloatWindowInfo) {
        if(mode == ControlViewMode.APP_LIST)
            cardAppListView.adapter?.notifyDataSetChanged()

        onDeActiveEvent?.onChange(info)
    }
    fun onDeActiveAll() {
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
}