package jp.kght6123.smalllittleappviewer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import jp.kght6123.smalllittleappviewer.custom.view.ExTouchFocusLinearLayout
import jp.kght6123.smalllittleappviewer.custom.view.OnTouchEventListener
import jp.kght6123.smalllittleappviewer.utils.UnitUtils


/**
 * Created by koga.hirotaka on 2017/05/09.
 */
class SystemOverlayLayerService : Service() {

	private val TAG = this.javaClass.simpleName
	
	private enum class Stroke {
		UNKNOWN, TOP, BOTTOM, LEFT, RIGHT
	}
	private enum class Mode {
		UNKNOWN, MOVE, RESIZE
	}
	
	val activeFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or //座標系をスクリーンに合わせる
			WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or // Viewの外のタッチイベントにも反応する？
			//WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or  // タッチイベントを拾わない。ロック画面を邪魔しない
			//WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  // ウィンドウにフォーカスが当たった時だけ、無効にしたい
			//WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM  // ウィンドウがどのように対話するかに関して、FLAG_NOT_FOCUSABLEの状態を反転させる？？
			WindowManager.LayoutParams.FLAG_DIM_BEHIND or  // 後ろのすべてが淡色表示されます。
			WindowManager.LayoutParams.FLAG_SPLIT_TOUCH  // 境界外のタッチイベントが受け付けられ、サポートする他ウィンドウにも送信
			//WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or   // 半透明のナビゲーションバー？
			//WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS   // 半透明のステータスバー？
	
	val activeDimAmount = 0.09f
	val activeAlpha = 0.95f
	
	val inactiveFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
			WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
			WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
			WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
	
	val inactiveAlpha = 0.5f
	
	private var strokeMode: Stroke = Stroke.UNKNOWN
	private var windowMode: Mode = Mode.UNKNOWN
	
