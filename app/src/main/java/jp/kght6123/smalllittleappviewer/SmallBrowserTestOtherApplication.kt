package jp.kght6123.smalllittleappviewer

import android.content.Intent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import jp.kght6123.multiwindowframework.MultiFloatWindowApplication
import jp.kght6123.multiwindowframework.MultiWindowUpdatePosition
import jp.kght6123.multiwindowframework.utils.UnitUtils
import java.util.*

/**
 * サンプルプラグインアプリケーション
 *
 * Created by kght6123 on 2017/09/16.
 */
class SmallBrowserTestOtherApplication : MultiFloatWindowApplication() {
    companion object {
        private val additionalHttpHeaders = TreeMap<String, String>()
        init {
            additionalHttpHeaders.put("Accept-Encoding", "gzip")
        }
    }
    override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
        return object : MultiFloatWindowViewFactory() {

            val view by lazy { createContentView(R.layout.small_webview) }
            val webView by lazy { view.findViewById(R.id.webView) as WebView }

            override fun createWindowView(arg: Int): View {
                webView.setWebViewClient(
                    object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            view?.loadUrl(request?.url?.toString(), additionalHttpHeaders)
                            return true
                        }
                    }
                )
                return view
            }
            override fun createMinimizedView(arg: Int): View {
                val mMinimizedView = ImageView(sharedContext)
                mMinimizedView.setImageResource(R.mipmap.ic_launcher)
                mMinimizedView.isFocusableInTouchMode = true
                mMinimizedView.isFocusable = true
                return mMinimizedView
            }
            override fun createMinimizedLayoutParams(arg: Int): LinearLayout.LayoutParams {
                return LinearLayout.LayoutParams(
                        UnitUtils.convertDp2Px(75f, sharedContext!!).toInt(),
                        UnitUtils.convertDp2Px(75f, sharedContext!!).toInt())
            }
            override fun createWindowLayoutParams(arg: Int): LinearLayout.LayoutParams {
                return LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
            }
            override fun start(intent: Intent?) {
                if (intent != null && intent.dataString != null) {
                    Toast.makeText(applicationContext, "hello!! small browser test. ${intent.dataString}", Toast.LENGTH_SHORT).show()
                    webView.loadUrl(intent.dataString)
                } else {
                    Toast.makeText(applicationContext, "hello!! small browser test.", Toast.LENGTH_SHORT).show()
                    webView.loadUrl("https://www.google.com/")
                }
            }
            override fun update(intent: Intent?, index: Int, positionName: String) {
                if(positionName == MultiWindowUpdatePosition.FIRST.name
                        || positionName == MultiWindowUpdatePosition.INDEX.name) {
                    start(intent)
                }
            }
        }
    }

    override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
        return object :  MultiFloatWindowSettingsFactory {
            override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
                return MultiFloatWindowInitSettings(
                        sharedContext!!.getResources().getDimensionPixelSize(R.dimen.x),
                        sharedContext!!.getResources().getDimensionPixelSize(R.dimen.y),
                        sharedContext!!.getResources().getDimensionPixelSize(R.dimen.width),
                        sharedContext!!.getResources().getDimensionPixelSize(R.dimen.height),
                        sharedContext!!.getColor(android.R.color.background_light)
                )
            }
        }
    }
}