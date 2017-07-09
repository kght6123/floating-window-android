package jp.kght6123.smalllittleappviewer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.util.Log
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import jp.kght6123.smalllittleappviewer.custom.view.ExTouchFocusFrameLayout
import jp.kght6123.smalllittleappviewer.utils.UnitUtils

/**
 * オーバーレイウィンドウ表示用のテスト用サービスクラス2
 *
 * Created by kght6123 on 2017/05/09.
 */
class SystemOverlayLayerService2 : Service() {

	//private val TAG = this.javaClass.name

	val overlayWindowManager: OverlayWindowManager by lazy { OverlayWindowManager(applicationContext) }

	override fun onBind(intent: Intent?): IBinder {
		throw UnsupportedOperationException("Not yet implemented")
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

		val margin = UnitUtils.convertDp2Px(25f, applicationContext).toInt()

		overlayWindowManager.addOverlayView(1, margin, margin, false)
		overlayWindowManager.addOverlayView(2, margin*2, margin*2, false)

		// 前面で起動する
		val pendingIntent =
				PendingIntent.getActivity(this, 0, Intent(this, SystemOverlayLayerActivity::class.java), 0)
		val notification = Notification.Builder(this)
				.setContentTitle("スモールアプリ2")
				.setContentText("起動中")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.build()
		startForeground(startId, notification)

		return START_NOT_STICKY // 強制終了後に再起動されない
	}

	override fun onDestroy() {
		super.onDestroy()
		overlayWindowManager.finish()
	}

	class OverlayWindowManager(val context: Context) {

		val windowManager: WindowManager by lazy {
			context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		}

		val overlayView: ExTouchFocusFrameLayout by lazy {
			val overlayView = View.inflate(context, R.layout.service_system_overlay_layer2, null) as ExTouchFocusFrameLayout

			overlayView.onDispatchTouchEventListener = object: View.OnTouchListener {

				val TAG = this.javaClass.name
				val gestureDetector: GestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {

					override fun onSingleTapUp(event: MotionEvent): Boolean {
						Log.d(TAG, "SimpleOnGestureListener onSingleTapUp")

						// 単純にタッチした時のみ、Active判定
						changeActiveEvent(event)
						//return super.onSingleTapUp(event)
						return true
					}
				})
				override fun onTouch(view: View, event: MotionEvent): Boolean {
					when (event.action) {
						MotionEvent.ACTION_OUTSIDE -> {
							Log.d(TAG, "onDispatchTouchEventListener onTouch MotionEvent.ACTION_OUTSIDE")
							changeActiveEvent(event)	// OUTSIDEの時もActive判定
							return true
						}
					}
					Log.d(TAG, "onDispatchTouchEventListener onTouch .action=${event.action}, .rawX,rawY=${event.rawX},${event.rawY} .x,y=${event.x},${event.y}")

					gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う
					return false
				}
			}
			overlayView
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
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE// or
		//WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR

		val inactiveAlpha = 0.5f
		val inactiveDimAmount = 0.0f

		// WindowManagerに設定するレイアウトパラメータ、オーバーレイViewの設定をする
		val params: WindowManager.LayoutParams by lazy {
			val params = WindowManager.LayoutParams(
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.MATCH_PARENT,
					0, // X
					0, // Y
					//WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,   // ロック画面より上にくる
					WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // なるべく上の階層で表示
					//WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY // Android-O以降
					activeFlags,
					PixelFormat.TRANSLUCENT)
			params.gravity = Gravity.TOP or Gravity.START// or Gravity.LEFT
			params.dimAmount = activeDimAmount
			//params.windowAnimations = android.R.style.Animation//Animation_Translucent//Animation_Activity//Animation_Toast//Animation_Dialog
			params.windowAnimations = android.R.style.Animation_Translucent
			params.alpha = activeAlpha
			params
		}

		private fun getActiveParams(): WindowManager.LayoutParams {
			params.flags = activeFlags
			params.dimAmount = activeDimAmount
			params.alpha = activeAlpha
			return params
		}

