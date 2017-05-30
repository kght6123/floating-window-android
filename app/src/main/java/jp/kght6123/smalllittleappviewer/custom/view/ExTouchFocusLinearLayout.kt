package jp.kght6123.smalllittleappviewer.custom.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import jp.kght6123.smalllittleappviewer.utils.UnitUtils

/**
 * Created by kogahirotaka on 2017/05/16.
 */
class ExTouchFocusLinearLayout : LinearLayout {
	constructor(context: Context?) : super(context)
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
	//constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
	
	private val TAG = this.javaClass.simpleName
	
	var onInTouchEventListener: OnTouchEventListener? = null
	var onOutTouchEventListener: OnTouchEventListener? = null
	var onDispatchTouchEventListener: OnTouchEventListener? = null
	
	var onKeyEventListener: OnKeyEventListener? = null
	
	var onGestureListener: GestureDetector.OnGestureListener = SimpleOnGestureListener()
	val gestureDetector: GestureDetector by lazy {
		GestureDetector(context, onGestureListener)
	}
	
	override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
		Log.d(TAG, "onInterceptTouchEvent event.rawX,Y=${event.rawX},${event.rawY} event.x,y=${event.x},${event.y} this.width,height=${this.width},${this.height}")

		if(event.x in 0..this.width && event.y in 0..this.height) {
			Log.d(TAG, "on View")
			this.onInTouchEventListener?.onTouch(event)
		} else {
			Log.d(TAG, "out View")
			this.onOutTouchEventListener?.onTouch(event)
		}
		
		val flag = super.onInterceptTouchEvent(event)
		Log.d(TAG, "onInterceptTouchEvent flag=$flag")
		return flag
		//return false
	}

	override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
		Log.d(TAG, "onInterceptHoverEvent event.rawX,Y=${event.rawX},${event.rawY} event.x,y=${event.x},${event.y}")
		return super.onInterceptHoverEvent(event)
		//return false
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
		Log.d(TAG, "dispatchTouchEvent flag=$flag event.rawX,rawY=${event?.rawX},${event?.rawY} event.x,y=${event?.x},${event?.y}")
		
		if(event != null) {
			this.onDispatchTouchEventListener?.onTouch(event)
		}
		gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う
		
		return flag
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