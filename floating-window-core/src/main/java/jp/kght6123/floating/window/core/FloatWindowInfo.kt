package jp.kght6123.floating.window.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import jp.kght6123.floating.window.core.layer.AnchorLayerGroup
import jp.kght6123.floating.window.core.layer.EdgeAnchorLayerGroup
import jp.kght6123.floating.window.core.layer.SinglePointAnchorLayerGroup
import jp.kght6123.floating.window.core.utils.DisplayUtils
import jp.kght6123.floating.window.framework.FloatWindowApplication
import jp.kght6123.floating.window.framework.MultiFloatWindowConstants
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
        val initSettings: FloatWindowApplication.MultiFloatWindowInitSettings,
        val name: String,
        var miniMode: Boolean = initSettings.miniMode,
        private val theme: MultiFloatWindowConstants.Theme = initSettings.theme,
        anchor: MultiFloatWindowConstants.Anchor = initSettings.anchor,
        private val initWidth: Int = initSettings.width,
        private val initHeight: Int = initSettings.height
) {

    private val tag = this.javaClass.name

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
    var maxWidth: Int =
            if(initSettings.windowMaxWidth < 0f)
                defaultDisplaySize.x - 0
            else
                UnitUtils.convertDp2Px(initSettings.windowMaxWidth, context).toInt()

    var maxHeight: Int =
            if(initSettings.windowMaxHeight < 0f)
                defaultDisplaySize.y - notificationBarSize
            else
                UnitUtils.convertDp2Px(initSettings.windowMaxHeight, context).toInt()

    // ディスプレイの最小値を設定
    var minWidth: Int = UnitUtils.convertDp2Px(initSettings.windowMinWidth, context).toInt()
    var minHeight: Int = UnitUtils.convertDp2Px(initSettings.windowMinHeight, context).toInt()

    // リサイズ可・不可を設定
    var resize: Boolean = initSettings.windowResize

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
                MultiFloatWindowConstants.WINDOW_MANAGER_OVERLAY_TYPE,
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

    val windowOutlineFrame: ViewGroup by lazy {
        val windowOutFrame =
                View.inflate(context, R.layout.window_frame, null).findViewById(R.id.windowOutlineFrame) as ViewGroup
        when (theme) {
            MultiFloatWindowConstants.Theme.Dark ->
                windowOutFrame.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
            MultiFloatWindowConstants.Theme.Light ->
                windowOutFrame.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
        }
        windowOutFrame.layoutParams = FrameLayout.LayoutParams(initWidth, initHeight)
        windowOutFrame
    }

    val windowInlineFrame: ViewGroup by lazy { windowOutlineFrame.findViewById(R.id.windowInlineFrame) as ViewGroup }
    var activeFlag: Boolean = false
        set(activeFlag) {
            field = activeFlag
            if(field) {
                Log.d(tag, "setActiveFlag addAnchor")
                this.addAnchor()
            } else {
                Log.d(tag, "setActiveFlag removeAnchor")
                this.removeAnchor()
            }
        }

    private val anchorLayerGroup: AnchorLayerGroup

    init {
        miniWindowFrame
        windowInlineFrame

        anchorLayerGroup = when (anchor) {
            MultiFloatWindowConstants.Anchor.Edge ->
                EdgeAnchorLayerGroup(this)
            MultiFloatWindowConstants.Anchor.SinglePoint ->
                SinglePointAnchorLayerGroup(this)
        }
        updateAnchorColor(Stroke.UNKNOWN)// デフォルト色に設定する
    }
    private fun getLayoutParams(viewGroup: ViewGroup): FrameLayout.LayoutParams {
        return (viewGroup.layoutParams as FrameLayout.LayoutParams)
    }
    fun getWindowLayoutParams(): FrameLayout.LayoutParams {
        return getLayoutParams(this.windowOutlineFrame)
    }
    fun isOnTouchEvent(event: MotionEvent): Boolean {
        val params = getWindowLayoutParams()
        return event.rawX in params.leftMargin..(params.leftMargin + params.width)
                && event.rawY in params.topMargin..(params.topMargin + params.height)
    }
    fun updateAnchorColor(strokeMode: Stroke) {

        anchorLayerGroup.onChangeStrokeMode(strokeMode)

        when (strokeMode) {
            Stroke.TOP -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_top)
            }
            Stroke.BOTTOM -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_bottom)
            }
            Stroke.LEFT -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_left)
            }
            Stroke.RIGHT -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_right)
            }
            Stroke.TOP_LEFT -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_top_left)
            }
            Stroke.TOP_RIGHT -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_top_right)
            }
            Stroke.BOTTOM_LEFT -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_bottom_left)
            }
            Stroke.BOTTOM_RIGHT -> {
                windowInlineFrame.setBackgroundResource(R.drawable.window_frame_stroke_bottom_right)
            }
            Stroke.ALL -> {
                windowInlineFrame.setBackgroundResource(android.R.color.holo_blue_dark)
            }
            Stroke.UNKNOWN -> {
                windowInlineFrame.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    fun updateAnchorLayerPosition() {
        val params = getWindowLayoutParams()
        anchorLayerGroup.onChangePosition(params.leftMargin, params.topMargin, true)
    }
    private fun updateAnchorMiniLayerPosition() {
        anchorLayerGroup.onChangePosition(miniModeParams.x, miniModeParams.y, false)
        val params = getLayoutParams(this.windowOutlineFrame)
        params.leftMargin = miniModeParams.x
        params.topMargin = miniModeParams.y
        this.windowOutlineFrame.layoutParams = params
    }
    fun removeAnchor() {
        Log.d(tag, "removeAnchor")
        if(!miniMode) {
            anchorLayerGroup.remove()
        }
        updateAnchorColor(FloatWindowInfo.Stroke.UNKNOWN)  // 色を元に戻す
    }
    fun addAnchor() {
        Log.d(tag, "addAnchor")
        if(!miniMode) {
            anchorLayerGroup.add()
        }
        updateAnchorColor(FloatWindowInfo.Stroke.UNKNOWN)  // 色を元に戻す
    }

    fun addMiniMode(updatePosition: Boolean) {
        if(updatePosition)
            updateMiniModePosition(anchorLayerGroup.getX(), anchorLayerGroup.getY())
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