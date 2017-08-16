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

	override fun onCreate() {
		super.onCreate()

		title = "マルチウィンドウブラウザテスト"

		initSettings = MultiFloatWindowInitSettings(
				UnitUtils.convertDp2Px(25f, applicationContext).toInt(),
				UnitUtils.convertDp2Px(25f, applicationContext).toInt(),
				UnitUtils.convertDp2Px(300f, applicationContext).toInt(),
				UnitUtils.convertDp2Px(450f, applicationContext).toInt(),
				getColor(android.R.color.background_light)
		)
		notificationSettings = MultiFloatWindowNotificationSettings(
				"マルチウィンドウアプリ",
				"起動中",
				Icon.createWithResource(applicationContext, R.mipmap.ic_launcher_round),
				PendingIntent.getActivity(this, 0, Intent(this, MultiFloatWindowTestActivity::class.java), 0)
		)
		windowLayoutParamFactory = object : MultiFloatWindowLayoutParamFactory {
			override fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams {
				return LinearLayout.LayoutParams(
						UnitUtils.convertDp2Px(75f, applicationContext).toInt(),
						UnitUtils.convertDp2Px(75f, applicationContext).toInt())
			}
			override fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams {
				return LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT)
			}
		}
		windowViewFactory = object : MultiFloatWindowViewFactory {
			override fun createMinimizedView(arg: Int): View {
				val iconView = ImageView(applicationContext)
				iconView.setImageResource(R.mipmap.ic_launcher)
				iconView.isFocusableInTouchMode = true
				iconView.isFocusable = true
				return iconView
			}
			override fun createWindowView(arg: Int): View {
				val webView = View.inflate(applicationContext, R.layout.small_webview, null).findViewById(R.id.webView) as WebView
				webView.setWebViewClient(object : WebViewClient() {
					override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
						return false
					}
				})
				webView.loadUrl("http://www.google.com")
				return webView
			}
			override fun start(intent: Intent?) {}
		}
	}
}