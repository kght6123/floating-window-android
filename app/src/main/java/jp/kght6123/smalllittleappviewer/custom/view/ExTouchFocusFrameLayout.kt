package jp.kght6123.smalllittleappviewer.custom.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * FrameLayoutをオーバーレイウィンドウ向けにイベント拡張したクラス
 *
 * Created by kght6123 on 2017/05/16.
 */
open class ExTouchFocusFrameLayout : FrameLayout {
	constructor(context: Context?) : super(context)
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	private val TAG = this.javaClass.simpleName

	var onDispatchTouchEventListener: OnTouchListener? = null
	var onKeyEventListener: OnKeyEventListener? = null

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
		Log.d(TAG, "dispatchKeyEvent ${event?.keyCode}")
		this.onKeyEventListener?.onKey(event)
		return super.dispatchKeyEvent(event)
	}
	
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		val flag = super.onTouchEvent(event)
		Log.d(TAG, "onTouchEvent flag=$flag")
		return flag
	}
}
interface OnTouchEventListener {
	fun onTouch(event: MotionEvent): Unit
}
interface OnKeyEventListener {
	fun onKey(event: KeyEvent?): Unit
}