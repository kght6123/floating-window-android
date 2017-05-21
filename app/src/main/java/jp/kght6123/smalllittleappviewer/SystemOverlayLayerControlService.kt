package jp.kght6123.smalllittleappviewer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SystemOverlayLayerControlService : Service() {
	
	private val TAG = this.javaClass.simpleName
	
	override fun onBind(intent: Intent): IBinder? {
		// TODO: Return the communication channel to the service.
		throw UnsupportedOperationException("Not yet implemented")
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.d(TAG, "onStartCommand")
		val it: Intent = Intent(this, SystemOverlayLayerControlActivity::class.java)
		it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
				Intent.FLAG_ACTIVITY_NEW_TASK or
				Intent.FLAG_ACTIVITY_CLEAR_TASK or
				Intent.FLAG_ACTIVITY_NO_HISTORY or
				Intent.FLAG_ACTIVITY_NO_ANIMATION or
				Intent.FLAG_ACTIVITY_NO_USER_ACTION
		
		startActivity(it)
		
		return START_STICKY
	}
}
