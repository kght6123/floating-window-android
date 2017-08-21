package jp.kght6123.smallappbrowser.activity

import android.app.Activity
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Toast
import jp.kght6123.multiwindow.MultiFloatWindowApplication
import jp.kght6123.smallappbrowser.R
import jp.kght6123.smallappbrowser.SmallBrowserApplicationService

/**
 * スモールブラウザの起動／停止を行う、テスト用Activity
 *
 * Created by kght6123 on 2017/05/09.
 */
class SmallBrowserApplicationTestActivity : Activity() {

	companion object {
		private val REQUEST_CODE_SYSTEM_OVERLAY :Int = 1234
	}
    private val TAG = SmallBrowserApplicationTestActivity::class.java.simpleName
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

	private fun init() {
		if (checkOverlayPermission()) {
            Toast.makeText(applicationContext, "OverlayPermission OK.", Toast.LENGTH_SHORT).show()

			this.setContentView(R.layout.activity_smallapp_browser_test)

            startService(Intent(this@SmallBrowserApplicationTestActivity, SmallBrowserApplicationService::class.java))
            bindService(Intent(this, SmallBrowserApplicationService::class.java), mConnection,
                    Context.BIND_AUTO_CREATE)

			multi_window_start_button.setOnClickListener({
				Log.i(TAG, "Hello")
				sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.HELLO, 0)
			})
			multi_window_open_button.setOnClickListener({
                Log.i(TAG, "Open")
				sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.OPEN, ++index)

				// FIXME WINDOW_INDEX渡しは不要、args1として渡せばOK、Intentを渡せるか検証のため
				val intent = Intent()
                intent.putExtra(MultiFloatWindowApplication.MultiWindowControlParam.WINDOW_INDEX.name, index)
                intent.data = Uri.parse("http://google.co.jp/")
                sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.START, index, intent)
            })
			multi_window_close_button.setOnClickListener({
                Log.i(TAG, "Close")
				sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.CLOSE, index--)
			})
			multi_window_exit_button.setOnClickListener({
                Log.i(TAG, "Exit")
				stopService(Intent(this@SmallBrowserApplicationTestActivity, SmallBrowserApplicationService::class.java))
			})
		} else {
            Toast.makeText(applicationContext, "OverlayPermission NG.", Toast.LENGTH_SHORT).show()
            requestOverlayPermission()
		}
	}
	private var mService :Messenger? = null
	private val mConnection = object: ServiceConnection {
		override fun onServiceDisconnected(name: ComponentName?) {
			mService = null
		}
		override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
			if(binder != null) {
				mService = Messenger(binder)
			}
		}
	}
	private fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int) {
        mService?.send(Message.obtain(null, command.ordinal, index, 0))
    }
    private fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int, obj: Any) {
        mService?.send(Message.obtain(null, command.ordinal, index, 0, obj))
    }
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		init()
	}
	override fun onStart() {
		super.onStart()
	}
	override fun onStop() {
		super.onStop()
	}
    override fun onDestroy() {
        super.onDestroy()

        if(mService != null) {
            unbindService(mConnection)
			stopService(Intent(this@SmallBrowserApplicationTestActivity, SmallBrowserApplicationService::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		when (resultCode) {
			RESULT_OK -> {
                Toast.makeText(applicationContext, "RESULT_OK.", Toast.LENGTH_SHORT).show()
                when (requestCode) {
					REQUEST_CODE_SYSTEM_OVERLAY ->
						// もう一度権限を確認して、権限があれば処理をする
						init()
				}
			}
			RESULT_CANCELED -> {
                Toast.makeText(applicationContext, "RESULT_CANCELED.", Toast.LENGTH_SHORT).show()
                when (requestCode) {
					REQUEST_CODE_SYSTEM_OVERLAY ->
						// もう一度権限を確認して、権限があれば処理をする
						init()
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}
	private fun checkOverlayPermission(): Boolean {
		return Settings.canDrawOverlays(this)
	}
	private fun requestOverlayPermission() {
		val intent =
				Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")) // packageNameは、getPackageName()を呼び出している
		this.startActivityForResult(intent,  REQUEST_CODE_SYSTEM_OVERLAY)
	}
}
