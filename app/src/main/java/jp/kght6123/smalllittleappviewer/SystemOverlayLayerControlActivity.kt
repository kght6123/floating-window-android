package jp.kght6123.smalllittleappviewer

import android.app.Activity
import android.app.Service
import android.content.AsyncQueryHandler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger

class SystemOverlayLayerControlActivity : Activity() {
	
	private val TAG = this.javaClass.simpleName
	private val receiver: BroadcastReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			Log.d(TAG, "BroadcastReceiver.onReceive")
		}
	}
	
	// サービスからのMessageを受け取るMessangerを初期化
	private val mSelfMessenger: Messenger by lazy {
		Messenger(object: Handler() {
			override fun handleMessage(msg: Message?) {
				Log.e(TAG, "handle response=" + msg)
				// objでobtainの第三引数で受け渡された値が取得可能
				
			}
		})
	}
	
	// サービスにMessageを送信するMessanger
	var mServiceMessenger: Messenger? = null
	
	// サービスに接続・切断した時の処理を記載
	private val mConnection: ServiceConnection = object: ServiceConnection {
		override fun onServiceDisconnected(name: ComponentName?) {
			TODO("not implemented")
		}
		
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			mServiceMessenger = Messenger(service)// サービスにMessageを送信するMessangerを初期化
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_system_overlay_layer_control)
		
		// サービスに接続する（サービスは自動生成）
		bindService(Intent(this, SystemOverlayLayerService::class.java),
				mConnection, Service.BIND_AUTO_CREATE)
		
		val iFilter: IntentFilter = IntentFilter()
		iFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
		this.registerReceiver(receiver, iFilter)
	}
	
	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		when (keyCode) {
			KeyEvent.KEYCODE_BACK -> {
				Log.d(TAG, "onKeyDown KeyEvent.KEYCODE_BACK")
				
				val msg: Message = Message.obtain(null, 1)
				// 第二引数のIntがwhat?（どの処理か判別するフラグ）になる
				// 第三引数のObjectが受けわたす値になる
				//   他にもBundleのsetter / getter（msg.setData(Bundle)）があるので、それを使っても良い、
				//     BundleのParcelableを実装すれば他アプリにも
				
				msg.replyTo = mSelfMessenger// 返信はこのMessangerによろしく？
				mServiceMessenger?.send(msg)// サービスにメッセージを送信
				
				
				// オーバーレイウィンドウの最小化、もしくは停止処理予定
				return false
			}
			KeyEvent.KEYCODE_APP_SWITCH -> {
				Log.d(TAG, "onKeyDown KeyEvent.KEYCODE_APP_SWITCH")
				return false
			}
			KeyEvent.KEYCODE_HOME -> {
				Log.d(TAG, "onKeyDown KeyEvent.KEYCODE_HOME")
				return false
			}
		}
		return super.onKeyDown(keyCode, event)
	}
	
	override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
		super.onCreate(savedInstanceState, persistentState)
	}
	
	override fun onUserLeaveHint() {
		super.onUserLeaveHint()
		Log.d(TAG, "onUserLeaveHint")
		
		Toast.makeText(applicationContext, "onUserLeaveHint, Good bye!" , Toast.LENGTH_SHORT).show()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		this.unregisterReceiver(receiver)
	}
}
