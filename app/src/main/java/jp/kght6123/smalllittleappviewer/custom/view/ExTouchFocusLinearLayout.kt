package jp.kght6123.smalllittleappviewer.custom.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout

/**
 * Created by kogahirotaka on 2017/05/16.
 */
class ExTouchFocusLinearLayout : LinearLayout {
	constructor(context: Context?) : super(context)
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	//constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
	
	private val TAG = this.javaClass.simpleName
	
	var onInterceptInTouchEventListener: OnInterceptTouchEventListener? = null
	var onInterceptOutTouchEventListener: OnInterceptTouchEventListener? = null
	
	var onDispatchKeyEventListener: OnDispatchKeyEventListener? = null
	
	override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
		Log.d(TAG, "onInterceptTouchEvent event.rawX,Y=${event.rawX},${event.rawY} event.x,y=${event.x},${event.y} this.width,height=${this.width},${this.height}")

		if(event.x in 0..this.width && event.y in 0..this.height) {
			Log.d(TAG, "on View")
			this.onInterceptInTouchEventListener?.onTouch(event)
		} else {
			Log.d(TAG, "out View")
			this.onInterceptOutTouchEventListener?.onTouch(event)
		}
		//return super.onInterceptTouchEvent(event)
		return false
	}

	override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
		Log.d(TAG, "onInterceptHoverEvent event.rawX,Y=${event.rawX},${event.rawY} event.x,y=${event.x},${event.y}")
		//return super.onInterceptHoverEvent(event)er
		return false
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
	
	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		Log.d(TAG, "dispatchTouchEvent")
		return super.dispatchTouchEvent(ev)
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
		onDispatchKeyEventListener?.onKey(event)
		
		return super.dispatchKeyEvent(event)
	}
}
interface OnInterceptTouchEventListener {
	fun onTouch(event: MotionEvent): Unit
}
interface OnDispatchKeyEventListener {
	fun onKey(event: KeyEvent?): Unit
}