		private fun getInActiveParams(): WindowManager.LayoutParams {
			params.width = WindowManager.LayoutParams.WRAP_CONTENT
			params.height = WindowManager.LayoutParams.WRAP_CONTENT
			params.flags = inactiveFlags
			params.dimAmount = inactiveDimAmount
			params.alpha = inactiveAlpha
			return params
		}

		val overlayWindowMap: MutableMap<String, OverlayWindowInfo> = LinkedHashMap()

		init {
			windowManager.addView(overlayView, getActiveParams())   // WindowManagerに追加
		}
		fun put(index: Int, miniMode: Boolean, x: Int?, y: Int?): OverlayWindowInfo {
			val name = "${context.packageName}.$index"
			val overlayWindowInfo = OverlayWindowInfo(context, this, name, miniMode)
			put(name, overlayWindowInfo)
			moveFixed(name, x!!, y!!)
			return overlayWindowInfo
		}
		fun put(name: String, overlayInfo: OverlayWindowInfo) {
			if(overlayWindowMap[name] != null)
				return

			overlayWindowMap.put(name, overlayInfo)  // 管理に追加
			overlayView.addView(overlayInfo.getActiveOverlay())

			updateActive(name)  // 追加したWindowをActiveに
			updateOtherDeActive(name)  // 追加したWindow以外をDeActiveに
		}
		private fun moveFixed(name: String, x: Int, y: Int) {
			val overlayInfo = overlayWindowMap[name]
			if(overlayInfo != null) {
				val params = overlayInfo.getActiveWindowLayoutParams()
				params.leftMargin = x
				params.topMargin = y
				overlayInfo.getActiveOverlay().layoutParams = params
			}
		}
		fun remove(name: String) {
			val overlayInfo = overlayWindowMap[name]
			if(overlayInfo != null){
				overlayWindowMap.remove(name)
				overlayView.removeView(overlayInfo.getActiveOverlay())
			}
			updateDeActive(name)
		}
		fun changeMode(name: String, miniMode: Boolean) {
			val overlayInfo = overlayWindowMap[name]
			if(overlayInfo != null){
				remove(name)
				overlayInfo.miniMode = miniMode
				put(name, overlayInfo)
			}
		}

		fun changeActive(name: String) {
			val overlayInfo = overlayWindowMap[name]
			if(overlayInfo != null) {
				remove(name)
				put(name, overlayInfo)
			}
		}
		private fun updateActive(name: String) {
			val overlayInfo = overlayWindowMap[name]
			overlayInfo?.activeFlag = true
			overlayInfo?.onActive()
		}
		private fun updateDeActive(name: String) {
			overlayWindowMap.forEach { entry ->
				if(entry.key == name) {
					entry.value.activeFlag = false
					entry.value.onDeActive()
				}
			}
		}
		private fun updateOtherDeActive(name: String) {
			overlayWindowMap.forEach { entry ->
				if(entry.key != name) {
					entry.value.activeFlag = false
					entry.value.onDeActive()
				}
			}
		}
		fun changeActiveEvent(event: MotionEvent) :Boolean {
			var changeActiveName: String? = null
			for ((overlayName, overlayInfo) in overlayWindowMap) {
				val onTouch = overlayInfo.isOnTouchEvent(event)
				if (!onTouch/* || changeActiveName != null*/) {
					// タッチされていない、他をActive済
					updateDeActive(overlayName)
				} else if (!overlayInfo.activeFlag) {
					// タッチされ、Active以外、他をActiveにしていない
					changeActiveName = overlayName
				} else if (overlayInfo.activeFlag) {
					// タッチされ、Active、他をActiveにしていない
					changeActiveName = ""
				}
			}
			if(changeActiveName == null)
				// nullの時、何もタッチされてないので、全体をinActiveへ
				windowManager.updateViewLayout(overlayView, getInActiveParams())
			else if(changeActiveName != "") {
				// 他のinActiveなウィンドウをタッチされた時、Activeへ
				changeActive(changeActiveName)
				windowManager.updateViewLayout(overlayView, getActiveParams())
			}
			return false
		}
		fun addOverlayView(index: Int, x: Int?, y: Int?, miniMode: Boolean): OverlayWindowInfo {
			return put(index, miniMode, x, y)// ここでビューをオーバーレイ領域に追加する
		}
		fun finish() {
			for ((overlayName, _) in overlayWindowMap.toList()) {
				this.remove(overlayName)
			}
			windowManager.removeViewImmediate(overlayView)
		}
	}
	class OverlayWindowInfo(val context: Context, val manager: OverlayWindowManager, val name: String, var miniMode: Boolean) {

