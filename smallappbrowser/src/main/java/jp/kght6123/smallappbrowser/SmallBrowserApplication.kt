package jp.kght6123.smallappbrowser

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.webkit.*
import android.widget.*
import jp.kght6123.floating.window.framework.FloatWindowApplication
import jp.kght6123.floating.window.framework.MultiFloatWindowConstants
import jp.kght6123.floating.window.framework.MultiWindowUpdatePosition
import jp.kght6123.smallappbrowser.adapter.WebHistoryItemAdapter
import jp.kght6123.smallappbrowser.application.SharedDataApplication
import jp.kght6123.smallappbrowser.utils.PrefUtils
import java.util.*


/**
 * スモールブラウザのサービス提供クラス
 *
 * @author    kght6123
 * @copyright 2017/05/09 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class SmallBrowserApplication : FloatWindowApplication() {

    private val TAG = SmallBrowserApplication::class.java.simpleName

	private enum class MoveControlArea {
		Left,
		Right,
		Bottom,
		Bottom2
	}

	companion object {
		private val additionalHttpHeaders = TreeMap<String, String>()
		init {
			additionalHttpHeaders.put("Accept-Encoding", "gzip")
		}
		val EXTRA_JAVASCRIPT_DISABLED = "kght6123.intent.EXTRA_JAVASCRIPT_DISABLED"
	}

	private var defaultCacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK


    override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
        return object : MultiFloatWindowViewFactory(multiWindowContext) {

            private val mainView by lazy { createContentView(R.layout.smallapp_browser_main) }
            private val webView by lazy { mainView.findViewById(R.id.webview) as WebView }
            private val progressBarForWeb by lazy { mainView.findViewById(R.id.progressBarForWeb) as ProgressBar }

            private var mMinimizedView: ImageView? = null
            private var focusUrl: String? = null

            private val onClickBackListener = View.OnClickListener {
                if (webView.canGoBack()) {
                    webView.settings.cacheMode = WebSettings.LOAD_CACHE_ONLY
                    webView.goBack()
                    webView.settings.cacheMode = defaultCacheMode
                }
                //else
                //	this@SmallBrowserApplication.finish()
            }
            private val onClickForwardListener = View.OnClickListener {
                if (webView.canGoForward()) {
                    webView.settings.cacheMode = WebSettings.LOAD_CACHE_ONLY
                    webView.goForward()
                    webView.settings.cacheMode = defaultCacheMode
                } else
                    webView.loadUrl("https://www.google.com/", additionalHttpHeaders)
            }
            private val onClickChromeListener = View.OnClickListener {
                val pm = packageManager
                val intent = pm.getLaunchIntentForPackage("com.android.chrome")
                intent.data = Uri.parse(webView.url)
                intent.action = Intent.ACTION_VIEW
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                startActivity(intent)

                // getWindow().setWindowState(WindowState.MINIMIZED)
            }
            private val onClickSharedListener = View.OnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, webView.url)
                intent.putExtra(Intent.EXTRA_SUBJECT, webView.title)
                // intent.putExtra(Browser.EXTRA_SHARE_FAVICON, favicon);
                // intent.putExtra(Browser.EXTRA_SHARE_SCREENSHOT, screenshot);

                try {
                    val send = Intent.createChooser(intent, getText(R.string.send))
                    send.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(send)
                } catch (ex: ActivityNotFoundException) {
                    // if no app handles it, do nothing
                }
                // getWindow().setWindowState(WindowState.MINIMIZED)
            }

            private var histAdapter: WebHistoryItemAdapter? = null
            private var pinAdapter: WebHistoryItemAdapter? = null

            private val onClickHistListener = View.OnClickListener {
                val listView = mainView.findViewById(R.id.browserHistoryListView) as ListView
                if (listView.visibility != View.GONE) {
                    listView.visibility = View.GONE
                    listView.adapter = null
                    histAdapter = null
                } else {
                    val sharedData = this@SmallBrowserApplication.applicationContext as SharedDataApplication
                    histAdapter = WebHistoryItemAdapter(this.webView, this@SmallBrowserApplication, 0, sharedData.webHistoryItemList, additionalHttpHeaders, true)

                    listView.visibility = View.VISIBLE
                    listView.adapter = histAdapter
                }
            }
            private val onClickPinListener = View.OnClickListener {
                val listView = mainView.findViewById(R.id.browserPinListView) as ListView
                if (listView.visibility != View.GONE) {
                    listView.visibility = View.GONE
                    listView.adapter = null
                    pinAdapter = null
                } else {
                    val sharedData = this@SmallBrowserApplication.applicationContext as SharedDataApplication
                    pinAdapter = WebHistoryItemAdapter(this.webView,this@SmallBrowserApplication, 0, sharedData.webPinItemList, additionalHttpHeaders, false)

                    listView.visibility = View.VISIBLE
                    listView.adapter = pinAdapter
                }
            }
            override fun createWindowView(arg: Int): View {

                CookieManager.setAcceptFileSchemeCookies(true)

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.acceptCookie()

                webView.setWebViewClient(
                        object : WebViewClient() {
                            private var historyItem: SharedDataApplication.WebHistoryItem? = null
                            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                progressBarForWeb.visibility = View.VISIBLE

                                val historyItem = SharedDataApplication.WebHistoryItem(url, view.title, favicon)
                                this.historyItem = historyItem

                                // FIXME java.lang.ClassCastException: android.app.ContextImpl cannot be cast to jp.kght6123.smallappbrowser.application.SharedDataApplication
//                                val sharedData = this@SmallBrowserApplication.sharedContext as SharedDataApplication
//                                sharedData.addWebHistoryItemList(historyItem)
                            }
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                view?.loadUrl(request?.url?.toString(), additionalHttpHeaders)
                                return true
                            }
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(webView, url)

                                progressBarForWeb.visibility = View.GONE

                                if (this.historyItem != null) {
                                    this.historyItem!!.title = view.title
                                    if (histAdapter != null)
                                        histAdapter!!.insert(this.historyItem, 0)
                                }
                            }
                            override fun onReceivedError(view: WebView, errorCode: Int,
                                                         description: String, failingUrl: String) {
                                progressBarForWeb.visibility = View.GONE

                                if (this.historyItem != null) {
                                    this.historyItem!!.title = view.title
                                }
                            }
                        }
                )
                webView.setWebChromeClient(
                        object : WebChromeClient() {
                            override fun onCreateWindow(view: WebView,
                                                        isDialog: Boolean, isUserGesture: Boolean,
                                                        resultMsg: Message): Boolean {
                                val result = super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                                setFaviconToMinimizedView(null)
                                return result
                            }
                            override fun onCloseWindow(view: WebView) {
                                super.onCloseWindow(view)
                                setFaviconToMinimizedView(view.favicon)
                            }
                            override fun onReceivedIcon(view: WebView, icon: Bitmap) {
                                super.onReceivedIcon(view, icon)
                                setFaviconToMinimizedView(icon)
                            }
                            override fun onProgressChanged(view: WebView, newProgress: Int) {
                                progressBarForWeb.progress = newProgress
                            }
                        }
                )
                webView.setVerticalScrollbarOverlay(true)

                val urlHandler = object : Handler() {
                    override fun handleMessage(msg: Message) {
                        focusUrl = msg.data.getString("url")
                    }
                }
                webView.requestFocusNodeHref(urlHandler.obtainMessage())
                webView.setOnLongClickListener(
                        View.OnLongClickListener { view ->
                            Log.d(TAG, "onLongClick")

                            val webView = view as WebView
                            val hr = webView.hitTestResult
                            Log.d(TAG, "HitTestResult.getType:" + hr.type)
                            Log.d(TAG, "HitTestResult.getExtra:" + hr.extra)

                            if (hr.extra == null)
                                false
                            else if (hr.type == WebView.HitTestResult.IMAGE_TYPE || hr.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
//								val url: String?
//								if (hr.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
//										&& focusUrl != null)
//									url = focusUrl
//								else
//									url = hr.extra
//
//								try {
//									val intent = Intent(getIntent())
//									intent.setClassName("jp.kght6123.smallappimageviewer",
//											"jp.kght6123.smallappimageviewer.smallapp.SmallImageViewApplication")
//									intent.putExtra("android.intent.extra.STREAM", url)
//
//									SmallApplicationManager.startApplication(getApplicationContext(), intent)
//								} catch (e: SmallAppNotFoundException) {
//									return@OnLongClickListener false
//								}
//								true
                                false
                            } else if (hr.type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                intent.data = Uri.parse(hr.extra)
                                startActivity(intent)
                                //getWindow().setWindowState(WindowState.MINIMIZED)
                                true
                            } else
                                false
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

                //ws.setPluginState(PluginState.ON);
                //ws.setPluginsEnabled(true);
                //ws.setSupportMultipleWindows(support);

                // キャッシュを設定
                ws.setAppCacheEnabled(true)
                ws.setAppCachePath(externalCacheDir.path)

                ws.databaseEnabled = true
                ws.domStorageEnabled = true
                ws.loadWithOverviewMode = true
                ws.useWideViewPort = true

                // FIXME java.lang.NumberFormatException: For input string: "2.0dip"
//                setupOptionMenu(webView)
                // FIXME include
//                setupActionBar(webView, R.id.moveControlAreaBottom, R.id.webviewControlAreaBottom2, R.id.smallappControlAreaBottom2, MoveControlArea.Bottom2)
//                setupActionBar(webView, R.id.moveControlAreaBottom, R.id.webviewControlAreaBottom, R.id.smallappControlAreaBottom, MoveControlArea.Bottom)
//                setupActionBar(webView, R.id.moveControlAreaRight, R.id.webviewControlAreaRight, R.id.smallappControlAreaRight, MoveControlArea.Right)
//                setupActionBar(webView, R.id.moveControlAreaLeft, R.id.webviewControlAreaLeft, R.id.smallappControlAreaLeft, MoveControlArea.Left)

                return mainView
            }
            override fun createMinimizedView(arg: Int): View {
                this.mMinimizedView = ImageView(sharedContext)
                this.mMinimizedView!!.setImageResource(R.mipmap.ic_launcher)
                this.mMinimizedView!!.isFocusableInTouchMode = true
                this.mMinimizedView!!.isFocusable = true
                return this.mMinimizedView!!
            }
            override fun start(intent: Intent?) {
                // キャッシュモードをネットワークの接続状況によって切り替え
                val ws = webView.settings
                val info = (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
                if (info != null && info.isConnected)
                    defaultCacheMode = WebSettings.LOAD_DEFAULT// LOAD_CACHE_ONLY
                else
                    defaultCacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                ws.cacheMode = defaultCacheMode

                if (intent?.extras != null)
                    ws.javaScriptEnabled = intent.extras.getBoolean(EXTRA_JAVASCRIPT_DISABLED, false)
                else
                    ws.javaScriptEnabled = true

                if (intent != null)
                    webView.loadUrl(intent.dataString, additionalHttpHeaders)
                else {
                    webView.loadUrl("https://www.google.com/", additionalHttpHeaders)
                }
            }
            override fun update(intent: Intent?, index: Int, positionName: String) {
                if(positionName == MultiWindowUpdatePosition.FIRST.name
                        || positionName == MultiWindowUpdatePosition.INDEX.name) {
                    start(intent)
                }
            }
            private fun setFaviconToMinimizedView(icon: Bitmap?) {
                this.mMinimizedView?.setImageBitmap(icon)
            }
            var savedMoveControlArea: MoveControlArea
                get() {
                    val sPref = getSharedPreferences(SmallBrowserApplication::class.java.name, Context.MODE_PRIVATE)
                    val moveControlAreaStr = sPref.getString(MoveControlArea::class.java.simpleName, MoveControlArea.Left.name)
                    return MoveControlArea.valueOf(moveControlAreaStr)
                }
                private set(moveControlArea) {
                    val sPref = getSharedPreferences(SmallBrowserApplication::class.java.name, Context.MODE_PRIVATE)

                    val editor = sPref.edit()
                    editor.putString(MoveControlArea::class.java.simpleName, moveControlArea.name)
                    editor.apply()
                }
// FIXME include
//            inner class OnClickListenerForMoveControl(private val moveControlArea: MoveControlArea) : View.OnClickListener {
//
//                private val moveControlAreaLeft = mainView.findViewById(R.id.moveControlAreaLeft) as LinearLayout
//                private val moveControlAreaRight = mainView.findViewById(R.id.moveControlAreaRight) as LinearLayout
//                private val moveControlAreaBottom = mainView.findViewById(R.id.moveControlAreaBottom) as LinearLayout
//
//                private val webViewControlAreaLeft = mainView.findViewById(R.id.webviewControlAreaLeft) as LinearLayout
//                private val webViewControlAreaRight = mainView.findViewById(R.id.webviewControlAreaRight) as LinearLayout
//                private val webViewControlAreaBottom = mainView.findViewById(R.id.webviewControlAreaBottom) as LinearLayout
//                private val webViewControlAreaBottom2 = mainView.findViewById(R.id.webviewControlAreaBottom2) as LinearLayout
//
//                private val smallappControlAreaLeft = mainView.findViewById(R.id.smallappControlAreaLeft) as LinearLayout
//                private val smallappControlAreaRight = mainView.findViewById(R.id.smallappControlAreaRight) as LinearLayout
//                private val smallappControlAreaBottom = mainView.findViewById(R.id.smallappControlAreaBottom) as LinearLayout
//                private val smallappControlAreaBottom2 = mainView.findViewById(R.id.smallappControlAreaBottom2) as LinearLayout
//
//                @Synchronized override fun onClick(v: View) {
//                    when (moveControlArea) {
//                        MoveControlArea.Left -> {
//                            moveControlAreaRight.visibility = View.VISIBLE
//                            moveControlAreaBottom.visibility = View.VISIBLE
//
//                            webViewControlAreaRight.visibility = View.INVISIBLE
//                            webViewControlAreaBottom.visibility = View.INVISIBLE
//                            webViewControlAreaBottom2.visibility = View.INVISIBLE
//
//                            smallappControlAreaRight.visibility = View.INVISIBLE
//                            smallappControlAreaBottom.visibility = View.INVISIBLE
//                            smallappControlAreaBottom2.visibility = View.INVISIBLE
//
//                            moveControlAreaLeft.visibility = View.INVISIBLE
//                            webViewControlAreaLeft.visibility = View.VISIBLE
//                            smallappControlAreaLeft.visibility = View.VISIBLE
//                        }
//                        MoveControlArea.Right -> {
//                            moveControlAreaLeft.visibility = View.VISIBLE
//                            moveControlAreaBottom.visibility = View.VISIBLE
//
//                            webViewControlAreaLeft.visibility = View.INVISIBLE
//                            webViewControlAreaBottom.visibility = View.INVISIBLE
//                            webViewControlAreaBottom2.visibility = View.INVISIBLE
//
//                            smallappControlAreaLeft.visibility = View.INVISIBLE
//                            smallappControlAreaBottom.visibility = View.INVISIBLE
//                            smallappControlAreaBottom2.visibility = View.INVISIBLE
//
//                            moveControlAreaRight.visibility = View.INVISIBLE
//                            webViewControlAreaRight.visibility = View.VISIBLE
//                            smallappControlAreaRight.visibility = View.VISIBLE
//                        }
//                        MoveControlArea.Bottom, MoveControlArea.Bottom2 -> {
//                            moveControlAreaLeft.visibility = View.VISIBLE
//                            moveControlAreaRight.visibility = View.VISIBLE
//
//                            webViewControlAreaLeft.visibility = View.INVISIBLE
//                            webViewControlAreaRight.visibility = View.INVISIBLE
//
//                            smallappControlAreaLeft.visibility = View.INVISIBLE
//                            smallappControlAreaRight.visibility = View.INVISIBLE
//
//
//                            val pref = PreferenceManager.getDefaultSharedPreferences(this@SmallBrowserApplication)
//                            if (pref.getBoolean(this@SmallBrowserApplication.getString(R.string.bottom_actionbar_view_close_let_bar_key), false)) {
//                                moveControlAreaBottom.visibility = View.INVISIBLE
//                                webViewControlAreaBottom.visibility = View.INVISIBLE
//                                smallappControlAreaBottom.visibility = View.INVISIBLE
//
//                                webViewControlAreaBottom2.visibility = View.VISIBLE
//                                smallappControlAreaBottom2.visibility = View.VISIBLE
//                            } else {
//                                moveControlAreaBottom.visibility = View.INVISIBLE
//                                webViewControlAreaBottom.visibility = View.VISIBLE
//                                smallappControlAreaBottom.visibility = View.VISIBLE
//
//                                webViewControlAreaBottom2.visibility = View.INVISIBLE
//                                smallappControlAreaBottom2.visibility = View.INVISIBLE
//                            }
//                        }
//                    }
//                    savedMoveControlArea = moveControlArea
//                }
//            }

// FIXME java.lang.NumberFormatException: For input string: "2.0dip"
//            private fun setupOptionMenu(webView: WebView): View {
//                val pref = PreferenceManager.getDefaultSharedPreferences(this@SmallBrowserApplication)
//                val header = createContentView(R.layout.smallapp_browser_header)// リソースからViewを取り出す
//
//                val optionMenu = header.findViewById(R.id.option_menu)
//                optionMenu.setOnClickListener(View.OnClickListener {
//                    val popup = PopupMenu(this@SmallBrowserApplication, optionMenu)
//                    popup.menuInflater.inflate(R.menu.smallapp_browser_menus, popup.menu)
//
//                    val menu = popup.menu
//                    if (webView.settings.javaScriptEnabled)
//                        menu.findItem(R.id.javascript).setTitle(R.string.javascriptDisable)
//                    else
//                        menu.findItem(R.id.javascript).setTitle(R.string.javascriptEnable)
//
//                    popup.setOnMenuItemClickListener { item ->
//                        if (R.id.javascript == item.itemId) {
//                            if (webView.settings.javaScriptEnabled) {
//                                menu.findItem(R.id.javascript).setTitle(R.string.javascriptEnable)
//                                webView.settings.javaScriptEnabled = false
//                                webView.reload()
//                            } else {
//                                menu.findItem(R.id.javascript).setTitle(R.string.javascriptDisable)
//                                webView.settings.javaScriptEnabled = true
//                                webView.reload()
//                            }
//                        }
//                        true
//                    }
//                    popup.show()
//                })
//                val back = header.findViewById(R.id.back)
//                back.setOnClickListener(this.onClickBackListener)
//                PrefUtils.setVisibility(R.string.back_view_title_key, back, false, this@SmallBrowserApplication, pref)
//
//                val forward = header.findViewById(R.id.forward)
//                forward.setOnClickListener(this.onClickForwardListener)
//                PrefUtils.setVisibility(R.string.forward_view_title_key, forward, false, this@SmallBrowserApplication, pref)
//
//                val chrome = header.findViewById(R.id.chrome)
//                chrome.setOnClickListener(this.onClickChromeListener)
//                PrefUtils.setVisibility(R.string.chrome_view_title_key, chrome, false, this@SmallBrowserApplication, pref)
//
//                /**
//                 * タイトルビューの設定
//
//                 * 縦は48dpぐらい。AndoridのActionBarと同じサイズ。
//                 * ActionButtonは48dp×48dpにすること。（UIのガイドラインレベル）
//                 * Action button icons on header area should be 48x48 dp.
//
//                 */
//                /* Deploy the option menu in the header area of the titlebar */
//                return header
//            }

            private fun setupActionBar(webView: WebView, moveControlAreaId: Int, webviewControlAreaId: Int, smallappControlAreaId: Int, targetMoveControlArea: MoveControlArea) {
                val pref = PreferenceManager.getDefaultSharedPreferences(this@SmallBrowserApplication)

                val moveControlArea = mainView.findViewById(moveControlAreaId)
                val webviewControlArea = mainView.findViewById(webviewControlAreaId)
                val smallappControlArea = mainView.findViewById(smallappControlAreaId)

                val back = webviewControlArea.findViewById(R.id.btnBack) as Button
                back.setOnClickListener(this.onClickBackListener)
                PrefUtils.setVisibility(R.string.back_view_bar_key, back, true, this@SmallBrowserApplication, pref)

                val forward = webviewControlArea.findViewById(R.id.btnForward) as Button
                forward.setOnClickListener(this.onClickForwardListener)
                PrefUtils.setVisibility(R.string.forward_view_bar_key, forward, false, this@SmallBrowserApplication, pref)

                val chrome = smallappControlArea.findViewById(R.id.btnChrome) as Button
                chrome.setOnClickListener(this.onClickChromeListener)
                PrefUtils.setVisibility(R.string.chrome_view_bar_key, chrome, true, this@SmallBrowserApplication, pref)

                val pin = webviewControlArea.findViewById(R.id.btnPin) as Button
                pin.setOnClickListener(this.onClickPinListener)
                PrefUtils.setVisibility(R.string.pin_view_bar_key, pin, false, this@SmallBrowserApplication, pref)

                val histry = webviewControlArea.findViewById(R.id.btnHistry) as Button
                histry.setOnClickListener(this.onClickHistListener)
                PrefUtils.setVisibility(R.string.history_view_bar_key, histry, false, this@SmallBrowserApplication, pref)

                val shared = webviewControlArea.findViewById(R.id.btnShared) as Button
                shared.setOnClickListener(this.onClickSharedListener)
                PrefUtils.setVisibility(R.string.shared_view_bar_key, shared, true, this@SmallBrowserApplication, pref)

                val close = smallappControlArea.findViewById(R.id.btnClose) as Button
                close.setOnClickListener { /*this@SmallBrowserApplication.finish()*/ }
                PrefUtils.setVisibility(R.string.close_view_bar_key, close, true, this@SmallBrowserApplication, pref)

                val refresh = webviewControlArea.findViewById(R.id.btnRefresh) as Button
                refresh.setOnClickListener {
                    webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    webView.reload()
                    webView.settings.cacheMode = defaultCacheMode
                }
                PrefUtils.setVisibility(R.string.refresh_view_bar_key, refresh, false, this@SmallBrowserApplication, pref)

                if (targetMoveControlArea != MoveControlArea.Bottom2) {
                    // FIXME include
//                    val btnMoveControl = moveControlArea.findViewById(R.id.btnMoveControl) as Button
//                    val onClickMoveCtrl = OnClickListenerForMoveControl(targetMoveControlArea)
//                    btnMoveControl.setOnClickListener(onClickMoveCtrl)
//
//                    if (targetMoveControlArea == savedMoveControlArea)
//                        onClickMoveCtrl.onClick(btnMoveControl)
                }
            }
        }
    }

    override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
		return object : MultiFloatWindowSettingsFactory(multiWindowContext) {
            override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
				return MultiFloatWindowInitSettings(
                        x = getDimensionPixelSize(R.dimen.x),
                        y = getDimensionPixelSize(R.dimen.y),
                        width = getDimensionPixelSize(R.dimen.width),
                        height = getDimensionPixelSize(R.dimen.height),
                        theme = MultiFloatWindowConstants.Theme.Light
                )
			}
        }
	}
}