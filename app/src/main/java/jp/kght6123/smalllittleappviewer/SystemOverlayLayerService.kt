package jp.kght6123.smalllittleappviewer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.LinearLayout
import android.webkit.WebViewClient
import android.view.Gravity
import android.view.WindowManager
import jp.kght6123.smalllittleappviewer.custom.view.ExTouchFocusLinearLayout
import jp.kght6123.smalllittleappviewer.custom.view.OnInterceptTouchEventListener


/**
 * Created by koga.hirotaka on 2017/05/09.
 */
class SystemOverlayLayerService : Service() {

	private val TAG = this.javaClass.simpleName

	// オーバーレイ表示させるビュー
	val overlayView: ExTouchFocusLinearLayout by lazy {
		val view: ExTouchFocusLinearLayout =
				LayoutInflater.from(this).inflate(R.layout.service_system_overlay_layer, null) as ExTouchFocusLinearLayout
		view.onInterceptInTouchEventListener = object: OnInterceptTouchEventListener {
			override fun onTouch(event: MotionEvent) {
				Log.d(TAG, "InTouch motionEvent.x, y = ${event.x}, ${event.y}")
				
				params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
						WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				windowManager.updateViewLayout(overlayView, params)
			}
		}
		view.onInterceptOutTouchEventListener = object: OnInterceptTouchEventListener {
			override fun onTouch(event: MotionEvent) {
				Log.d(TAG, "OutTouch motionEvent.x, y = ${event.x}, ${event.y}")
				params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
						WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
						WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				windowManager.updateViewLayout(overlayView, params)
			}
		}
		view
	}
	val windowFrameTop: LinearLayout by lazy { overlayView.findViewById(R.id.windowFrameTop) as LinearLayout }
	val windowFrame: LinearLayout by lazy { overlayView.findViewById(R.id.windowFrame) as LinearLayout }
	val titleBar: LinearLayout by lazy { overlayView.findViewById(R.id.titleBar) as LinearLayout }
	val bodyArea: LinearLayout by lazy { overlayView.findViewById(R.id.bodyArea) as LinearLayout }
	val webView: WebView by lazy { overlayView.findViewById(R.id.webView) as WebView }

	val overlayMiniView: ViewGroup by lazy { LayoutInflater.from(this).inflate(R.layout.service_system_overlay_mini_layer, null) as ViewGroup }

	// WindowManager
	val windowManager: WindowManager by lazy { applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

	// WindowManagerに設定するレイアウトパラメータ、オーバーレイViewの設定をする
	val params: WindowManager.LayoutParams by lazy { WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT,
			WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT,
			100,// X
			100,// Y
			//WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
			WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,   //なるべく上の階層で表示
			//WindowManager.LayoutParams.TYPE_PHONE,
			WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN	//座標系をスクリーンに合わせる
					//or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL  // タッチイベントを拾わない。ロック画面を邪魔しない
					//or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  // ウィンドウにフォーカスが当たった時だけ、無効にしたい
					or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // Viewの外のタッチイベントにも反応する？（端末ボタンが無効になる？）
			PixelFormat.TRANSLUCENT)
	}

	// ディスプレイのサイズを格納する
	val displaySize: Point by lazy {
		val display = windowManager.defaultDisplay
		val size = Point()
		display.getSize(size)

		//Log.d(TAG, "bodyArea.layoutParams.height="+bodyArea.layoutParams.height)
		//size.x -= bodyArea.width
		//size.y -= bodyArea.height
		size
	}

	// ロングタップ判定用
	//var isLongClick: Boolean = false

