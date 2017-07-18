package jp.kght6123.multiwindow

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.*

/**
 * マルチウィンドウ情報のコントロールを行う
 *
 * Created by kght6123 on 2017/07/11.
 */
class MultiFloatWindowManager(val context: Context) {

    val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    val overlayView: MultiFloatWindowOverlayLayout by lazy {
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

    private fun getActiveParams(): WindowManager.LayoutParams {
        params.flags = activeFlags
        params.dimAmount = activeDimAmount
        params.alpha = activeAlpha
        return params
    }

    private fun getInActiveParams(): WindowManager.LayoutParams {
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.flags = inactiveFlags
        params.dimAmount = inactiveDimAmount
        params.alpha = inactiveAlpha
        return params
    }

    val overlayWindowMap: MutableMap<String, MultiFloatWindowInfo> = LinkedHashMap()

    init {
        windowManager.addView(overlayView, getActiveParams())   // WindowManagerに追加
    }
    private fun put(index: Int, miniMode: Boolean, x: Int?, y: Int?, backgroundColor: Int, initWidth: Int, initHeight: Int): MultiFloatWindowInfo {
        val name = "${context.packageName}.$index"
        val overlayWindowInfo = MultiFloatWindowInfo(context, this, name, miniMode, backgroundColor, initWidth, initHeight)
        put(name, overlayWindowInfo)
        moveFixed(name, x!!, y!!)
        return overlayWindowInfo
    }
    private fun put(name: String, overlayInfo: MultiFloatWindowInfo) {
        if(overlayWindowMap[name] != null)
            return

        overlayWindowMap.put(name, overlayInfo)  // 管理に追加
        overlayView.addView(overlayInfo.getActiveOverlay())

        updateActive(name)  // 追加したWindowをActiveに
        updateOtherDeActive(name)  // 追加したWindow以外をDeActiveに
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
    private fun remove(name: String) {
         val overlayInfo = overlayWindowMap[name]
        if(overlayInfo != null){
            overlayWindowMap.remove(name)
            overlayView.removeView(overlayInfo.getActiveOverlay())
        }
        updateDeActive(name)
    }
    fun changeMode(name: String, miniMode: Boolean) {
        val overlayInfo = overlayWindowMap[name]
        if(overlayInfo != null){
            remove(name)
            overlayInfo.miniMode = miniMode
            put(name, overlayInfo)
        }
    }

    private fun changeActive(name: String) {
        val overlayInfo = overlayWindowMap[name]
        if(overlayInfo != null) {
            remove(name)
            put(name, overlayInfo)
        }
    }
    private fun updateActive(name: String) {
        val overlayInfo = overlayWindowMap[name]
        overlayInfo?.activeFlag = true
        overlayInfo?.onActive()
    }
    private fun updateDeActive(name: String) {
        overlayWindowMap.forEach { entry ->
            if(entry.key == name) {
                entry.value.activeFlag = false
                entry.value.onDeActive()
            }
        }
    }
    private fun updateOtherDeActive(name: String) {
        overlayWindowMap.forEach { entry ->
            if(entry.key != name) {
                entry.value.activeFlag = false
                entry.value.onDeActive()
            }
        }
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
        if(changeActiveName == null)
        // nullの時、何もタッチされてないので、全体をinActiveへ
            windowManager.updateViewLayout(overlayView, getInActiveParams())
        else if(changeActiveName != "") {
            // 他のinActiveなウィンドウをタッチされた時、Activeへ
            changeActive(changeActiveName)
            windowManager.updateViewLayout(overlayView, getActiveParams())
        }
        return false
    }
    fun add(index: Int, x: Int?, y: Int?, miniMode: Boolean, backgroundColor: Int, initWidth: Int, initHeight: Int): MultiFloatWindowInfo {
        return put(index, miniMode, x, y, backgroundColor, initWidth, initHeight)// ここでビューをオーバーレイ領域に追加する
    }
    fun finish() {
        for ((overlayName, _) in overlayWindowMap.toList()) {
            this.remove(overlayName)
        }
        windowManager.removeViewImmediate(overlayView)
    }
}