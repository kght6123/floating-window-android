package jp.kght6123.floating.window.core.layer

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.util.Log
import android.view.*
import jp.kght6123.floating.window.core.FloatWindowInfo
import jp.kght6123.floating.window.core.viewgroup.FloatWindowOverlayLayout
import jp.kght6123.floating.window.framework.MultiFloatWindowConstants
import jp.kght6123.floating.window.framework.gesture.LongClickOnGestureListener
import jp.kght6123.floating.window.framework.utils.UnitUtils

/**
 *
 * @author    kght6123
 * @copyright 2017/12/03 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
abstract class AnchorLayer(private val position: Position, private val info: FloatWindowInfo) {

    private val tag = this.javaClass.name

    enum class Position {
        TOP, BOTTOM, LEFT, RIGHT
    }

    protected abstract fun getAnchorLayoutResource(): Int
    protected abstract fun getAnchorLayerId(): Int
    protected abstract fun getBorderWidth(): Int
    protected abstract fun updatePosition(x: Int, y: Int)
    abstract fun updateBackgroundResource(resId: Int, alpha: Float)
    protected abstract fun updateStroke(event: MotionEvent)

    //private val borderWidth = UnitUtils.convertDp2Px(24f, info.context).toInt()
    protected val shadowWidth = 28

    private val anchorActiveFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

    protected val anchorLayerParams: WindowManager.LayoutParams by lazy {
        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,//UnitUtils.convertDp2Px(150f, context).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT,//UnitUtils.convertDp2Px(5f, context).toInt(),
                0, // X
                0, // Y
                MultiFloatWindowConstants.WINDOW_MANAGER_OVERLAY_TYPE,
                anchorActiveFlags,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START
        params.dimAmount = 0.0f
        params.alpha = 0.75f
        params
    }

    protected val anchorLayer: FloatWindowOverlayLayout by lazy {
        val anchorLayer =
                View.inflate(info.context, getAnchorLayoutResource(), null).findViewById(getAnchorLayerId()) as FloatWindowOverlayLayout

        anchorLayer.onDispatchTouchEventListener = object: View.OnTouchListener {

            val longClickDetector: LongClickOnGestureListener by lazy {
                LongClickOnGestureListener(object: LongClickOnGestureListener.OnLongClickListener{
                    override fun onLongClick(event: MotionEvent, view: View) {
                        if(info.strokeMode != FloatWindowInfo.Stroke.UNKNOWN /*&& info.windowMode == FloatWindowInfo.Mode.UNKNOWN*/ && info.resize) {
                            // リサイズモードの背景色に変える
                            info.updateAnchorColor(info.strokeMode)
                            info.windowMode = FloatWindowInfo.Mode.RESIZE
                        }
                    }
                }, 250L, UnitUtils.convertDp2Px(6f, info.context))
            }

            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialWidth: Int = 0
            private var initialHeight: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f

            val gestureDetector: GestureDetector = GestureDetector(info.context, object: GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    Log.d(tag, "anchorLayer SimpleOnGestureListener onSingleTapConfirmed")

                    return false
                }

                private var tempX = 0
                private var tempY = 0
                private var tempWidth = 0
                private var tempHeight = 0

                inner class ChangeSizeModeUpdater(val modeWidth: Int, val modeHeight: Int, val modeX: Int, val modeY: Int) {
                    fun update() {
                        val params = info.getWindowLayoutParams()
                        if(modeWidth != 0 && modeHeight != 0) {
                            if(tempWidth != 0
                                    && tempHeight != 0
                                    && params.width == modeWidth
                                    && params.height == modeHeight) {
                                // 元のサイズが残ってれば元に戻す
                                // 元のサイズに戻す
                                params.width = tempWidth
                                params.height = tempHeight
                                tempWidth = 0
                                tempHeight = 0

                                // 元の位置に戻す
                                params.topMargin = tempX
                                params.leftMargin = tempY
                                tempX = 0
                                tempY = 0

                            } else if(tempWidth != 0
                                    && tempHeight != 0) {
                                // 元のサイズが残ってれば元を更新しない
                                params.width = modeWidth
                                params.height = modeHeight

                                if(modeX != 0 && modeY != 0) {
                                    // 位置指定があれば、その位置に
                                    params.topMargin = modeX
                                    params.leftMargin = modeY

                                } else if(tempX != 0 && tempY != 0) {
                                    // 位置指定がなく元の位置が保持されてれば、元の位置に
                                    params.topMargin = tempX
                                    params.leftMargin = tempY
                                }

                            } else {
                                // 指定された大きさにする
                                tempWidth = params.width
                                tempHeight = params.height
                                params.width = modeWidth
                                params.height = modeHeight

                                // 元の位置は常に保持（元のサイズに戻す時、位置も戻したい）
                                tempX = params.topMargin
                                tempY = params.leftMargin

                                if(modeX != 0 && modeY != 0) {
                                    // 位置指定があれば、その位置に
                                    params.topMargin = modeX
                                    params.leftMargin = modeY
                                }
                            }
                        }
                        info.updateAnchorLayerPosition()
                        info.windowOutlineFrame.layoutParams = params
                    }
                }

                val changeMaxSizeModeUpdater: ChangeSizeModeUpdater = ChangeSizeModeUpdater(info.maxWidth, info.maxHeight, info.notificationBarSize,-1)
                val changeMinSizeModeUpdater: ChangeSizeModeUpdater = ChangeSizeModeUpdater(info.minWidth, info.minHeight,0,0)

                override fun onDoubleTap(event: MotionEvent): Boolean {
                    Log.d(tag, "SimpleOnGestureListener onDoubleTap")
                    Log.d(tag, "SimpleOnGestureListener onDoubleTap ${info.strokeMode}")

                    when (info.strokeMode) {
                        FloatWindowInfo.Stroke.TOP, FloatWindowInfo.Stroke.BOTTOM, FloatWindowInfo.Stroke.LEFT, FloatWindowInfo.Stroke.RIGHT -> {
                            info.switchMiniMode()
                        }
                        FloatWindowInfo.Stroke.TOP_LEFT, FloatWindowInfo.Stroke.TOP_RIGHT -> {
                            // 角のダブルタップで最大化
                            changeMaxSizeModeUpdater.update()
                        }
                        FloatWindowInfo.Stroke.BOTTOM_LEFT, FloatWindowInfo.Stroke.BOTTOM_RIGHT -> {
                            // 角のダブルタップで最小化
                            changeMinSizeModeUpdater.update()
                        }
                        else -> {
                        }
                    }

                    return super.onDoubleTap(event)
                }
            })

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                longClickDetector.onTouchEvent(view, event)

                Log.d(tag, "motionEvent.action = ${event.action}")
                Log.d(tag, "motionEvent.rawX, rawY = ${event.rawX}, ${event.rawY}")
                Log.d(tag, "motionEvent.x, y = ${event.x}, ${event.y}")

                val rx: Float = event.rawX
                val ry: Float = event.rawY

                val params = info.getWindowLayoutParams()
                Log.d(tag, "params.width, height=${params.width}, ${params.height}")
                Log.d(tag, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

                when (event.action) {
                    MotionEvent.ACTION_CANCEL -> {

                    }
                    MotionEvent.ACTION_DOWN -> {

                        // Activeに
                        if(!info.activeFlag) {
                            info.manager.changeActiveIndex(info.index)
                            info.manager.changeActiveOverlayView()
                        }

                        // モードのリセット
                        info.windowMode = FloatWindowInfo.Mode.UNKNOWN
                        info.strokeMode = FloatWindowInfo.Stroke.UNKNOWN

                        // モード更新
                        updateStroke(event)

                        Log.d(tag, "dispatchTouchEvent ${info.strokeMode} borderWidth=${getBorderWidth()} position=${this@AnchorLayer.position}")

                        if(info.strokeMode != FloatWindowInfo.Stroke.UNKNOWN) {
                            // 移動・拡大縮小のための初期値設定
                            initialX = params.leftMargin
                            initialY = params.topMargin
                            initialWidth = params.width
                            initialHeight = params.height
                            initialTouchX = rx
                            initialTouchY = ry

                            info.updateAnchorColor(FloatWindowInfo.Stroke.ALL)
                        }
                    }
                    MotionEvent.ACTION_UP -> {

                        if(info.windowMode == FloatWindowInfo.Mode.MOVE && info.strokeMode != FloatWindowInfo.Stroke.UNKNOWN) {
                            // 移動完了
                            Log.d(tag, "displaySize.x, y = ${info.defaultDisplaySize.x}, ${info.defaultDisplaySize.y}")
                            Log.d(tag, "motionEvent.rawX, rawY = $rx, $ry")
                            Log.d(tag, "motionEvent.x, y = ${event.x}, ${event.y}")
                            Log.d(tag, "params.width, height=${params.width}, ${params.height}")
                            Log.d(tag, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

                            info.windowMode = FloatWindowInfo.Mode.FINISH    // フリック誤作動防止
                        }
                        if(info.windowMode == FloatWindowInfo.Mode.RESIZE && info.strokeMode != FloatWindowInfo.Stroke.UNKNOWN) {
                            info.windowMode = FloatWindowInfo.Mode.FINISH    // フリック誤作動防止
                        }
                        if(info.strokeMode != FloatWindowInfo.Stroke.UNKNOWN) {
                            // ACTION_UP,DOWNのみの対策。
                            info.updateAnchorColor(FloatWindowInfo.Stroke.UNKNOWN)
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {

                        if(info.windowMode != FloatWindowInfo.Mode.RESIZE && info.strokeMode != FloatWindowInfo.Stroke.UNKNOWN) {
                            // 移動中の処理
                            Log.d(tag, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

                            params.leftMargin = initialX + (rx - initialTouchX).toInt()
                            params.topMargin = initialY + (ry - initialTouchY).toInt()

                            info.windowMode = FloatWindowInfo.Mode.MOVE
                        }
                        if(info.windowMode == FloatWindowInfo.Mode.RESIZE && info.strokeMode != FloatWindowInfo.Stroke.UNKNOWN && info.resize) {

                            val limitWidth = fun(): Boolean {
                                return when {
                                    info.maxWidth < params.width -> {
                                        params.width = info.maxWidth
                                        true
                                    }
                                    info.minWidth > params.width -> {
                                        params.width = info.minWidth
                                        true
                                    }
                                    else -> false
                                }
                            }
                            val limitHeight = fun(): Boolean {
                                return when {
                                    info.maxHeight < params.height -> {
                                        params.height = info.maxHeight
                                        true
                                    }
                                    info.minHeight > params.height -> {
                                        params.height = info.minHeight
                                        true
                                    }
                                    else -> false
                                }
                            }
                            val resizeTop = fun(){
                                params.height = (initialHeight - (ry - initialTouchY).toInt())
                                if(!limitHeight())
                                    params.topMargin = initialY + (ry - initialTouchY).toInt()
                            }
                            val resizeBottom = fun(){
                                params.height = (initialHeight + (ry - initialTouchY).toInt())
                                limitHeight()
                            }
                            val resizeLeft = fun(){
                                params.width = (initialWidth - (rx - initialTouchX).toInt())
                                if(!limitWidth())
                                    params.leftMargin = initialX + (rx - initialTouchX).toInt()
                            }
                            val resizeRight = fun(){
                                params.width = (initialWidth + (rx - initialTouchX).toInt())
                                limitWidth()
                            }
                            when(info.strokeMode){
                                FloatWindowInfo.Stroke.TOP -> {
                                    resizeTop()
                                }
                                FloatWindowInfo.Stroke.BOTTOM -> {
                                    resizeBottom()
                                }
                                FloatWindowInfo.Stroke.LEFT -> {
                                    resizeLeft()
                                }
                                FloatWindowInfo.Stroke.RIGHT -> {
                                    resizeRight()
                                }
                                FloatWindowInfo.Stroke.TOP_LEFT -> {
                                    resizeTop()
                                    resizeLeft()
                                }
                                FloatWindowInfo.Stroke.TOP_RIGHT -> {
                                    resizeTop()
                                    resizeRight()
                                }
                                FloatWindowInfo.Stroke.BOTTOM_LEFT -> {
                                    resizeBottom()
                                    resizeLeft()
                                }
                                FloatWindowInfo.Stroke.BOTTOM_RIGHT -> {
                                    resizeBottom()
                                    resizeRight()
                                }
                                else -> {}
                            }
                        }
                        info.updateAnchorLayerPosition()
                        info.windowOutlineFrame.layoutParams = params
                    }
                    MotionEvent.ACTION_OUTSIDE -> {
                        Log.d(tag, "anchorLayer onDispatchTouchEventListener onTouch MotionEvent.ACTION_OUTSIDE")
                        Log.d(tag, "ACTION_OUTSIDE event.x, y = ${event.x}, ${event.y}")
                        Log.d(tag, "ACTION_OUTSIDE event.rawX, rawY = ${event.rawX}, ${event.rawY}")
                        return false
                    }
                }
                Log.d(tag, "anchorLayer onDispatchTouchEventListener onTouch .action=${event.action}, .rawX,rawY=${event.rawX},${event.rawY} .x,y=${event.x},${event.y}")

                gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う

                //return false ここで伝搬を止めないと、ACTION_DOWN以降が動かない
                return true
            }
        }
        anchorLayer
    }

    init {
        anchorLayer
    }
    fun updatePosition(x: Int, y: Int, update: Boolean) {
        updatePosition(x, y)
        if(update)
            info.manager.windowManager.updateViewLayout(anchorLayer, anchorLayerParams)
    }
    fun add() {
        info.manager.windowManager.addView(anchorLayer, anchorLayerParams)
    }
    fun remove() {
        info.manager.windowManager.removeViewImmediate(anchorLayer)
    }
    fun getX() = anchorLayerParams.x
    fun getY() = anchorLayerParams.y
}