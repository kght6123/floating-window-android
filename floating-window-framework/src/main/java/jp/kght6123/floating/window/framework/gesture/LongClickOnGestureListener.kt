package jp.kght6123.floating.window.framework.gesture

import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * 長押しのジェスチャーを検出するクラス
 *
 * @author    kght6123
 * @copyright 2017/11/28 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class LongClickOnGestureListener(private val onLongClickListener: OnLongClickListener, private val longPressTime: Long, private val longPressRange: Float) {

    private var downX: Float = 0f
    private var downY: Float = 0f

    companion object {
        private val TAG = LongClickOnGestureListener::class.java.name

    }

    private val longPressHandler: Handler = Handler()
    private var longPressReceiver: LongPressReceiver = LongPressReceiver()

    fun onTouchEvent(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "onTouchEvent cancel")
            }
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent down")
                // 触れたマス番号を記憶
                downX = event.rawX
                downY = event.rawY

                longPressReceiver.event = event
                longPressReceiver.view = view

                // 長押し判定開始
                longPressHandler.postDelayed(longPressReceiver, longPressTime)
            }
            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "onTouchEvent up")
                // 長押し中に指を上げたらhandlerの処理を中止
                longPressHandler.removeCallbacks(longPressReceiver)

                //onLongClickListener.onLongClickCancel(long_press_receiver.event, long_press_receiver.view, event, view)
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "onTouchEvent move")
                // 触れたマスが最初に触れていたマスと違う番号なら
                if ((event.rawX - downX) in -longPressRange..longPressRange
                        && (event.rawY - downY)  in -longPressRange..longPressRange) {

                } else {
                    // 最初に触れたマスから離れたらhandlerの処理を中止
                    longPressHandler.removeCallbacks(longPressReceiver)

                    //onLongClickListener.onLongClickCancel(long_press_receiver.event, long_press_receiver.view, event, view)
                }
            }
            MotionEvent.ACTION_OUTSIDE -> {
                Log.d(TAG, "onTouchEvent outside")
            }
            else -> {
                Log.d(TAG, "onTouchEvent else")
            }
        }
        return false
    }

    inner class LongPressReceiver: Runnable {

        lateinit var event: MotionEvent
        lateinit var view: View

        override fun run()
        {// 長押しする
            onLongClickListener.onLongClick(event, view)
        }
    }

    interface OnLongClickListener {
        fun onLongClick(event: MotionEvent, view: View)
        //fun onLongClickCancel(event: MotionEvent, view: View, cancelEvent: MotionEvent, cancelView: View)
    }

}