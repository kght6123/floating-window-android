package jp.kght6123.smalllittleappviewer.custom.view

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import jp.kght6123.smalllittleappviewer.R
import jp.kght6123.smalllittleappviewer.utils.UnitUtils

/**
 * Created by kogahirotaka on 2017/06/02.
 */
class OverlayWindowLinearLayout(context: Context) : ExTouchFocusLinearLayout(context) {
	
	private val TAG = this.javaClass.simpleName
	
	private enum class Stroke {
		UNKNOWN, TOP, BOTTOM, LEFT, RIGHT
	}
	private enum class Mode {
		UNKNOWN, MOVE, RESIZE, FINISH
	}
	
	val activeFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or //座標系をスクリーンに合わせる
			WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or // Viewの外のタッチイベントにも反応する？
			//WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or  // タッチイベントを拾わない。ロック画面を邪魔しない
			//WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  // ウィンドウにフォーカスが当たった時だけ、無効にしたい
			//WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM  // ウィンドウがどのように対話するかに関して、FLAG_NOT_FOCUSABLEの状態を反転させる？？
			WindowManager.LayoutParams.FLAG_DIM_BEHIND or  // 後ろのすべてが淡色表示されます。
			WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or // 境界外のタッチイベントが受け付けられ、サポートする他ウィンドウにも送信
			//WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or   // 半透明のナビゲーションバー？
			//WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS   // 半透明のステータスバー？
			//WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
			WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
			WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
			WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
	
	val activeDimAmount = 0.09f
	val activeAlpha = 0.95f
	
	val inactiveFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
			WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
			WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
			WindowManager.LayoutParams.FLAG_SPLIT_TOUCH or
			WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
			WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
			WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
	
	val inactiveAlpha = 0.5f
	
	private var strokeMode: Stroke = Stroke.UNKNOWN
	private var windowMode: Mode = Mode.UNKNOWN
	
