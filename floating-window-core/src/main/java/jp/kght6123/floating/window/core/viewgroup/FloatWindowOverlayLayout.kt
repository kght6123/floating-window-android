package jp.kght6123.floating.window.core.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * マルチウィンドウを子供に表示する最上位のオーバーレイビュー
 *
 * @author    kght6123
 * @copyright 2017/07/11 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
open class FloatWindowOverlayLayout : FrameLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val TAG = this.javaClass.simpleName

    var onDispatchTouchEventListener: OnTouchListener? = null
    var onDispatchKeyEventListener: OnKeyListener? = null

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptTouchEvent .action=${event.action}, .rawX,Y=${event.rawX},${event.rawY} .x,y=${event.x},${event.y} this.width,height=${this.width},${this.height}")

        val flag = super.onInterceptTouchEvent(event)
        Log.d(TAG, "onInterceptTouchEvent flag=$flag")

        return flag
        //return false
    }

    override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptHoverEvent event.rawX,Y=${event.rawX},${event.rawY} event.x,y=${event.x},${event.y}")
        return super.onInterceptHoverEvent(event)
    }

    override fun dispatchWindowFocusChanged(hasFocus: Boolean) {
        super.dispatchWindowFocusChanged(hasFocus)
        Log.d(TAG, "dispatchWindowFocusChanged hasFocus=$hasFocus")
    }

    override fun dispatchWindowVisibilityChanged(visibility: Int) {
        super.dispatchWindowVisibilityChanged(visibility)
        Log.d(TAG, "dispatchWindowVisibilityChanged visibility=$visibility")
    }

    override fun dispatchGenericPointerEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "dispatchGenericPointerEvent")
        return super.dispatchGenericPointerEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val flag = super.dispatchTouchEvent(event)
        Log.d(TAG, "dispatchTouchEvent .action=${event?.action}, flag=$flag .rawX,rawY=${event?.rawX},${event?.rawY} .x,y=${event?.x},${event?.y}")

        if(event != null) {
            this.onDispatchTouchEventListener?.onTouch(this, event)
        }
        //return flag
        return false
        //return true
    }

    override fun dispatchDisplayHint(hint: Int) {
        Log.d(TAG, "dispatchDisplayHint hint=$hint")
        super.dispatchDisplayHint(hint)
    }

    override fun dispatchGenericFocusedEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "dispatchGenericFocusedEvent event.rawX=${event?.rawX}")
        return super.dispatchGenericFocusedEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "dispatchGenericMotionEvent event.rawX=${event.rawX}")
        return super.dispatchGenericMotionEvent(event)
    }

    override fun dispatchUnhandledMove(focused: View?, direction: Int): Boolean {
        Log.d(TAG, "dispatchUnhandledMove")
        return super.dispatchUnhandledMove(focused, direction)
    }

    override fun focusableViewAvailable(v: View?) {
        Log.d(TAG, "focusableViewAvailable")
        super.focusableViewAvailable(v)
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
        Log.d(TAG, "dispatchKeyEventPreIme ${event?.keyCode}")
        return super.dispatchKeyEventPreIme(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val flag = super.dispatchKeyEvent(event)
        Log.d(TAG, "dispatchKeyEvent keyCode=${event?.keyCode}, flag=$flag")

        if(event != null) {
            this.onDispatchKeyEventListener?.onKey(this, event.keyCode, event)
        }
        //return flag
        return false
        //return true
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        val flag = super.onTouchEvent(event)
//        Log.d(TAG, "onTouchEvent flag=$flag")
//        return flag
//    }
}