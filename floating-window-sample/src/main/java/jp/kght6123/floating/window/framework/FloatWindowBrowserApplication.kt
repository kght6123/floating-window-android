package jp.kght6123.floating.window.framework

import android.content.Intent
import android.graphics.Bitmap
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.Toast
import java.util.*

/**
 * サンプルプラグインアプリケーション
 *
 * @author    kght6123
 * @copyright 2017/09/16 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowBrowserApplication : FloatWindowApplication() {

    companion object {
        private val additionalHttpHeaders = TreeMap<String, String>()
        init {
            additionalHttpHeaders.put("Accept-Encoding", "gzip")
        }
    }
    private val tag = FloatWindowBrowserApplication::class.java.name
    override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
        return object : MultiFloatWindowViewFactory(multiWindowContext) {

            val view by lazy { createContentView(R.layout.floatwindow_webview) }
            val webView by lazy { view.findViewById(R.id.webView) as WebView }

            val mMinimizedView by lazy {
                val imageView = ImageView(sharedContext)
                imageView.setImageResource(R.mipmap.ic_launcher)
                imageView
            }

            override fun createWindowView(arg: Int): View {
                webView.setWebViewClient(
                    object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            view?.loadUrl(request?.url?.toString(), additionalHttpHeaders)
                            return true
                        }
                    }
                )
                webView.setWebChromeClient(
                    object : WebChromeClient() {
                        override fun onCreateWindow(view: WebView,
                                                    isDialog: Boolean, isUserGesture: Boolean,
                                                    resultMsg: Message): Boolean {
                            val result = super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                            Log.d(tag, "WebChromeClient onCreateWindow")
                            mMinimizedView.setImageBitmap(view.favicon)
                            return result
                        }
                        override fun onCloseWindow(view: WebView) {
                            super.onCloseWindow(view)
                            Log.d(tag, "WebChromeClient onCloseWindow")
                            mMinimizedView.setImageBitmap(view.favicon)
                        }
                        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
                            super.onReceivedIcon(view, icon)
                            Log.d(tag, "WebChromeClient onReceivedIcon")
                            mMinimizedView.setImageBitmap(icon)
                        }
                        override fun onProgressChanged(view: WebView, newProgress: Int) {

                        }
                    }
                )
                webView.scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY

                // マルチタッチでのピンチズームを有効化
                val ws = webView.settings
                ws.builtInZoomControls = true
                ws.setSupportZoom(true)
                ws.displayZoomControls = false// ズームボタンを出さない

                ws.javaScriptCanOpenWindowsAutomatically = true

                ws.loadWithOverviewMode = true

                ws.loadsImagesAutomatically = true
                ws.blockNetworkImage = false
                ws.blockNetworkLoads = false

                ws.mediaPlaybackRequiresUserGesture = true
                ws.useWideViewPort = true

                @Suppress("DEPRECATION")
                ws.pluginState = WebSettings.PluginState.ON

                //ws.setSupportMultipleWindows(support);

                // キャッシュを設定
                ws.setAppCacheEnabled(true)
                ws.setAppCachePath(externalCacheDir.path)

                ws.databaseEnabled = true
                ws.domStorageEnabled = true
                ws.loadWithOverviewMode = true
                ws.useWideViewPort = true

                return view
            }
            override fun createMinimizedView(arg: Int): View {
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
                        width = getDimensionPixelSize(R.dimen.width),
                        height = getDimensionPixelSize(R.dimen.height),
                        theme = MultiFloatWindowConstants.Theme.Light,
                        anchor = MultiFloatWindowConstants.Anchor.Edge
                )
            }
        }
    }
}