package jp.kght6123.smallappbrowser.activity

import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.os.*
import jp.kght6123.multiwindow.MultiFloatWindowApplicationActivity
import jp.kght6123.multiwindowframework.MultiWindowOpenType
import jp.kght6123.smallappbrowser.R

/**
 * スモールブラウザの起動／停止を行う、テスト用Activity
 *
 * Created by kght6123 on 2017/05/09.
 */
class SmallBrowserApplicationTestActivity: MultiFloatWindowApplicationActivity() {

    private val multi_window_start_button: Button by lazy {
		findViewById(R.id.multi_window_start_button) as Button
	}
    private val multi_window_open_button: Button by lazy {
		findViewById(R.id.multi_window_open_button) as Button
	}
    private val multi_window_update_button: Button by lazy {
        findViewById(R.id.multi_window_update_button) as Button
    }
    private val multi_window_close_button: Button by lazy {
		findViewById(R.id.multi_window_close_button) as Button
	}
    private val multi_window_exit_button: Button by lazy {
		findViewById(R.id.multi_window_exit_button) as Button
	}
    private val multi_window_widget_button: Button by lazy {
        findViewById(R.id.multi_window_widget_button) as Button
    }

	private var index: Int = 0

    override fun onCheckOverlayPermissionResult(result: Boolean) {
        if(result) {
            this.setContentView(R.layout.activity_smallapp_browser_test)

            multi_window_start_button.setOnClickListener({
                startMultiFloatWindowService()
            })
            multi_window_open_button.setOnClickListener({
                openMultiFloatWindowView(++index, MultiWindowOpenType.NEW)

                val intent = Intent()
                intent.data = Uri.parse("http://google.co.jp/")

                startMultiFloatWindowView(index, intent)
            })
            multi_window_update_button.setOnClickListener({
                openMultiFloatWindowView(index, MultiWindowOpenType.UPDATE)

                val intent = Intent()
                intent.data = Uri.parse("http://google.co.jp/")

                startMultiFloatWindowView(index, intent)
            })
            multi_window_close_button.setOnClickListener({
                closeMultiFloatWindowView(index--, Intent())
                //closeMultiFloatWindowView(index, Intent())
            })
            multi_window_exit_button.setOnClickListener({
                stopMultiFloatWindowService()
            })
            multi_window_widget_button.setOnClickListener({
                startAppWidgetWindowView(++index)
            })

//            val packageInfoList =
//                    this.packageManager.getPackagesHoldingPermissions(arrayOf("jp.kght6123.multiwindow.permission.APPS"), PackageManager.GET_SERVICES)
//
//            for (packageInfo in packageInfoList) {
//                for (serviceInfo in packageInfo.services) {
//                    Toast.makeText(applicationContext, "$serviceInfo.name.", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            val sharedContext = this.createPackageContext("jp.kght6123.smalllittleappviewer", Context.CONTEXT_INCLUDE_CODE)
//            val classObj = Class.forName("jp.kght6123.smalllittleappviewer.MultiFloatWindowDelegateViewTest", true, sharedContext.classLoader)
//
//            Toast.makeText(applicationContext, "className=${classObj.name}.", Toast.LENGTH_SHORT).show()
//
//            val delegate = classObj.getConstructor(Context::class.java, Context::class.java).newInstance(sharedContext, applicationContext)
//
//            Toast.makeText(applicationContext, "delegate=$delegate.", Toast.LENGTH_SHORT).show()
//
//            val view = classObj.getMethod("onCreate").invoke(delegate) as View
//            val webView = classObj.getMethod("onCreateWebView").invoke(delegate) as View
//
//            Toast.makeText(applicationContext, "view=$view,webView=$webView.", Toast.LENGTH_SHORT).show()
//
//            val buttonGroup = findViewById(R.id.buttonGroup) as LinearLayout
//            buttonGroup.addView(view)
//            buttonGroup.addView(webView)
        }
    }
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}
	override fun onStart() {
		super.onStart()
	}
	override fun onStop() {
		super.onStop()
	}
    override fun onDestroy() {
        super.onDestroy()
    }
}
