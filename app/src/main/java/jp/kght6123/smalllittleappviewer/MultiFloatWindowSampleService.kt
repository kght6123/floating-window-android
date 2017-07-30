package jp.kght6123.smalllittleappviewer

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import jp.kght6123.multiwindow.MultiFloatWindowApplication
import jp.kght6123.multiwindow.utils.UnitUtils


/**
 * マルチウィンドウライブラリのサンプルサービスクラス
 *
 * Created by kght6123 on 2017/05/09.
 */
class MultiFloatWindowSampleService : MultiFloatWindowApplication() {
	override fun getIconView(): View {
		val iconView = ImageView(applicationContext)
		iconView.setImageResource(R.mipmap.ic_launcher)
		iconView.isFocusableInTouchMode = true
		iconView.isFocusable = true
		return iconView
	}
	override fun getIconLayoutParam(): LinearLayout.LayoutParams {
		return LinearLayout.LayoutParams(
				UnitUtils.convertDp2Px(75f, applicationContext).toInt(),
				UnitUtils.convertDp2Px(75f, applicationContext).toInt())
	}
	override fun getWindowView(): View {
		val webView = View.inflate(applicationContext, R.layout.small_webview, null).findViewById(R.id.webView) as WebView
		webView.setWebViewClient(object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
				return false
			}
		})
		webView.loadUrl("http://www.google.com")
		return webView
	}
	override fun getWindowLayoutParam(): LinearLayout.LayoutParams {
		return LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT)
	}
	override fun getX(): Int {
		return UnitUtils.convertDp2Px(25f, applicationContext).toInt()
	}
	override fun getY(): Int {
		return UnitUtils.convertDp2Px(25f, applicationContext).toInt()
	}
	override fun getInitWidth(): Int {
		return UnitUtils.convertDp2Px(300f, applicationContext).toInt()
	}
	override fun getInitHeight(): Int {
		return UnitUtils.convertDp2Px(450f, applicationContext).toInt()
	}
	override fun getBackgroundColor(): Int {
		return getColor(android.R.color.background_light)
	}
	override fun getNotificationTitle(): String {
		return "マルチウィンドウアプリ"
	}
	override fun getNotificationText(): String {
		return "起動中"
	}
	override fun getNotificationIcon(): Icon {
		return Icon.createWithResource(applicationContext, R.mipmap.ic_launcher_round)
	}
	override fun getNotificationPendingIntent(): PendingIntent {
		val pendingIntent =
				PendingIntent.getActivity(this, 0, Intent(this, MultiFloatWindowTestActivity::class.java), 0)
		return pendingIntent
	}
}