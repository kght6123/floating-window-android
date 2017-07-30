package jp.kght6123.smalllittleappviewer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import jp.kght6123.multiwindow.MultiFloatWindowManager
import jp.kght6123.multiwindow.utils.UnitUtils

/**
 * マルチウィンドウライブラリのテスト用サービスクラス
 *
 * Created by kght6123 on 2017/05/09.
 */
class MultiFloatWindowTestService : Service() {

	//private val TAG = this.javaClass.name

	val manager: MultiFloatWindowManager by lazy { MultiFloatWindowManager(applicationContext) }
	val iconView1 by lazy {
		val iconView = ImageView(applicationContext)
		iconView.setImageResource(R.mipmap.ic_launcher)
		iconView.isFocusableInTouchMode = true
		iconView.isFocusable = true
		iconView
	}
	val iconViewLayoutParam1 by lazy {
		LinearLayout.LayoutParams(
				UnitUtils.convertDp2Px(75f, applicationContext).toInt(),
				UnitUtils.convertDp2Px(75f, applicationContext).toInt())
	}
	val webView1 by lazy {
		val webView = View.inflate(applicationContext, R.layout.small_webview, null).findViewById(R.id.webView) as WebView
		webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
                return false
            }
        })
        webView.loadUrl("http://www.google.com")
		webView
	}

	val iconView2 by lazy {
		val iconView = ImageView(applicationContext)
		iconView.setImageResource(R.mipmap.ic_launcher)
		iconView.isFocusableInTouchMode = true
		iconView.isFocusable = true
		iconView
	}
	val iconViewLayoutParam2 by lazy {
		LinearLayout.LayoutParams(
				UnitUtils.convertDp2Px(75f, applicationContext).toInt(),
				UnitUtils.convertDp2Px(75f, applicationContext).toInt())
	}
	val webView2 by lazy {
		val webView = View.inflate(applicationContext, R.layout.small_webview, null).findViewById(R.id.webView) as WebView
		webView.setWebViewClient(object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
				return false
			}
		})
		webView.loadUrl("http://www.google.com")
		webView
	}

	override fun onCreate() {
		super.onCreate()
	}

	override fun onBind(intent: Intent?): IBinder {
		throw UnsupportedOperationException("Not yet implemented")
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

		val margin = UnitUtils.convertDp2Px(25f, applicationContext).toInt()
		val initWidth = UnitUtils.convertDp2Px(300f, applicationContext).toInt()
		val initHeight = UnitUtils.convertDp2Px(450f, applicationContext).toInt()

		val info1 = manager.add(1, margin, margin, false, getColor(android.R.color.background_light), initWidth, initHeight)
		info1.miniWindowFrame.addView(iconView1, iconViewLayoutParam1)
		info1.windowInlineFrame.addView(webView1)

		val info2 = manager.add(2, margin*2, margin*2, false, getColor(android.R.color.background_light), initWidth, initHeight)
		info2.miniWindowFrame.addView(iconView2, iconViewLayoutParam2)
		info2.windowInlineFrame.addView(webView2)

		// 前面で起動する
		val pendingIntent =
				PendingIntent.getActivity(this, 0, Intent(this, MultiFloatWindowTestActivity::class.java), 0)
		val notification = Notification.Builder(this)
				.setContentTitle("マルチウィンドウアプリ")
				.setContentText("起動中")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.build()
		startForeground(startId, notification)

		return START_NOT_STICKY // 強制終了後に再起動されない
	}

	override fun onDestroy() {
		super.onDestroy()
		manager.finish()
	}
}