		private val TAG = this.javaClass.name

		private enum class Stroke {
			UNKNOWN, TOP, BOTTOM, LEFT, RIGHT
		}
		enum class Mode {
			UNKNOWN, MOVE, RESIZE, FINISH
		}

		private var strokeMode: Stroke = Stroke.UNKNOWN
		var windowMode: Mode = Mode.UNKNOWN

		// ディスプレイのサイズを格納する
		val defaultDisplaySize: Point by lazy {
			val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
			val size = Point()
			display.getSize(size)
			size
		}

		val overlayMiniView: ViewGroup by lazy {

			val miniView =
					View.inflate(context, R.layout.small_mini_webview, null) as LinearLayout

			val layoutParams =
					FrameLayout.LayoutParams(
							UnitUtils.convertDp2Px(75f, context).toInt(),
							UnitUtils.convertDp2Px(75f, context).toInt())
			layoutParams.leftMargin = 100
			layoutParams.topMargin = 100

			miniView.layoutParams = layoutParams

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
				var initialTouchX: Float = 0f
				var initialTouchY: Float = 0f
				var windowMode: Mode = Mode.UNKNOWN

				override fun onTouch(view: View, event: MotionEvent): Boolean {
					Log.d(TAG, "overlayMiniView event.action = ${event.action}")

					val rx: Float = event.rawX
					val ry: Float = event.rawY

					val params = getActiveWindowLayoutParams()

					when (event.action) {
						MotionEvent.ACTION_UP -> {
							if(windowMode != Mode.MOVE )
								manager.changeMode(this@OverlayWindowInfo.name, false)
							windowMode = Mode.FINISH
						}
						MotionEvent.ACTION_MOVE -> {
							windowMode = Mode.MOVE

							// 移動中の処理
							params.leftMargin = initialX + (rx - initialTouchX).toInt()
							params.topMargin = initialY + (ry - initialTouchY).toInt()

							getActiveOverlay().layoutParams = params
						}
						MotionEvent.ACTION_DOWN -> {
							windowMode = Mode.UNKNOWN

							// 移動・拡大縮小のための初期値設定
							initialX = params.leftMargin
							initialY = params.topMargin
							initialTouchX = rx
							initialTouchY = ry
						}
					}
					//return false ここで伝搬を止めないと、ACTION_DOWN以降が動かない
					return true
				}
			})

			val iconView = ImageView(context)
			iconView.setImageResource(R.mipmap.ic_launcher)
			iconView.isFocusableInTouchMode = true
			iconView.isFocusable = true

			val layoutParams1 =
					LinearLayout.LayoutParams(
							UnitUtils.convertDp2Px(75f, context).toInt(),
							UnitUtils.convertDp2Px(75f, context).toInt())

			miniView.addView(iconView, layoutParams1)

