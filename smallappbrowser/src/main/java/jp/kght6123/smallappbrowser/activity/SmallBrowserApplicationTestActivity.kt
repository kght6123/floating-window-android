package jp.kght6123.smallappbrowser.activity

import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.os.*
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

    //private val TAG = SmallBrowserApplicationTestActivity::class.java.simpleName
    private val multi_window_start_button: Button by lazy {
		findViewById(R.id.multi_window_start_button) as Button
	}
    private val multi_window_open_button: Button by lazy {
		findViewById(R.id.multi_window_open_button) as Button
	}
    private val multi_window_close_button: Button by lazy {
		findViewById(R.id.multi_window_close_button) as Button
	}
    private val multi_window_exit_button: Button by lazy {
		findViewById(R.id.multi_window_exit_button) as Button
	}

	private var index: Int = 0

    override fun onCheckOverlayPermissionResult(result: Boolean) {
        if(result) {
            this.setContentView(R.layout.activity_smallapp_browser_test)

            multi_window_start_button.setOnClickListener({
                startMultiFloatWindowService()
            })
            multi_window_open_button.setOnClickListener({
                // FIXME 新しいウィンドウを常に開くか、既存のウィンドウがあればを使うかなど、フラグの方が良いかも。対象のウィンドウのIndexを返す。
                //openMultiFloatWindowView(++index, MultiFloatWindowApplication.MultiWindowOpenType.NEW)
                openMultiFloatWindowView(index, MultiFloatWindowApplication.MultiWindowOpenType.UPDATE)

                val intent = Intent()
                intent.data = Uri.parse("http://google.co.jp/")

                startMultiFloatWindowView(index, intent)
            })
            multi_window_close_button.setOnClickListener({
                //closeMultiFloatWindowView(index--, Intent())
                closeMultiFloatWindowView(index, Intent())
            })
            multi_window_exit_button.setOnClickListener({
                stopMultiFloatWindowService()
            })
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
