package jp.kght6123.multiwindow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import jp.kght6123.multiwindow.utils.DisplayUtils
import jp.kght6123.multiwindowframework.utils.UnitUtils

/**
 * マルチウィンドウの状態を保持するクラス
 *
 * Created by kght6123 on 2017/07/11.
 */
class MultiFloatWindowInfo(
        val context: Context,
        val manager: MultiFloatWindowManager,
        val index: Int,
        var miniMode: Boolean,
        val backgroundColor: Int,
        private val initWidth: Int,
        private val initHeight: Int,
        val name: String
) {

    private val tag = this.javaClass.name

    private enum class Stroke {
        UNKNOWN, TOP, BOTTOM, LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    }
    private enum class Mode {
        UNKNOWN, MOVE, RESIZE, FINISH
    }

    private var strokeMode: Stroke = Stroke.UNKNOWN
    private var windowMode: Mode = Mode.UNKNOWN

    // ディスプレイのサイズを格納する
    private val defaultDisplaySize: Point by lazy {
        DisplayUtils.defaultDisplaySize(context)
    }

    val miniWindowFrame: ViewGroup by lazy {

        val miniView = LinearLayout(context)

        val layoutParams =
                FrameLayout.LayoutParams(
                        UnitUtils.convertDp2Px(75f, context).toInt(),
                        UnitUtils.convertDp2Px(75f, context).toInt())
        layoutParams.leftMargin = 100
        layoutParams.topMargin = 100

        miniView.layoutParams = layoutParams

        /**
         * 最小化表示も、ウィンドウモードの様に枠外処理など適切に処理する必要がありそう。
         * OverlayWindowLinearLayoutクラスとの共用部分を抜き出して、共通化し、実装を進めていく必要がある。
         *  → そもそも、WindowManagerに複数Viewを追加するのではなく、一つのViewを追加して、その中でマルチウィンドウを実現した方が良い気がする。
         *  → メモリ的に大丈夫なのか・・・？
         *    → ベースが同じServiceなので、WindowManagerに複数追加と変わらない気がする
         */
        miniView.setOnTouchListener(object: View.OnTouchListener {

            val TAG = this.javaClass.name

            var initialX: Int = 0
            var initialY: Int = 0
            var initialTouchX: Float = 0f
            var initialTouchY: Float = 0f
            var windowMode: Mode = Mode.UNKNOWN

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                Log.d(TAG, "miniWindowFrame event.action = ${event.action}")

                val rx: Float = event.rawX
                val ry: Float = event.rawY

                val params = getActiveWindowLayoutParams()

                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        if(windowMode != Mode.MOVE )
                            manager.changeMode(this@MultiFloatWindowInfo.index, false)
                        windowMode = Mode.FINISH
                    }
                    MotionEvent.ACTION_MOVE -> {
                        windowMode = Mode.MOVE

                        // 移動中の処理
                        params.leftMargin = initialX + (rx - initialTouchX).toInt()
                        params.topMargin = initialY + (ry - initialTouchY).toInt()

                        getActiveOverlay().layoutParams = params
                    }
                    MotionEvent.ACTION_DOWN -> {
                        windowMode = Mode.UNKNOWN

                        // 移動・拡大縮小のための初期値設定
                        initialX = params.leftMargin
                        initialY = params.topMargin
                        initialTouchX = rx
                        initialTouchY = ry
                    }
                }
                //return false ここで伝搬を止めないと、ACTION_DOWN以降が動かない
                return true
            }
        })
        miniView
    }
    private val windowOutlineFrame: ViewGroup by lazy {
        val windowOutFrame =
                View.inflate(context, R.layout.multiwindow_frame, null).findViewById(R.id.windowOutlineFrame) as ViewGroup
        windowOutFrame.layoutParams = FrameLayout.LayoutParams(initWidth, initHeight)
        windowOutFrame
    }

    val windowInlineFrame: ViewGroup by lazy {

        val windowInFrame =
                windowOutlineFrame.findViewById(R.id.windowInlineFrame) as ViewGroup
        windowInFrame.setBackgroundColor(backgroundColor)

        windowOutlineFrame.setOnLongClickListener {
            if(strokeMode != Stroke.UNKNOWN && windowMode == Mode.UNKNOWN) {
                // リサイズモードの背景色に変える
                when (strokeMode) {
                    Stroke.TOP -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_top)
                    }
                    Stroke.BOTTOM -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_bottom)
                    }
                    Stroke.LEFT -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_left)
                    }
                    Stroke.RIGHT -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_right)
                    }
                    Stroke.TOP_LEFT -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_top_left)
                    }
                    Stroke.TOP_RIGHT -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_top_right)
                    }
                    Stroke.BOTTOM_LEFT -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_bottom_left)
                    }
                    Stroke.BOTTOM_RIGHT -> {
                        windowInFrame.setBackgroundResource(R.drawable.multiwindow_frame_stroke_bottom_right)
                    }
                    else -> {}
                }
                windowMode = Mode.RESIZE
            }
            return@setOnLongClickListener false
        }
        val switchMiniMode = fun() {
            if(strokeMode != Stroke.UNKNOWN) {
                manager.changeMode(this.index, true)
            }
        }
        windowOutlineFrame.setOnTouchListener(object: View.OnTouchListener {
            var onGestureListener: GestureDetector.OnGestureListener = object: GestureDetector.SimpleOnGestureListener() {

                override fun onDoubleTap(event: MotionEvent): Boolean {
                    Log.d(tag, "SimpleOnGestureListener onDoubleTap")
                    Log.d(tag, "SimpleOnGestureListener onDoubleTap $strokeMode")

                    when (strokeMode) {
                        Stroke.TOP, Stroke.BOTTOM, Stroke.LEFT, Stroke.RIGHT -> {
                            switchMiniMode()
                        }
                        Stroke.TOP_LEFT, Stroke.TOP_RIGHT, Stroke.BOTTOM_LEFT, Stroke.BOTTOM_RIGHT -> {
                            // FIXME
                        }
                        else -> {}
                    }
                    return super.onDoubleTap(event)
                }
            }
            val gestureDetector: GestureDetector by lazy {
                GestureDetector(context, onGestureListener)
            }

            private val strokeWidth: Int = UnitUtils.convertDp2Px(3f, context).toInt() + 40/*ぼかしの分*/

            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialWidth: Int = 0
            private var initialHeight: Int = 0
            private var initialTouchX: Float = 0f
            private var initialTouchY: Float = 0f

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, event: MotionEvent): Boolean {

                Log.d(tag, "motionEvent.action = ${event.action}")
                Log.d(tag, "motionEvent.rawX, rawY = ${event.rawX}, ${event.rawY}")
                Log.d(tag, "motionEvent.x, y = ${event.x}, ${event.y}")

                val rx: Float = event.rawX
                val ry: Float = event.rawY

                val params = getActiveWindowLayoutParams()
                Log.d(tag, "params.width, height=${params.width}, ${params.height}")
                Log.d(tag, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

                when (event.action) {
                    MotionEvent.ACTION_CANCEL -> {

                    }
                    MotionEvent.ACTION_DOWN -> {

                        // モードのリセット
                        windowMode = Mode.UNKNOWN
                        strokeMode = Stroke.UNKNOWN

                        Log.d(tag, "params.width-this.strokeWidth = ${params.width-this.strokeWidth}")
                        Log.d(tag, "params.height-this.strokeWidth = ${params.height-this.strokeWidth}")

                        val left = event.x in 0..this.strokeWidth
                        val right = event.x in (params.width-this.strokeWidth)..params.width
                        val top = event.y in 0..this.strokeWidth
                        val bottom = event.y in (params.height-this.strokeWidth)..params.height

                        if(left){
                            strokeMode = Stroke.LEFT
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(right) {
                            strokeMode = Stroke.RIGHT
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(top) {
                            strokeMode = Stroke.TOP
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(bottom) {
                            strokeMode = Stroke.BOTTOM
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(top && left) {
                            strokeMode = Stroke.TOP_LEFT
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(top && right) {
                            strokeMode = Stroke.TOP_RIGHT
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(bottom && left) {
                            strokeMode = Stroke.BOTTOM_LEFT
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(bottom && right) {
                            strokeMode = Stroke.BOTTOM_RIGHT
                            Log.d(tag, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
                        }
                        if(strokeMode != Stroke.UNKNOWN) {
                            // 移動・拡大縮小のための初期値設定
                            initialX = params.leftMargin
                            initialY = params.topMargin
                            initialWidth = params.width
                            initialHeight = params.height
                            initialTouchX = rx
                            initialTouchY = ry

                            windowInFrame.setBackgroundResource(android.R.color.holo_blue_dark)
                        }
                    }
                    MotionEvent.ACTION_UP -> {

                        if(windowMode == Mode.MOVE && strokeMode != Stroke.UNKNOWN) {
                            // 移動完了
                            Log.d(tag, "displaySize.x, y = ${defaultDisplaySize.x}, ${defaultDisplaySize.y}")
                            Log.d(tag, "motionEvent.rawX, rawY = $rx, $ry")
                            Log.d(tag, "motionEvent.x, y = ${event.x}, ${event.y}")
                            Log.d(tag, "params.width, height=${params.width}, ${params.height}")
                            Log.d(tag, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

                            windowMode = Mode.FINISH    // フリック誤作動防止
                        }
                        if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
                            windowMode = Mode.FINISH    // フリック誤作動防止
                        }
                        if(strokeMode != Stroke.UNKNOWN) {
                            // ACTION_UP,DOWNのみの対策。
                            windowInFrame.setBackgroundColor(backgroundColor)
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val space = 0
                        if(windowMode != Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
                            // 移動中の処理
                            params.leftMargin = initialX + (rx - initialTouchX).toInt()
                            params.topMargin = initialY + (ry - initialTouchY).toInt()

                            Log.d(tag, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

                            windowMode = Mode.MOVE
                        }
                        if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {

                            val limitMaxWidth = fun(){
                                if(defaultDisplaySize.x < params.width)
                                    params.width = defaultDisplaySize.x
                            }
                            val limitMaxHeight = fun(){
                                if(defaultDisplaySize.y - space < params.height)
                                    params.height = defaultDisplaySize.y - space // 通知バー、ナビゲーションバーの考慮(space)
                            }
                            val resizeTop = fun(){
                                params.height = (initialHeight - (ry - initialTouchY).toInt())
                                params.topMargin = initialY + (ry - initialTouchY).toInt()
                                limitMaxHeight()
                                //limitMaxY()
                            }
                            val resizeBottom = fun(){
                                params.height = (initialHeight + (ry - initialTouchY).toInt())
                                limitMaxHeight()
                            }
                            val resizeLeft = fun(){
                                params.width = (initialWidth - (rx - initialTouchX).toInt())
                                params.leftMargin = initialX + (rx - initialTouchX).toInt()
                                limitMaxWidth()
                                //limitMaxX()
                            }
                            val resizeRight = fun(){
                                params.width = (initialWidth + (rx - initialTouchX).toInt())
                                limitMaxWidth()
                            }
                            when(strokeMode){
                                Stroke.TOP -> {
                                    resizeTop()
                                }
                                Stroke.BOTTOM -> {
                                    resizeBottom()
                                }
                                Stroke.LEFT -> {
                                    resizeLeft()
                                }
                                Stroke.RIGHT -> {
                                    resizeRight()
                                }
                                Stroke.TOP_LEFT -> {
                                    resizeTop()
                                    resizeLeft()
                                }
                                Stroke.TOP_RIGHT -> {
                                    resizeTop()
                                    resizeRight()
                                }
                                Stroke.BOTTOM_LEFT -> {
                                    resizeBottom()
                                    resizeLeft()
                                }
                                Stroke.BOTTOM_RIGHT -> {
                                    resizeBottom()
                                    resizeRight()
                                }
                                else -> {}
                            }
                        }
                        getActiveOverlay().layoutParams = params
                    }
                    MotionEvent.ACTION_OUTSIDE -> {
                        Log.d(tag, "ACTION_OUTSIDE event.x, y = ${event.x}, ${event.y}")
                        Log.d(tag, "ACTION_OUTSIDE event.rawX, rawY = ${event.rawX}, ${event.rawY}")
                    }
                }
                gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う

                return false
            }
        })
        windowInFrame
    }

    var activeFlag: Boolean = false

    init {
        miniWindowFrame
        windowInlineFrame
    }
    fun getActiveOverlay(): ViewGroup {
        return if(this.miniMode)
            this.miniWindowFrame
        else
            this.windowOutlineFrame
    }
    private fun getLayoutParams(viewGroup: ViewGroup): FrameLayout.LayoutParams {
        return (viewGroup.layoutParams as FrameLayout.LayoutParams)
    }
    fun getActiveWindowLayoutParams(): FrameLayout.LayoutParams {
        return getLayoutParams(getActiveOverlay())
    }
    fun isOnTouchEvent(event: MotionEvent): Boolean {
        val params = getActiveWindowLayoutParams()
        return event.rawX in params.leftMargin..(params.leftMargin + params.width)
                && event.rawY in params.topMargin..(params.topMargin + params.height)
    }
}