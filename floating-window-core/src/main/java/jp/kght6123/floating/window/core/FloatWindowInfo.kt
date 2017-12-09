package jp.kght6123.floating.window.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import jp.kght6123.floating.window.core.layer.AnchorLayer
import jp.kght6123.floating.window.core.utils.DisplayUtils
import jp.kght6123.floating.window.framework.utils.UnitUtils

/**
 * マルチウィンドウの状態を保持するクラス
 *
 * @author    kght6123
 * @copyright 2017/07/11 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowInfo(
        val context: Context,
        val manager: FloatWindowManager,
        val index: Int,
        var miniMode: Boolean,
        private val backgroundColor: Int,
        private val initWidth: Int,
        private val initHeight: Int,
        val name: String
) {

    // private val tag = this.javaClass.name

    enum class Stroke {
        UNKNOWN, TOP, BOTTOM, LEFT, RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, ALL
    }
    enum class Mode {
        UNKNOWN, MOVE, RESIZE, FINISH
    }

    val notificationBarSize = 50 + 40/* ぼかしの分 */

    var strokeMode: Stroke = Stroke.UNKNOWN
    var windowMode: Mode = Mode.UNKNOWN

    // ディスプレイのサイズを格納する
    val defaultDisplaySize: Point by lazy {
        DisplayUtils.defaultDisplaySize(context)
    }

    // ディスプレイの最大値を設定
    var maxWidth: Int = defaultDisplaySize.x - 0
    var maxHeight: Int = defaultDisplaySize.y - notificationBarSize

    // ディスプレイの最小値を設定
    var minWidth: Int = UnitUtils.convertDp2Px(150f, context).toInt()
    var minHeight: Int = UnitUtils.convertDp2Px(150f, context).toInt()

    // リサイズ可・不可を設定
    var resize: Boolean = true

    private val miniModeFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

    private val miniModeParams: WindowManager.LayoutParams by lazy {
        val params = WindowManager.LayoutParams(
                UnitUtils.convertDp2Px(75f, context).toInt(),
                UnitUtils.convertDp2Px(75f, context).toInt(),
                0, // X
                0, // Y
                //WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // なるべく上の階層で表示
                //WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Android-O以降
                miniModeFlags,
                PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.START
        params.dimAmount = 0.0f
        params.alpha = 0.75f
        params
    }

    val miniWindowFrame: ViewGroup by lazy {

        val miniView = LinearLayout(context)

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

            val gestureDetector: GestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapConfirmed(event: MotionEvent?): Boolean {
                    Log.d(TAG, "miniWindowFrame SimpleOnGestureListener onSingleTapConfirmed")
                    manager.changeMode(this@FloatWindowInfo.index, false)
                    return false
                }
                override fun onDown(e: MotionEvent?): Boolean {
                    Log.d(TAG, "miniWindowFrame SimpleOnGestureListener onDown")
                    // 移動・拡大縮小のための初期値設定
                    val params = miniModeParams
                    initialX = params.x
                    initialY = params.y
                    return false
                }
                override fun onLongPress(e: MotionEvent?) {
                    Log.d(TAG, "miniWindowFrame SimpleOnGestureListener onLongPress")
                }
                override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                    Log.d(TAG, "miniWindowFrame SimpleOnGestureListener onDoubleTapEvent")
                    return false
                }
                override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                    Log.d(TAG, "miniWindowFrame SimpleOnGestureListener onScroll distanceX=$distanceX, distanceY=$distanceY")
                    // 移動中の処理
                    val params = miniModeParams
                    params.x = initialX + (e2!!.rawX - e1!!.rawX).toInt()
                    params.y = initialY + (e2.rawY - e1.rawY).toInt()
                    manager.windowManager.updateViewLayout(miniView, params)
                    return false
                }
            })

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                Log.d(TAG, "miniWindowFrame event.action = ${event.action}")
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "miniWindowFrame ACTION_UP")
                    }
                    MotionEvent.ACTION_MOVE -> {
                        Log.d(TAG, "miniWindowFrame ACTION_MOVE")
                    }
                    MotionEvent.ACTION_DOWN -> {
                        Log.d(TAG, "miniWindowFrame ACTION_DOWN")
                    }
                }
                gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う

                //return false ここで伝搬を止めないと、ACTION_DOWN以降が動かない
                return true
            }
        })
        miniView
    }

    val switchMiniMode = fun() {
        if(strokeMode != Stroke.UNKNOWN) {
            manager.changeMode(this.index, true)
        }
    }

    private val windowOutlineFrame: ViewGroup by lazy {
        val windowOutFrame =
                View.inflate(context, R.layout.window_frame, null).findViewById(R.id.windowOutlineFrame) as ViewGroup
        windowOutFrame.layoutParams = FrameLayout.LayoutParams(initWidth, initHeight)
        windowOutFrame
    }

    val windowInlineFrame: ViewGroup by lazy { windowOutlineFrame.findViewById(R.id.windowInlineFrame) as ViewGroup }
    var activeFlag: Boolean = false

    private val anchorLayerTop: AnchorLayer by lazy { AnchorLayer(AnchorLayer.Position.TOP,this) }
    private val anchorLayerLeft: AnchorLayer by lazy { AnchorLayer(AnchorLayer.Position.LEFT,this) }
    private val anchorLayerRight: AnchorLayer by lazy { AnchorLayer(AnchorLayer.Position.RIGHT,this) }
    private val anchorLayerBottom: AnchorLayer by lazy { AnchorLayer(AnchorLayer.Position.BOTTOM,this) }

    init {
        miniWindowFrame
        windowInlineFrame

        anchorLayerTop
        anchorLayerLeft
        anchorLayerRight
        anchorLayerBottom

        // デフォルト色に設定する
        updateAnchorColor(Stroke.UNKNOWN)   // FIXME 色のみ指定可、背景そのものの変更は出来ない
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
    fun getActiveLayoutParams(): FrameLayout.LayoutParams {
        return getLayoutParams(getActiveOverlay())
    }
    fun getWindowLayoutParams(): FrameLayout.LayoutParams {
        return getLayoutParams(this.windowOutlineFrame)
    }
    fun isOnTouchEvent(event: MotionEvent): Boolean {
        val params = getActiveLayoutParams()
        return event.rawX in params.leftMargin..(params.leftMargin + params.width)
                && event.rawY in params.topMargin..(params.topMargin + params.height)
    }

    fun updateAnchorColor(strokeMode: Stroke) {
        when (strokeMode) {
            Stroke.TOP -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_top)
            }
            Stroke.BOTTOM -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_bottom)
            }
            Stroke.LEFT -> {
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_left)
            }
            Stroke.RIGHT -> {
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_right)
            }
            Stroke.TOP_LEFT -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_top_left)
            }
            Stroke.TOP_RIGHT -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_top_right)
            }
            Stroke.BOTTOM_LEFT -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_bottom_left)
            }
            Stroke.BOTTOM_RIGHT -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_bottom_right)
            }
            Stroke.ALL -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_move)
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_move)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_move)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_move)
                windowInlineFrame.setBackgroundResource(android.R.color.holo_blue_dark)
            }
            Stroke.UNKNOWN -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color)
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color)
                windowInlineFrame.setBackgroundColor(backgroundColor)
            }
        }
    }

    fun updateAnchorLayerPosition() {
        val params = getActiveLayoutParams()
        updateAnchorLayerPosition(params.leftMargin, params.topMargin, true)
    }
    private fun updateAnchorMiniLayerPosition() {
        updateAnchorLayerPosition(miniModeParams.x, miniModeParams.y, false)
        val params = getLayoutParams(this.windowOutlineFrame)
        params.leftMargin = miniModeParams.x
        params.topMargin = miniModeParams.y
        this.windowOutlineFrame.layoutParams = params
    }
    private fun updateAnchorLayerPosition(x: Int, y: Int, update: Boolean) {
        anchorLayerTop.updatePosition(x, y, update)
        anchorLayerLeft.updatePosition(x, y, update)
        anchorLayerRight.updatePosition(x, y, update)
        anchorLayerBottom.updatePosition(x, y, update)
    }
    fun removeAnchor() {
        if(!miniMode) {
            anchorLayerTop.remove()
            anchorLayerBottom.remove()
            anchorLayerLeft.remove()
            anchorLayerRight.remove()
        }
        updateAnchorColor(FloatWindowInfo.Stroke.UNKNOWN)  // 色を元に戻す
    }
    fun addAnchor() {
        if(!miniMode) {
            anchorLayerTop.add()
            anchorLayerBottom.add()
            anchorLayerLeft.add()
            anchorLayerRight.add()
        }
        updateAnchorColor(FloatWindowInfo.Stroke.UNKNOWN)  // 色を元に戻す
    }

    fun addMiniMode(updatePosition: Boolean) {
        if(updatePosition)
            updateMiniModePosition(anchorLayerTop.getX(), anchorLayerTop.getY())
        manager.windowManager.addView(this.miniWindowFrame, miniModeParams)
    }

    fun updateMiniModePosition(x: Int, y: Int) {
        miniModeParams.x = if(x < 0) 0 else x
        miniModeParams.y = if(y < notificationBarSize) notificationBarSize else y
    }

    fun removeMiniMode(updatePosition: Boolean) {
        if(updatePosition)
            updateAnchorMiniLayerPosition()
        manager.windowManager.removeViewImmediate(this.miniWindowFrame)
    }

    fun updateMiniMode() {
        manager.windowManager.updateViewLayout(this.miniWindowFrame, miniModeParams)
    }
}