			miniView
		}
		val windowOutFrame: LinearLayout by lazy {
			val windowOutFrame = View.inflate(context, R.layout.service_system_overlay_window2, null) as LinearLayout
			val layoutParams =
					FrameLayout.LayoutParams(
							UnitUtils.convertDp2Px(300f, context).toInt(),
							UnitUtils.convertDp2Px(450f, context).toInt())

			windowOutFrame.layoutParams = layoutParams
			windowOutFrame
		}

		val windowInFrame: ViewGroup by lazy {

			val windowInFrame =
					windowOutFrame.findViewById(R.id.windowInFrame) as LinearLayout
			val mainLayoutView =
					View.inflate(context, R.layout.small_webview, windowInFrame) as ViewGroup

			val webView = mainLayoutView.findViewById(R.id.webView) as WebView
			webView.setWebViewClient(object : WebViewClient() {
				override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
					return false
				}
			})
			webView.loadUrl("http://www.google.com")

			windowOutFrame.setOnLongClickListener {
				if(strokeMode != Stroke.UNKNOWN && windowMode == Mode.UNKNOWN) {
					// リサイズモードの背景色に変える
					windowInFrame.setBackgroundResource(R.color.colorAccent)
					windowMode = Mode.RESIZE
				}
				return@setOnLongClickListener false
			}
			val switchMiniMode = fun() {
				if(/*windowMode == Mode.UNKNOWN && */strokeMode != Stroke.UNKNOWN) {
					manager.changeMode(this.name, true)
				}
			}
			windowOutFrame.setOnTouchListener(object: View.OnTouchListener {
				var onGestureListener: GestureDetector.OnGestureListener = object: GestureDetector.SimpleOnGestureListener() {

					override fun onDoubleTap(event: MotionEvent): Boolean {
						Log.d(TAG, "SimpleOnGestureListener onDoubleTap")
						Log.d(TAG, "SimpleOnGestureListener onDoubleTap $strokeMode")

						when (strokeMode) {
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
				}
				val gestureDetector: GestureDetector by lazy {
					GestureDetector(context, onGestureListener)
				}

				private val strokeWidth: Int = UnitUtils.convertDp2Px(3f, context).toInt() + 40/*ぼかしの分*/

				private var initialX: Int = 0
				private var initialY: Int = 0
				private var initialWidth: Int = 0
				private var initialHeight: Int = 0
				private var initialTouchX: Float = 0f
				private var initialTouchY: Float = 0f

				override fun onTouch(view: View, event: MotionEvent): Boolean {

					Log.d(TAG, "motionEvent.action = ${event.action}")
					Log.d(TAG, "motionEvent.rawX, rawY = ${event.rawX}, ${event.rawY}")
					Log.d(TAG, "motionEvent.x, y = ${event.x}, ${event.y}")

					val rx: Float = event.rawX
					val ry: Float = event.rawY

					val params = getActiveWindowLayoutParams()
					Log.d(TAG, "params.width, height=${params.width}, ${params.height}")
					Log.d(TAG, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

					when (event.action) {
						MotionEvent.ACTION_CANCEL -> {

						}
						MotionEvent.ACTION_DOWN -> {

							// モードのリセット
							windowMode = Mode.UNKNOWN
							strokeMode = Stroke.UNKNOWN

							Log.d(TAG, "params.width-this.strokeWidth = ${params.width-this.strokeWidth}")
							Log.d(TAG, "params.height-this.strokeWidth = ${params.height-this.strokeWidth}")

							if(event.x in 0..this.strokeWidth){
								strokeMode = Stroke.LEFT
								Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")

							}
							if(event.x in (params.width-this.strokeWidth)..params.width) {
								strokeMode = Stroke.RIGHT
								Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")

							}
							if(event.y in 0..this.strokeWidth) {
								strokeMode = Stroke.TOP
								Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")

							}
							if(event.y in (params.height-this.strokeWidth)..params.height) {
								strokeMode = Stroke.BOTTOM
								Log.d(TAG, "dispatchTouchEvent $strokeMode strokeWidth=$strokeWidth")

							}
							if(strokeMode != Stroke.UNKNOWN) {
								// 移動・拡大縮小のための初期値設定
								initialX = params.leftMargin
								initialY = params.topMargin
								initialWidth = params.width
								initialHeight = params.height
								initialTouchX = rx
								initialTouchY = ry

								windowInFrame.setBackgroundResource(android.R.color.holo_blue_dark)
							}
						}
						MotionEvent.ACTION_UP -> {

							if(windowMode == Mode.MOVE && strokeMode != Stroke.UNKNOWN) {
								// 移動完了
								Log.d(TAG, "displaySize.x, y = ${defaultDisplaySize.x}, ${defaultDisplaySize.y}")
								Log.d(TAG, "motionEvent.rawX, rawY = $rx, $ry")
								Log.d(TAG, "motionEvent.x, y = ${event.x}, ${event.y}")
								Log.d(TAG, "params.width, height=${params.width}, ${params.height}")
								Log.d(TAG, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

								windowMode = Mode.FINISH    // フリック誤作動防止
							}
							if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
								windowMode = Mode.FINISH    // フリック誤作動防止
							}
							if(strokeMode != Stroke.UNKNOWN) {
								// ACTION_UP,DOWNのみの対策。
								windowInFrame.setBackgroundResource(android.R.color.background_light)
							}
						}
						MotionEvent.ACTION_MOVE -> {
							val space = 0
							if(windowMode != Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {
								// 移動中の処理
								params.leftMargin = initialX + (rx - initialTouchX).toInt()
								params.topMargin = initialY + (ry - initialTouchY).toInt()

								Log.d(TAG, "params.x, y = ${params.leftMargin}, ${params.topMargin}")

								windowMode = Mode.MOVE
							}
							if(windowMode == Mode.RESIZE && strokeMode != Stroke.UNKNOWN) {

								val limitMaxWidth = fun(){
									if(defaultDisplaySize.x < params.width)
										params.width = defaultDisplaySize.x
								}
								val limitMaxHeight = fun(){
									if(defaultDisplaySize.y - space < params.height)
										params.height = defaultDisplaySize.y - space // 通知バー、ナビゲーションバーの考慮(space)
								}
								when(strokeMode){
									Stroke.TOP -> {
										params.height = (initialHeight - (ry - initialTouchY).toInt())
										params.topMargin = initialY + (ry - initialTouchY).toInt()
										limitMaxHeight()
										//limitMaxY()
									}
									Stroke.BOTTOM -> {
										params.height = (initialHeight + (ry - initialTouchY).toInt())
										limitMaxHeight()
									}
									Stroke.LEFT -> {
										params.width = (initialWidth - (rx - initialTouchX).toInt())
										params.leftMargin = initialX + (rx - initialTouchX).toInt()
										limitMaxWidth()
										//limitMaxX()
									}
									Stroke.RIGHT -> {
										params.width = (initialWidth + (rx - initialTouchX).toInt())
										limitMaxWidth()
									}
									else -> {

									}
								}
							}
							getActiveOverlay().layoutParams = params
						}
						MotionEvent.ACTION_OUTSIDE -> {
							Log.d(TAG, "ACTION_OUTSIDE event.x, y = ${event.x}, ${event.y}")
							Log.d(TAG, "ACTION_OUTSIDE event.rawX, rawY = ${event.rawX}, ${event.rawY}")
						}
					}
					gestureDetector.onTouchEvent(event)// ジェスチャー機能を使う

					return false
				}
			})
			windowInFrame
		}

		var activeFlag: Boolean = false

		init {
			overlayMiniView
			windowInFrame
		}
		fun getActiveOverlay(): ViewGroup {
			if(this.miniMode)
				return this.overlayMiniView
			else
				return this.windowOutFrame
		}
		private fun getLayoutParams(viewGroup: ViewGroup): FrameLayout.LayoutParams {
			return (viewGroup.layoutParams as FrameLayout.LayoutParams)
		}
//		fun getMiniFrameLayoutParams(): FrameLayout.LayoutParams {
//			return getLayoutParams(this.overlayMiniView)
//		}
//		fun getWindowOutFrameLayoutParams(): FrameLayout.LayoutParams {
//			return getLayoutParams(this.windowOutFrame)
//		}
		fun getActiveWindowLayoutParams(): FrameLayout.LayoutParams {
			return getLayoutParams(getActiveOverlay())
		}
		fun onActive() {}
		fun onDeActive() {}

		fun isOnTouchEvent(event: MotionEvent): Boolean {
			val params = getActiveWindowLayoutParams()
			return event.rawX in params.leftMargin..(params.leftMargin + params.width)
					&& event.rawY in params.topMargin..(params.topMargin + params.height)
		}
	}
}