package jp.kght6123.smalllittleappviewer

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * オーバーレイウィンドウサービスの起動／停止を行う、テスト用Activity
 *
 * Created by kght6123 on 2017/05/09.
 */
class SystemOverlayLayerActivity : Activity() {
	val REQUEST_SYSTEM_OVERLAY :Int = 1234
	
	val overlay_start_button: Button by lazy {
		findViewById(R.id.overlay_start_button) as Button
	}
	val overlay_stop_button: Button by lazy {
		findViewById(R.id.overlay_stop_button) as Button
	}

	val overlay_start_button2: Button by lazy {
		findViewById(R.id.overlay_start_button2) as Button
	}
	val overlay_stop_button2: Button by lazy {
		findViewById(R.id.overlay_stop_button2) as Button
	}

	private fun init() {
		if (checkOverlayPermission()) {
			this.setContentView(R.layout.activity_system_overlay_layer)
			
			overlay_start_button.setOnClickListener({
				startService(Intent(this@SystemOverlayLayerActivity, SystemOverlayLayerService::class.java))
			})
			overlay_stop_button.setOnClickListener({
				stopService(Intent(this@SystemOverlayLayerActivity, SystemOverlayLayerService::class.java))
			})

			overlay_start_button2.setOnClickListener({
				startService(Intent(this@SystemOverlayLayerActivity, MultiFloatWindowTestService::class.java))
			})
			overlay_stop_button2.setOnClickListener({
				stopService(Intent(this@SystemOverlayLayerActivity, MultiFloatWindowTestService::class.java))
			})

		} else {
			requestOverlayPermission()
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		init()
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		when (requestCode) {
			REQUEST_SYSTEM_OVERLAY -> if (checkOverlayPermission()) {
				// もう一度権限を確認して、権限があれば処理をする
				init()
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
		val intent =
				Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")) // packageNameは、getPackageName()を呼び出している
		this.startActivityForResult(intent,  REQUEST_SYSTEM_OVERLAY)
	}
}
