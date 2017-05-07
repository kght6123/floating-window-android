package jp.kght6123.smalllittleappviewer

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Button
import android.view.LayoutInflater
import android.graphics.PixelFormat
import android.view.WindowManager





open class MainActivity : Activity() {
	var REQUEST_SYSTEM_OVERLAY = 1234
	/**
	 * Called when the activity is first created to set up all of the features.
	 */
	//@Override
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (checkOverlayPermission()) {
			// 権限があるときに処理する。

			// If the user is in the PopupMainActivity function, the setUpWindow function would be called from that class
			// otherwise it would call the function from this class that has no implementation.
			setUpWindow()
			// Make sure to set your content view AFTER you have set up the window or it will crash.
			setContentView(R.layout.main)

			// Again, this will call either the function from this class or the PopupMainActivity one,
			// depending on where the user is
			setUpButton()

		} else {
		// 権限がないときに権限管理の設定画面を呼び出す
			requestOverlayPermission()
		}
	}

	protected open fun setUpWindow() {
// Nothing here because we don't need to set up anything extra for the full app.
	}

	protected open fun setUpButton() {
// Creates the button and defines it's behavior for the full app.
		val switchMode = findViewById(R.id.switch_modes) as Button
		switchMode.text = resources.getString(R.string.switch_to_window)
		switchMode.setOnClickListener(/*object : View.OnClickListener {
			//@Override
			override fun onClick(view: View) */{
				finish()

				val window = Intent(this@MainActivity, PopupMainActivity::class.java)
				window.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(window)
			}
		/*}*/)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		when (requestCode) {
			REQUEST_SYSTEM_OVERLAY -> if (checkOverlayPermission()) {
				// もう一度権限を確認して、権限があれば処理をする
				setUpWindow()
				setContentView(R.layout.main)
				setUpButton()
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	private fun Context.checkOverlayPermission(): Boolean {
		if (Build.VERSION.SDK_INT < 23) {
			return true
		}
		return Settings.canDrawOverlays(this)
	}

	private fun Activity.requestOverlayPermission() {
		val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")) // packageNameは、getPackageName()を呼び出している
		this.startActivityForResult(intent,  REQUEST_SYSTEM_OVERLAY)
	}
}