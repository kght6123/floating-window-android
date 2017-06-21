package jp.kght6123.smalllittleappviewer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import jp.kght6123.smalllittleappviewer.custom.view.ExTouchFocusFrameLayout

/**
 * オーバーレイウィンドウ表示用のテスト用サービスクラス2
 *
 * Created by kght6123 on 2017/05/09.
 */
class SystemOverlayLayerService2 : Service() {

//	private val TAG = this.javaClass.simpleName

	val windowManager: WindowManager by lazy {
		getSystemService(Context.WINDOW_SERVICE) as WindowManager
	}

	val overlayView: ExTouchFocusFrameLayout by lazy {
		View.inflate(this, R.layout.service_system_overlay_layer2, null) as ExTouchFocusFrameLayout
	}

	val webView1 by lazy {
		overlayView.findViewById(R.id.webView1) as WebView
	}
	val webView2 by lazy {
		overlayView.findViewById(R.id.webView2) as WebView
	}

	val btnChange by lazy {
		overlayView.findViewById(R.id.btnChange) as Button
	}

		// ディスプレイのサイズを格納する
//	val defaultDisplaySize: Point by lazy {
//		val display = windowManager.defaultDisplay
//		val size = Point()
//		display.getSize(size)
//		size
//	}

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

	override fun onBind(intent: Intent?): IBinder {
		throw UnsupportedOperationException("Not yet implemented")
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

		webView1.setWebViewClient(object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
				return false
			}
		})
		webView1.loadUrl("http://www.google.com")

		webView2.setWebViewClient(object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
				return false
			}
		})
		webView2.loadUrl("http://www.google.com")

		btnChange.setOnClickListener {
			val c2 = overlayView.findViewById(R.id.child2) as LinearLayout
			overlayView.removeView(c2)
			overlayView.addView(c2, 0)

			overlayView.clipChildren = false
			overlayView.clipToOutline = false
			overlayView.clipToPadding = false
			overlayView.background = null
			overlayView.foreground = null

			//overlayView.layoutParams.width = 500
			//overlayView.layoutParams.height = 500

			windowManager.updateViewLayout(overlayView, getInActiveParams())

			overlayView.clipChildren = false
			overlayView.clipToOutline = false
			overlayView.clipToPadding = false
			overlayView.background = null
			overlayView.foreground = null
		}

		windowManager.addView(overlayView, getActiveParams())   // WindowManagerに追加

		// 前面で起動する
		val pendingIntent =
				PendingIntent.getActivity(this, 0, Intent(this, SystemOverlayLayerActivity::class.java), 0)
		val notification = Notification.Builder(this)
				.setContentTitle("スモールアプリ")
				.setContentText("起動中")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.build()
		startForeground(startId, notification)

		return START_NOT_STICKY // 強制終了後に再起動されない
	}

	override fun onDestroy() {
		super.onDestroy()
		windowManager.removeViewImmediate(overlayView)
	}
}