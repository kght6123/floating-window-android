package jp.kght6123.smalllittleappviewer

import android.app.Activity
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.*
import android.widget.RemoteViews

/**
 * オーバーレイウィンドウサービスの起動／停止を行う、テスト用Activity
 *
 * Created by kght6123 on 2017/05/09.
 */
class MultiFloatWindowTestActivity : Activity() {

	companion object {
		val APP_WIDGET_HOST_ID :Int = 6123

		private val REQUEST_CODE_SYSTEM_OVERLAY :Int = 1234
		private val REQUEST_CODE_PICK_APPWIDGET :Int = 5678
		private val REQUEST_CODE_CONFIGURE_APPWIDGET :Int = 9012
	}

	val appWidgetHost by lazy {
		AppWidgetHost(this, APP_WIDGET_HOST_ID)
	}
	var appWidgetId :Int = -1

	val remote_start_button: Button by lazy {
		findViewById(R.id.remote_start_button) as Button
	}
    val remote_add_button: Button by lazy {
        findViewById(R.id.remote_add_button) as Button
    }
	val remote_stop_button: Button by lazy {
		findViewById(R.id.remote_stop_button) as Button
	}

	val widget_start_button: Button by lazy {
		findViewById(R.id.widget_start_button) as Button
	}
	val widget_stop_button: Button by lazy {
		findViewById(R.id.widget_stop_button) as Button
	}

    private val serviceIntent: Intent by lazy {
        val intent = Intent("jp.kght6123.smallappbrowser.SmallBrowserApplicationService")
		intent.`package` = "jp.kght6123.smallappbrowser"
		intent
    }
    private var mService : Messenger? = null
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

	private fun init() {
		if (checkOverlayPermission()) {
			this.setContentView(R.layout.activity_system_overlay_layer)

            remote_start_button.setOnClickListener({
                //startService(serviceIntent)
                bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
			})
            remote_add_button.setOnClickListener({
                val msg = Message.obtain(null, 3/*ADD_REMOTE_VIEWS*/, 10, 0/*UPDATE*/)
                msg.data = Bundle()
                msg.data.putParcelable("REMOTE_WINDOW_VIEWS", RemoteViews(packageName, R.layout.remote_window_view))
                msg.data.putParcelable("REMOTE_MINI_VIEWS", RemoteViews(packageName, R.layout.remote_mini_view))
                mService?.send(msg)
            })
            remote_stop_button.setOnClickListener({
                unbindService(mConnection)
                //stopService(serviceIntent)
			})
			widget_start_button.setOnClickListener({

				// ウィジェット毎ユニークIDを取得
				appWidgetId = appWidgetHost.allocateAppWidgetId()

				val appWidgetProviderInfoList = ArrayList<AppWidgetProviderInfo>()
				val bundleList = ArrayList<Bundle>()
				val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
						.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
						.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, appWidgetProviderInfoList)
						.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, bundleList)

				// ウィジェット一覧表示
				startActivityForResult(intent, REQUEST_CODE_PICK_APPWIDGET)
			})
			widget_stop_button.setOnClickListener({
				stopService(Intent(this@MultiFloatWindowTestActivity, MultiFloatWidgetTestService::class.java))
			})

		} else {
			requestOverlayPermission()
		}
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

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

		when (resultCode) {
			RESULT_OK -> {
				when (requestCode) {
					REQUEST_CODE_SYSTEM_OVERLAY ->
						if (checkOverlayPermission()) {
						// もう一度権限を確認して、権限があれば処理をする
						init()
					}
					REQUEST_CODE_PICK_APPWIDGET -> if(data != null || appWidgetId != -1) {
						//
						val appWidgetId: Int =
								data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: appWidgetId
						val appWidgetProviderInfo =
								AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)

						if (appWidgetProviderInfo.configure != null) {
							val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
									.setComponent(appWidgetProviderInfo.configure)
									.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
							startActivityForResult(intent, REQUEST_CODE_CONFIGURE_APPWIDGET)
						} else {
							onActivityResult(REQUEST_CODE_CONFIGURE_APPWIDGET, Activity.RESULT_OK, data)
						}
					}
					REQUEST_CODE_CONFIGURE_APPWIDGET -> if(data != null || appWidgetId != -1) {
						//
						val intent = Intent(this@MultiFloatWindowTestActivity, MultiFloatWidgetTestService::class.java)
						intent.replaceExtras(data)
						startService(intent)
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
					REQUEST_CODE_PICK_APPWIDGET, REQUEST_CODE_CONFIGURE_APPWIDGET -> {
						if (data != null) {
							val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
							if (appWidgetId != -1)
								// appWidgetIdを削除
								appWidgetHost.deleteAppWidgetId(appWidgetId)
						}
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}
	private fun checkOverlayPermission(): Boolean {
//		if (Build.VERSION.SDK_INT < 23) {
//			return true
//		}
		return Settings.canDrawOverlays(this)
	}
	private fun requestOverlayPermission() {
		val intent =
				Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")) // packageNameは、getPackageName()を呼び出している
		this.startActivityForResult(intent,  REQUEST_CODE_SYSTEM_OVERLAY)
	}
}
