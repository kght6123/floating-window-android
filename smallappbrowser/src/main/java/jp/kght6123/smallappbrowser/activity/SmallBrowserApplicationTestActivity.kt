package jp.kght6123.smallappbrowser.activity

import android.content.Context
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import jp.kght6123.multiwindow.MultiFloatWindowApplication
import jp.kght6123.multiwindow.MultiFloatWindowApplicationActivity
import jp.kght6123.smallappbrowser.R
import jp.kght6123.smallappbrowser.SmallBrowserApplicationService

/**
 * スモールブラウザの起動／停止を行う、テスト用Activity
 *
 * Created by kght6123 on 2017/05/09.
 */
class SmallBrowserApplicationTestActivity: MultiFloatWindowApplicationActivity<SmallBrowserApplicationService>(SmallBrowserApplicationService::class.java) {

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
                openMultiFloatWindowView(++index, MultiFloatWindowApplication.MultiWindowOpenType.NEW)

                val intent = Intent()
                intent.data = Uri.parse("http://google.co.jp/")

                startMultiFloatWindowView(index, intent)
            })
            multi_window_update_button.setOnClickListener({
                openMultiFloatWindowView(index, MultiFloatWindowApplication.MultiWindowOpenType.UPDATE)

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

            val sharedContext = this.createPackageContext("jp.kght6123.smalllittleappviewer", Context.CONTEXT_INCLUDE_CODE)
            val classObj = Class.forName("jp.kght6123.smalllittleappviewer.MultiFloatWindowDelegateViewTest", true, sharedContext.classLoader)

            Toast.makeText(applicationContext, "className=${classObj.name}.", Toast.LENGTH_SHORT).show()

            val delegate = classObj.getConstructor(Context::class.java).newInstance(sharedContext)

            Toast.makeText(applicationContext, "delegate=$delegate.", Toast.LENGTH_SHORT).show()

            val view = classObj.getMethod("onCreate").invoke(delegate) as View

            Toast.makeText(applicationContext, "view=$view.", Toast.LENGTH_SHORT).show()

            val buttonGroup = findViewById(R.id.buttonGroup) as LinearLayout
            buttonGroup.addView(view)

//            val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            val otherAppContext = createPackageContext(
//                    "jp.kght6123.smalllittleappviewer",
//                    Context.CONTEXT_INCLUDE_CODE/* or Context.CONTEXT_IGNORE_SECURITY*/)
//
//            val mMainThreadField = otherAppContext.javaClass.getDeclaredField("mMainThread")
//            mMainThreadField.isAccessible = true
//
//            val mMainThread = mMainThreadField.get(otherAppContext)
//
//            val mInitialApplicationField = mMainThread.javaClass.getDeclaredField("mInitialApplication")
//            mInitialApplicationField.isAccessible = true
//
//            val mInitialApplication = mInitialApplicationField.get(mMainThread)
//
//            val otherApplication = mInitialApplication as Application
//
//            Toast.makeText(applicationContext, "className=${otherApplication.applicationInfo.className}.", Toast.LENGTH_SHORT).show()
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