	// オーバーレイ表示させるビュー
	val overlayView: ExTouchFocusLinearLayout by lazy {
		val view: ExTouchFocusLinearLayout =
				LayoutInflater.from(this).inflate(R.layout.service_system_overlay_layer, null) as ExTouchFocusLinearLayout
		view.onInTouchEventListener = object: OnTouchEventListener {
			override fun onTouch(event: MotionEvent) {
				Log.d(TAG, "InTouch motionEvent.x, y = ${event.x}, ${event.y}")
				
				params.flags = activeFlags
				params.dimAmount = activeDimAmount
				params.alpha = activeAlpha
				windowManager.updateViewLayout(overlayView, params)
				
				// TODO コントロール用のActivity起動予定
			}
		}
		view.onOutTouchEventListener = object: OnTouchEventListener {
			override fun onTouch(event: MotionEvent) {
				Log.d(TAG, "OutTouch motionEvent.x, y = ${event.x}, ${event.y}")
				
				params.flags = inactiveFlags
				params.dimAmount = 0.0f
				params.alpha = inactiveAlpha
				windowManager.updateViewLayout(overlayView, params)
				
				// TODO コントロール用のActivity停止予定
			}
		}
		view.setOnLongClickListener {
			if(strokeMode != Stroke.UNKNOWN && windowMode == Mode.UNKNOWN) {
				// リサイズモードの背景色に変える
				windowFrame.setBackgroundResource(R.color.colorAccent)
				windowMode = Mode.RESIZE
			}
			return@setOnLongClickListener false
		}
		view.onDispatchTouchEventListener = object: OnTouchEventListener {
			
			private val strokeWidth: Int = UnitUtils.convertDp2Px(3f, this@SystemOverlayLayerService).toInt() + 40/*ぼかしの分*/
			
			private var initialX: Int = 0
			private var initialY: Int = 0
			private var initialWidth: Int = 0
			private var initialHeight: Int = 0
			private var initialTouchX: Float = 0f
			private var initialTouchY: Float = 0f
			
			override fun onTouch(event: MotionEvent) {
				
				Log.d(TAG, "motionEvent.rawX, rawY = ${event.rawX}, ${event.rawY}")
				Log.d(TAG, "motionEvent.x, y = ${event.x}, ${event.y}")
				
				// 移動のための現時点の情報作成
				val touchAreaX: Float = displaySize.x.toFloat() - (params.width.toFloat() - event.x)
				val touchAreaY: Float = displaySize.y.toFloat() - (params.height.toFloat() - event.y)
				
				val rx: Float =
						if(event.rawX > touchAreaX) touchAreaX else event.rawX
				val ry: Float =
						if(event.rawY > touchAreaY) touchAreaY else event.rawY
				
				when (event.action) {
					
					MotionEvent.ACTION_DOWN -> {
						
						// モードのリセット
						windowMode = Mode.UNKNOWN
						strokeMode = Stroke.UNKNOWN
						
						if(event.x in 0..this.strokeWidth){
							strokeMode = Stroke.LEFT
							Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
							
						}
						if(event.x in (view.width-this.strokeWidth)..view.width) {
							strokeMode = Stroke.RIGHT
							Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
							
						}
						if(event.y in 0..this.strokeWidth) {
							strokeMode = Stroke.TOP
							Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")
							
						}
						if(event.y in (view.height-this.strokeWidth)..view.height) {
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
						}
					}
					MotionEvent.ACTION_UP -> {
						
						if(windowMode == Mode.MOVE && strokeMode != Stroke.UNKNOWN) {
							// 移動完了
							Log.d(TAG, "displaySize.x, y = ${displaySize.x}, ${displaySize.y}")
							Log.d(TAG, "motionEvent.rawX, rawY = $rx, $ry")
							Log.d(TAG, "motionEvent.x, y = ${event.x}, ${event.y}")
							Log.d(TAG, "touchArea.x, y = $touchAreaX, $touchAreaY")
							Log.d(TAG, "params.width, height=${params.width}, ${params.height}")
							Log.d(TAG, "(touchAreaX - rx), (touchAreaY - ry)=${(touchAreaX - rx)}, ${(touchAreaY - ry)}")
							Log.d(TAG, "params.x, y = ${params.x}, ${params.y}")
							
							if ((touchAreaX - rx) in 0..10
									|| params.x - (displaySize.x - params.width) in 0..10
									|| params.y - (displaySize.y - params.height) in 0..10
									|| (touchAreaY - ry) in 0..(161 + 10)) {
								// 両端に移動したら小さくする
								windowManager.removeView(overlayView)
								windowManager.addView(overlayMiniView, params)
							}
							windowMode = Mode.UNKNOWN
						}
						if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
							windowMode = Mode.UNKNOWN
							
							windowFrame.setBackgroundResource(android.R.color.background_light)
						}
					}
					MotionEvent.ACTION_MOVE -> {
						
						if(windowMode != Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
							// 移動中の処理
							params.x = initialX + (rx - initialTouchX).toInt()
							params.y = initialY + (ry - initialTouchY).toInt()
							
							if (params.x > displaySize.x - params.width)
								params.x = displaySize.x - params.width
							else if (params.x < 0)
								params.x = 0
							
							if (params.y > displaySize.y - params.height)
								params.y = displaySize.y - params.height
							else if (params.y < 0)
								params.y = 0
							
							Log.d(TAG, "params.x, y = ${params.x}, ${params.y}")
							
							windowManager.updateViewLayout(overlayView, params)
							
							windowMode = Mode.MOVE
						}
						if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
							
							when(strokeMode){
								Stroke.TOP -> {
									params.height = (initialHeight - (ry - initialTouchY).toInt())
									params.y = initialY + (ry - initialTouchY).toInt()
									windowManager.updateViewLayout(overlayView, params)
								}
								Stroke.BOTTOM -> {
									params.height = (initialHeight + (ry - initialTouchY).toInt())
									windowManager.updateViewLayout(overlayView, params)
								}
								Stroke.LEFT -> {
									params.width = (initialWidth - (rx - initialTouchX).toInt())
									params.x = initialX + (rx - initialTouchX).toInt()
									windowManager.updateViewLayout(overlayView, params)
								}
								Stroke.RIGHT -> {
									params.width = (initialWidth + (rx - initialTouchX).toInt())
									windowManager.updateViewLayout(overlayView, params)
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
		view
	}
	//val windowFrameTop: LinearLayout by lazy { overlayView.findViewById(R.id.windowFrameTop) as LinearLayout }
	val windowFrame: LinearLayout by lazy { overlayView.findViewById(R.id.windowFrame) as LinearLayout }
	//val titleBar: LinearLayout by lazy { overlayView.findViewById(R.id.titleBar) as LinearLayout }
	//val bodyArea: LinearLayout by lazy { overlayView.findViewById(R.id.bodyArea) as LinearLayout }
	val webView: WebView by lazy { overlayView.findViewById(R.id.webView) as WebView }
	
	val overlayMiniView: ViewGroup by lazy { LayoutInflater.from(this).inflate(R.layout.service_system_overlay_mini_layer, null) as ViewGroup }
	
	// WindowManager
	val windowManager: WindowManager by lazy { applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
	
	// WindowManagerに設定するレイアウトパラメータ、オーバーレイViewの設定をする
	val params: WindowManager.LayoutParams by lazy { WindowManager.LayoutParams(
			UnitUtils.convertDp2Px(300f, this).toInt(),//WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT,
			UnitUtils.convertDp2Px(400f, this).toInt(),//WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT,
			100,// X
			100,// Y
			//WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
			WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,     // なるべく上の階層で表示
			//WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY // Android-O以降
			activeFlags,
			PixelFormat.TRANSLUCENT)
	}
	
	// ディスプレイのサイズを格納する
	val displaySize: Point by lazy {
		val display = windowManager.defaultDisplay
		val size = Point()
		display.getSize(size)
		size
	}
	
	override fun onBind(intent: Intent?): IBinder {
		throw UnsupportedOperationException("Not yet implemented")
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		
		params.gravity = Gravity.TOP or Gravity.START// or Gravity.LEFT
		params.dimAmount = activeDimAmount
		params.windowAnimations = android.R.style.Animation_Dialog//Animation_Activity//Animation_Toast
		params.alpha = activeAlpha
		
		overlayMiniView.setOnTouchListener({ view, motionEvent ->
			
			when (motionEvent.action) {
				MotionEvent.ACTION_UP -> {
					windowManager.removeView(overlayMiniView)
					windowManager.addView(overlayView, params)
				}
			}
			return@setOnTouchListener false
		})
		
		webView.setWebViewClient(object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
				return false
			}
		})
		webView.loadUrl("http://www.google.com");
		
		// ここでビューをオーバーレイ領域に追加する
		windowManager.addView(overlayView, params)
		
		return START_STICKY
	}
	
	override fun onDestroy() {
		super.onDestroy()
		windowManager.removeView(overlayView)
	}
}