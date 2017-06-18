package jp.kght6123.smalllittleappviewer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import jp.kght6123.smalllittleappviewer.manager.OverlayWindowManager


/**
 * オーバーレイウィンドウ表示用のテスト用サービスクラス
 *
 * Created by kght6123 on 2017/05/09.
 */
class SystemOverlayLayerService : Service() {

	//private val TAG = this.javaClass.simpleName
	
	// オーバーレイビューをコントロールするマネージャー
	val overlayManager: OverlayWindowManager by lazy {
		OverlayWindowManager(this)
	}

	override fun onBind(intent: Intent?): IBinder {
		throw UnsupportedOperationException("Not yet implemented")
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		// Overlay用のViewを追加
		val webView1 = this.overlayManager.addOverlayView(this, 1, R.layout.small_webview).findViewById(R.id.webView) as WebView
		val webView2 = this.overlayManager.addOverlayView(this, 2, R.layout.small_webview).findViewById(R.id.webView) as WebView

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
		this.overlayManager.finish()
	}
}