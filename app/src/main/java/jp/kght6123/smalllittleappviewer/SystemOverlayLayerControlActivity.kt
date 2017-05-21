package jp.kght6123.smalllittleappviewer

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import android.content.Intent
import android.content.IntentFilter



class SystemOverlayLayerControlActivity : Activity() {
	
	private val TAG = this.javaClass.simpleName
	private val receiver: BroadcastReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			Log.d(TAG, "BroadcastReceiver.onReceive")
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_system_overlay_layer_control)
		
		val iFilter: IntentFilter = IntentFilter()
		iFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
		this.registerReceiver(receiver, iFilter)
	}
	
	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		when (keyCode) {
			KeyEvent.KEYCODE_BACK -> {
				Log.d(TAG, "onKeyDown KeyEvent.KEYCODE_BACK")
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
