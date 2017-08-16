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

	val multi_window_start_button: Button by lazy {
		findViewById(R.id.multi_window_start_button) as Button
	}
	val multi_window_open_button: Button by lazy {
		findViewById(R.id.multi_window_open_button) as Button
	}
	val multi_window_close_button: Button by lazy {
		findViewById(R.id.multi_window_close_button) as Button
	}
	val multi_window_exit_button: Button by lazy {
		findViewById(R.id.multi_window_exit_button) as Button
	}
	var index: Int = 1

	private fun init() {
		if (checkOverlayPermission()) {
			this.setContentView(R.layout.activity_smallapp_browser_test)

			multi_window_start_button.setOnClickListener({
				startService(Intent(this@SmallBrowserApplicationTestActivity, SmallBrowserApplicationService::class.java))
				sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.HELLO, 0)
			})
			multi_window_open_button.setOnClickListener({
				sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.OPEN, index++)
			})
			multi_window_close_button.setOnClickListener({
				sendMessage(MultiFloatWindowApplication.MultiWindowControlCommand.CLOSE, --index)
			})
			multi_window_exit_button.setOnClickListener({
				stopService(Intent(this@SmallBrowserApplicationTestActivity, SmallBrowserApplicationService::class.java))
			})
			bindService(Intent(this, SmallBrowserApplicationService::class.java), mConnection,
					Context.BIND_AUTO_CREATE)
		} else {
			requestOverlayPermission()
		}
	}
	var mService :Messenger? = null
	val mConnection = object: ServiceConnection {
		override fun onServiceDisconnected(name: ComponentName?) {
			mService = null
		}
		override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
			if(binder != null) {
				mService = Messenger(binder)
			}
		}
	}
	fun sendMessage(command: MultiFloatWindowApplication.MultiWindowControlCommand, index: Int) {
		mService?.send(Message.obtain(null, command.ordinal, index, 0))
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		init()
	}
	override fun onStart() {
		super.onStart()
		//bindService(Intent(this, SmallBrowserApplicationService::class.java), mConnection,
		//		Context.BIND_AUTO_CREATE)
	}
	override fun onStop() {
		super.onStop()

		if(mService != null)
			unbindService(mConnection)
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		when (resultCode) {
			RESULT_OK -> {
				when (requestCode) {
					REQUEST_CODE_SYSTEM_OVERLAY ->
						if (checkOverlayPermission()) {
						// もう一度権限を確認して、権限があれば処理をする
						init()
					}
				}
			}
			RESULT_CANCELED -> {
				when (requestCode) {
					REQUEST_CODE_SYSTEM_OVERLAY ->
						if (checkOverlayPermission()) {
						// もう一度権限を確認して、権限があれば処理をする
						init()
					}
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
