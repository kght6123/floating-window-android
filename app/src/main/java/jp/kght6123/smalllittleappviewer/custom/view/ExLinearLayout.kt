package jp.kght6123.smalllittleappviewer.custom.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout

/**
 * Created by kogahirotaka on 2017/05/16.
 */
class ExLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    //constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val TAG = this.javaClass.simpleName

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptTouchEvent event.rawX,Y=${event.rawX},${event.rawY} event.x, y = ${event.x}, ${event.y}")
        return super.onInterceptTouchEvent(event)
    }

    override fun onInterceptHoverEvent(event: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptHoverEvent event.rawX,Y=${event.rawX},${event.rawY} event.x, y = ${event.x}, ${event.y}")
        return super.onInterceptHoverEvent(event)
    }

    override fun dispatchWindowFocusChanged(hasFocus: Boolean) {
        super.dispatchWindowFocusChanged(hasFocus)
        Log.d(TAG, "dispatchWindowFocusChanged hasFocus=$hasFocus")
    }
}