	val windowFrame: LinearLayout by lazy {
		View.inflate(context, R.layout.service_system_overlay_layer/*フレームView*/, this)
		this.findViewById(R.id.windowFrame) as LinearLayout
	}
	val webView: WebView by lazy {
		val webView = windowFrame.findViewById(R.id.webView) as WebView
		webView.setWebViewClient(object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
				return false
			}
		})
		webView.loadUrl("http://www.google.com")
		webView
	}
	
	val overlayMiniView: ViewGroup by lazy {
		val overlayMiniView = LayoutInflater.from(context).inflate(R.layout.service_system_overlay_mini_layer, null) as ViewGroup
		overlayMiniView.setOnTouchListener({ view, motionEvent ->
			
			when (motionEvent.action) {
				MotionEvent.ACTION_UP -> {
					windowManager.removeViewImmediate(overlayMiniView)
					windowManager.addView(this@OverlayWindowLinearLayout, params)
				}
			}
			return@setOnTouchListener false
		})
		overlayMiniView
	}
	
	// WindowManager
	val windowManager: WindowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
	
	// WindowManagerに設定するレイアウトパラメータ、オーバーレイViewの設定をする
	val params: WindowManager.LayoutParams by lazy {
		val params = WindowManager.LayoutParams(
				UnitUtils.convertDp2Px(300f, context).toInt(), //WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT,
				UnitUtils.convertDp2Px(400f, context).toInt(), //WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT,
				100, // X
				100, // Y
				//WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // なるべく上の階層で表示
				//WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY // Android-O以降
				activeFlags,
				PixelFormat.TRANSLUCENT)
		params.gravity = Gravity.TOP or Gravity.START// or Gravity.LEFT
		params.dimAmount = activeDimAmount
		params.windowAnimations = android.R.style.Animation_Dialog//Animation_Activity//Animation_Toast
		params.alpha = activeAlpha
		params
	}
	// ディスプレイのサイズを格納する
	val displaySize: Point by lazy {
		val display = windowManager.defaultDisplay
		val size = Point()
		display.getSize(size)
		size
	}
	
	init {
		val layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT)
		this.layoutParams = layoutParams
		this.orientation = LinearLayout.VERTICAL
		this.isFocusable = true
		this.isFocusableInTouchMode = true
		this.setBackgroundResource(R.drawable.shadow_233622)
		
		this.onInTouchEventListener = object: OnTouchEventListener {
			override fun onTouch(event: MotionEvent) {
				Log.d(TAG, "InTouch motionEvent.x, y = ${event.x}, ${event.y}")
				
				params.flags = activeFlags
				params.dimAmount = activeDimAmount
				params.alpha = activeAlpha
				windowManager.updateViewLayout(this@OverlayWindowLinearLayout, params)
				
				// TODO コントロール用のActivity起動予定
			}
		}
		this.onOutTouchEventListener = object: OnTouchEventListener {
			override fun onTouch(event: MotionEvent) {
				Log.d(TAG, "OutTouch motionEvent.x, y = ${event.x}, ${event.y}")
				
				params.flags = inactiveFlags
				params.dimAmount = 0.0f
				params.alpha = inactiveAlpha
				windowManager.updateViewLayout(this@OverlayWindowLinearLayout, params)
				
				// TODO コントロール用のActivity停止予定
			}
		}
		this.setOnLongClickListener {
			if(strokeMode != Stroke.UNKNOWN && windowMode == Mode.UNKNOWN) {
				// リサイズモードの背景色に変える
				windowFrame.setBackgroundResource(R.color.colorAccent)
				windowMode = Mode.RESIZE
			}
			return@setOnLongClickListener false
		}
		
		val switchMiniMode = fun() {
			if(/*windowMode == Mode.UNKNOWN && */strokeMode != Stroke.UNKNOWN) {
				windowManager.removeViewImmediate(this@OverlayWindowLinearLayout)
				windowManager.addView(overlayMiniView, params)
			}
		}
		
		this.onGestureListener = object: SimpleOnGestureListener() {
			override fun onDoubleTap(event: MotionEvent): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onDoubleTap")
				Log.d(TAG, "SimpleOnGestureListener onDoubleTap $strokeMode")
				
				when(strokeMode){
					Stroke.TOP -> {
						switchMiniMode()
					}
					Stroke.BOTTOM -> {
						switchMiniMode()
					}
					Stroke.LEFT -> {
						switchMiniMode()
					}
					Stroke.RIGHT -> {
						switchMiniMode()
					}
					else -> {
						
					}
				}
				return super.onDoubleTap(event)
			}
			override fun onDoubleTapEvent(event: MotionEvent): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onDoubleTapEvent")
				
				when(event.action){
					MotionEvent.ACTION_MOVE -> {
						Log.d(TAG, "SimpleOnGestureListener onDoubleTapEvent ACTION_MOVE")
					}
					MotionEvent.ACTION_DOWN -> {
						Log.d(TAG, "SimpleOnGestureListener onDoubleTapEvent ACTION_DOWN")
					}
					MotionEvent.ACTION_UP -> {
						Log.d(TAG, "SimpleOnGestureListener onDoubleTapEvent ACTION_UP")
					}
					else -> {
						Log.d(TAG, "SimpleOnGestureListener event.action=${event.action}")
					}
				}
				return super.onDoubleTapEvent(event)
			}
			override fun onDown(event: MotionEvent): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onDown")
				return super.onDown(event)
			}
			override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onFling velocityX,Y=$velocityX,$velocityY")
				
				if(Math.abs(velocityX) > Math.abs(velocityY)) {
					Log.d(TAG, "SimpleOnGestureListener onFling X")
					if(velocityX > 3000){// 反応速度の調整
						Log.d(TAG, "SimpleOnGestureListener onFling Right")
						//switchMiniMode()
					}else if(velocityX < -3000){// 反応速度の調整
						Log.d(TAG, "SimpleOnGestureListener onFling Left")
						//switchMiniMode()
					}
					
				}else if(Math.abs(velocityX) < Math.abs(velocityY)) {
					Log.d(TAG, "SimpleOnGestureListener onFling Y")
					if(velocityY > 3000){// 反応速度の調整
						Log.d(TAG, "SimpleOnGestureListener onFling Down")
						//switchMiniMode()
					}else if(velocityX < -3000){// 反応速度の調整
						Log.d(TAG, "SimpleOnGestureListener onFling Up")
						//switchMiniMode()
					}
				}
				return super.onFling(event1, event2, velocityX, velocityY)
			}
			override fun onLongPress(event: MotionEvent) {
				Log.d(TAG, "SimpleOnGestureListener onLongPress")
				super.onLongPress(event)
			}
			override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onScroll")
				return super.onScroll(event1, event2, distanceX, distanceY)
			}
			override fun onShowPress(event: MotionEvent) {
				Log.d(TAG, "SimpleOnGestureListener onShowPress")
				super.onShowPress(event)
			}
			override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onSingleTapConfirmed")
				return super.onSingleTapConfirmed(event)
			}
			override fun onSingleTapUp(event: MotionEvent): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onSingleTapUp")
				return super.onSingleTapUp(event)
			}
			override fun onContextClick(e: MotionEvent?): Boolean {
				Log.d(TAG, "SimpleOnGestureListener onContextClick")
				return super.onContextClick(e)
			}
		}
		this.onDispatchTouchEventListener = object: OnTouchEventListener {
			
			private val strokeWidth: Int = UnitUtils.convertDp2Px(3f, context).toInt() + 40/*ぼかしの分*/
			
			private var initialX: Int = 0
			private var initialY: Int = 0
			private var initialWidth: Int = 0
			private var initialHeight: Int = 0
			private var initialTouchX: Float = 0f
			private var initialTouchY: Float = 0f
			
			override fun onTouch(event: MotionEvent) {
				
				Log.d(TAG, "motionEvent.action = ${event.action}")
				Log.d(TAG, "motionEvent.rawX, rawY = ${event.rawX}, ${event.rawY}")
				Log.d(TAG, "motionEvent.x, y = ${event.x}, ${event.y}")
				
				// 移動のための現時点の情報作成
				//val touchAreaX: Float = displaySize.x.toFloat() - (params.width.toFloat() - event.x)
				//val touchAreaY: Float = displaySize.y.toFloat() - (params.height.toFloat() - event.y)
				
				val rx: Float = event.rawX //if(event.rawX > touchAreaX) touchAreaX else event.rawX
				val ry: Float = event.rawY //if(event.rawY > touchAreaY) touchAreaY else event.rawY
				
				when (event.action) {
					MotionEvent.ACTION_CANCEL -> {
						
					}
					MotionEvent.ACTION_DOWN -> {
						
						// モードのリセット
						windowMode = Mode.UNKNOWN
						strokeMode = Stroke.UNKNOWN
						
						if(event.x in 0..this.strokeWidth){
							strokeMode = Stroke.LEFT
							Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
							
						}
						if(event.x in (this@OverlayWindowLinearLayout.width-this.strokeWidth)..this@OverlayWindowLinearLayout.width) {
							strokeMode = Stroke.RIGHT
							Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
							
						}
						if(event.y in 0..this.strokeWidth) {
							strokeMode = Stroke.TOP
							Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
							
						}
						if(event.y in (this@OverlayWindowLinearLayout.height-this.strokeWidth)..this@OverlayWindowLinearLayout.height) {
							strokeMode = Stroke.BOTTOM
							Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
							
						}
						if(strokeMode != Stroke.UNKNOWN) {
							// 移動・拡大縮小のための初期値設定
							initialX = params.x
							initialY = params.y
							initialWidth = params.width
							initialHeight = params.height
							initialTouchX = rx
							initialTouchY = ry
							
							windowFrame.setBackgroundResource(android.R.color.holo_blue_dark)
						}
					}
					MotionEvent.ACTION_UP -> {
						
						if(windowMode == Mode.MOVE && strokeMode != Stroke.UNKNOWN) {
							// 移動完了
							Log.d(TAG, "displaySize.x, y = ${displaySize.x}, ${displaySize.y}")
							Log.d(TAG, "motionEvent.rawX, rawY = $rx, $ry")
							Log.d(TAG, "motionEvent.x, y = ${event.x}, ${event.y}")
							//Log.d(TAG, "touchArea.x, y = $touchAreaX, $touchAreaY")
							Log.d(TAG, "params.width, height=${params.width}, ${params.height}")
							//Log.d(TAG, "(touchAreaX - rx), (touchAreaY - ry)=${(touchAreaX - rx)}, ${(touchAreaY - ry)}")
							Log.d(TAG, "params.x, y = ${params.x}, ${params.y}")

//							if ((touchAreaX - rx) in 0..(10 + 100)  // 上部は干渉防止に100px余裕を
//									|| params.x /*- (displaySize.x - params.width)*/ in 0..10   // Left
//									|| params.y /*- (displaySize.y - params.height)*/ in 0..10  // Top
//									|| (touchAreaY - ry) in 0..(161 + 10)) {    // 下部はソフトキー分の余裕を
//								// 両端に移動したら小さくする
//								windowManager.removeView(overlayView)
//								windowManager.addView(overlayMiniView, params)
//							}
							windowMode = Mode.FINISH    // フリック誤作動防止
						}
						if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
							windowMode = Mode.FINISH    // フリック誤作動防止
						}
						if(strokeMode != Stroke.UNKNOWN) {
							// ACTION_UP,DOWNのみの対策。
							windowFrame.setBackgroundResource(android.R.color.background_light)
						}
					}
					MotionEvent.ACTION_MOVE -> {
						val space = 0//100
						//val maxX = displaySize.x
						//val maxY = displaySize.y
						//val limitMaxX = fun(){
						//	if (params.x > maxX - params.width)
						//		params.x = maxX - params.width
						//	else if (params.x < 0)
						//		params.x = 0
						//}
						//val limitMaxY = fun(){
						//	if (params.y > maxY - params.height)
						//		params.y = maxY - params.height
						//	else if (params.y < space)// 上部は通知バーに干渉しない様に、100pxスペースを空ける(space)
						//		params.y = space
						//}
						if(windowMode != Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
							// 移動中の処理
							params.x = initialX + (rx - initialTouchX).toInt()
							params.y = initialY + (ry - initialTouchY).toInt()
							
							//limitMaxX()
							//limitMaxY()
							
							Log.d(TAG, "params.x, y = ${params.x}, ${params.y}")
							
							windowManager.updateViewLayout(this@OverlayWindowLinearLayout, params)
							
							windowMode = Mode.MOVE
						}
						if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
							
							val limitMaxWidth = fun(){
								if(displaySize.x < params.width)
									params.width = displaySize.x
							}
							val limitMaxHeight = fun(){
								if(displaySize.y - space < params.height)
									params.height = displaySize.y - space // 通知バー、ナビゲーションバーの考慮(space)
							}
							when(strokeMode){
								Stroke.TOP -> {
									params.height = (initialHeight - (ry - initialTouchY).toInt())
									params.y = initialY + (ry - initialTouchY).toInt()
									limitMaxHeight()
									//limitMaxY()
									windowManager.updateViewLayout(this@OverlayWindowLinearLayout, params)
								}
								Stroke.BOTTOM -> {
									params.height = (initialHeight + (ry - initialTouchY).toInt())
									limitMaxHeight()
									windowManager.updateViewLayout(this@OverlayWindowLinearLayout, params)
								}
								Stroke.LEFT -> {
									params.width = (initialWidth - (rx - initialTouchX).toInt())
									params.x = initialX + (rx - initialTouchX).toInt()
									limitMaxWidth()
									//limitMaxX()
									windowManager.updateViewLayout(this@OverlayWindowLinearLayout, params)
								}
								Stroke.RIGHT -> {
									params.width = (initialWidth + (rx - initialTouchX).toInt())
									limitMaxWidth()
									windowManager.updateViewLayout(this@OverlayWindowLinearLayout, params)
								}
								else -> {
									
								}
							}
						}
					}
					MotionEvent.ACTION_OUTSIDE -> {
						Log.d(TAG, "ACTION_OUTSIDE event.x, y = ${event.x}, ${event.y}")
						Log.d(TAG, "ACTION_OUTSIDE event.rawX, rawY = ${event.rawX}, ${event.rawY}")
					}
				}
				
			}
		}
		// WebView初期化
		webView
		
		// ここでビューをオーバーレイ領域に追加する
		windowManager.addView(this, params)
	}
	
	fun finish() {
		windowManager.removeViewImmediate(this)
	}
}