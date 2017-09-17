package jp.kght6123.smalllittleappviewer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView

/**
 * Created by kogahirotaka on 2017/09/06.
 */
class MultiFloatWindowDelegateViewTest(val sharedContext: Context, val delegateContext: Context) {

    public fun onCreate(): View {
        val view = View.inflate(sharedContext, R.layout.remote_mini_view, null)//LayoutInflater.from(applicationContext).inflate(R.layout.remote_mini_view, null)
        return view
    }

    public fun onCreateWebView(): View {
        val parser = sharedContext.resources.getLayout(R.layout.small_webview)
        val view = LayoutInflater.from(delegateContext).inflate(parser, null)
        //val view = View.inflate(applicationContext, R.layout.small_webview, null)//LayoutInflater.from(applicationContext).inflate(R.layout.remote_mini_view, null)
        val webView = view.findViewById(R.id.webView) as WebView
        webView.loadUrl("https://www.google.com/")
        return view
    }
}