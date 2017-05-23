package jp.kght6123.smalllittleappviewer

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log

class SystemOverlayLayerControlService : Service() {
	
	private val TAG = this.javaClass.simpleName
	
	enum class MessengeWhat {
		NORTH, SOUTH, WEST, EAST
	}
	
	val mServiceMessenger: Messenger by lazy {
		Messenger(object: Handler() {
			override fun handleMessage(msg: Message?) {
				Log.e(TAG, "handle request=" + msg)
				// objでobtainの第三引数で受け渡された値が取得可能
				
				try {
					// Activityから指定されたMessangerへ返信する
					msg?.replyTo?.send(Message.obtain(null, 1))// Activityへの送信はreplyToのMessangerを使用
					// 第二引数のIntがwhat?（どの処理か判別するフラグ）になる
					// 第三引数のObjectが受けわたす値になる
					//   他にもBundleのsetter / getter（msg.setData(Bundle)）があるので、それを使っても良い、
					//     BundleのParcelableを実装すれば他アプリにも
				} catch (e: RemoteException) {
					e.printStackTrace()
				}
			}
		})
	}
	
	// オーバーレイのコントロールActivity
	val intent: Intent by lazy {
		val it = Intent(this, SystemOverlayLayerControlActivity::class.java)
		it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
				Intent.FLAG_ACTIVITY_NEW_TASK or
				Intent.FLAG_ACTIVITY_CLEAR_TASK or
				Intent.FLAG_ACTIVITY_NO_HISTORY or
				Intent.FLAG_ACTIVITY_NO_ANIMATION or
				Intent.FLAG_ACTIVITY_NO_USER_ACTION
		it
	}
	
	override fun onBind(intent: Intent): IBinder? {
		return mServiceMessenger.binder// ActivityのServiceConnection#onServiceConnectedに渡されるIBinderを返す（Messangerでラップして簡易化）
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.d(TAG, "onStartCommand")
		startActivity(intent)
		
		return START_STICKY
	}
}