	override fun onBind(intent: Intent?): IBinder {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

		params.gravity = Gravity.TOP or Gravity.START// or Gravity.LEFT

		windowFrameTop.setOnFocusChangeListener({view: View, hasFocus: Boolean ->
			Log.d(TAG, "setOnFocusChangeListener hasFocus=$hasFocus")
		})

		windowFrame.setOnFocusChangeListener({view: View, hasFocus: Boolean ->
			Log.d(TAG, "setOnFocusChangeListener hasFocus=$hasFocus")
		})

		// applyを共通関数？で実行、apply()の戻り値はレシーバオブジェクト本体、つまり呼び出したインスタンスそのもの
		titleBar.apply(clickListener())

		overlayMiniView.setOnTouchListener({ view, motionEvent ->

			when (motionEvent.action) {
				MotionEvent.ACTION_UP -> {
					windowManager.removeView(overlayMiniView)
					windowManager.addView(overlayView, params)
				}
			}
			false
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

//    var oldX: Int? = null
//    var oldY: Int? = null

	// レシーバはView型で引数はなし、戻り値はUnit型（voidと同等）
	//  => あたかも、View.clickListener()の様に呼び出せる
	//  => clickListener()はapplyの引数を共通関数化？した関数
	private fun clickListener(): View.() -> Unit {
		return {
			setOnLongClickListener { view ->
				// ロングタップ状態にする
				//isLongClick = true
				// ロングタップ状態が分かりやすいように背景色を変える
				view.setBackgroundResource(android.R.color.holo_red_light)
				false

			}.apply {// Unit型にapply？？、以前のapplyのスコープが引き継がれるっぽい？

				setOnTouchListener (object: View.OnTouchListener {

					private var initialX: Int = 0
					private var initialY: Int = 0
					private var initialTouchX: Float = 0f
					private var initialTouchY: Float = 0f

					override fun onTouch(v: View, event: MotionEvent): Boolean {

						val touchAreaX: Float = displaySize.x.toFloat() - (bodyArea.width.toFloat() - event.x)
						val touchAreaY: Float = displaySize.y.toFloat() - (bodyArea.height.toFloat() - event.y)

						val rx: Float =
								if(event.rawX > touchAreaX) touchAreaX else event.rawX
						val ry: Float =
								if(event.rawY > touchAreaY) touchAreaY else event.rawY

						when (event.action) {
							MotionEvent.ACTION_DOWN -> {
								// Get current time in nano seconds.
								initialX = params.x
								initialY = params.y
								initialTouchX = rx
								initialTouchY = ry
							}
							MotionEvent.ACTION_UP -> {
								Log.d(TAG, "displaySize.x, y = ${displaySize.x}, ${displaySize.y}")
								Log.d(TAG, "motionEvent.rawX, rawY = $rx, $ry")
								Log.d(TAG, "motionEvent.x, y = ${event.x}, ${event.y}")
								Log.d(TAG, "touchArea.x, y = $touchAreaX, $touchAreaY")
								Log.d(TAG, "bodyArea.width, height=${bodyArea.width}, ${bodyArea.height}")
								Log.d(TAG, "(touchAreaX - rx), (touchAreaY - ry)=${(touchAreaX - rx)}, ${(touchAreaY - ry)}")

								if((touchAreaX - rx) in 0..10 || params.x in 0..10 || params.y in 0..10 || (touchAreaY - ry) in 0..(161+10) ) {
									// 両端に移動したら小さくする
									windowManager.removeView(overlayView)
									windowManager.addView(overlayMiniView, params)
								}
							}
							MotionEvent.ACTION_MOVE -> {
								params.x = initialX + (rx - initialTouchX).toInt()
								params.y = initialY + (ry - initialTouchY).toInt()

								if(params.x > displaySize.x - bodyArea.width)
									params.x = displaySize.x - bodyArea.width
								else if(params.x < 0)
									params.x = 0

								if(params.y > displaySize.y - bodyArea.height - titleBar.height)
									params.y = displaySize.y - bodyArea.height - titleBar.height
								else if(params.y < 0)
									params.y = 0

								Log.d(TAG, "params.x, y = ${params.x}, ${params.y}")

								windowManager.updateViewLayout(overlayView, params)
							}
							MotionEvent.ACTION_OUTSIDE -> {
								Log.d(TAG, "ACTION_OUTSIDE event.x, y = ${event.x}, ${event.y}")
								Log.d(TAG, "ACTION_OUTSIDE event.rawX, rawY = ${event.rawX}, ${event.rawY}")
							}
						}
						return false
					}
				})
				
			}
		}
	}
}