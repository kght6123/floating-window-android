package jp.kght6123.smalllittleappviewer

import android.content.Intent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.Toast
import jp.kght6123.multiwindowframework.MultiFloatWindowApplication
import jp.kght6123.multiwindowframework.MultiWindowUpdatePosition
import java.util.*

/**
 * サンプルプラグインアプリケーション
 *
 * @author    kght6123
 * @copyright 2017/09/16 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class SmallBrowserTestOtherApplication : MultiFloatWindowApplication() {

    companion object {
        private val additionalHttpHeaders = TreeMap<String, String>()
        init {
            additionalHttpHeaders.put("Accept-Encoding", "gzip")
        }
    }
    override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
        return object : MultiFloatWindowViewFactory(multiWindowContext) {

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
            override fun onActive() {
                //Toast.makeText(applicationContext, "onActive", Toast.LENGTH_SHORT).show()
            }
            override fun onDeActive() {
                //Toast.makeText(applicationContext, "onDeActive", Toast.LENGTH_SHORT).show()
            }
            override fun onDeActiveAll() {
                //Toast.makeText(applicationContext, "onDeActiveAll", Toast.LENGTH_SHORT).show()
            }
            override fun onChangeMiniMode() {
                //Toast.makeText(applicationContext, "onChangeMiniMode", Toast.LENGTH_SHORT).show()
            }
            override fun onChangeWindowMode() {
                //Toast.makeText(applicationContext, "onChangeWindowMode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
        return object : MultiFloatWindowSettingsFactory(multiWindowContext) {
            override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
                return MultiFloatWindowInitSettings(
                        getDimensionPixelSize(R.dimen.x),
                        getDimensionPixelSize(R.dimen.y),
                        getDimensionPixelSize(R.dimen.width),
                        getDimensionPixelSize(R.dimen.height),
                        getColor(android.R.color.background_light)
                )
            }
        }
    }
}