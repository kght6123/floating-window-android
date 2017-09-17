package jp.kght6123.smalllittleappviewer

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import jp.kght6123.multiwindowframework.MultiFloatWindowApplication
import jp.kght6123.multiwindowframework.MultiFloatWindowConstants
import jp.kght6123.multiwindowframework.utils.UnitUtils

/**
 * Created by kght6123 on 2017/09/16.
 */
class SmallBrowserTestOtherApplication : MultiFloatWindowApplication() {
    override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
        return object : MultiFloatWindowViewFactory() {

            override fun createWindowView(arg: Int): View {
                val view = createContentView(R.layout.small_webview)
                val webView = view.findViewById(R.id.webView) as WebView
                webView.loadUrl("https://www.google.com/")
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
//                if (intent != null)
//                    webView.loadUrl(intent.getDataString())
//                else {
//                    webView.loadUrl("https://www.google.com/")
//                }
            }
        }
    }

    override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
        return object :  MultiFloatWindowSettingsFactory {
            override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
                // FIXME ココの設定値はうまく渡せていない
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