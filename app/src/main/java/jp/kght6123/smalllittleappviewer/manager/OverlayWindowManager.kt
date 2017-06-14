package jp.kght6123.smalllittleappviewer.manager

import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import jp.kght6123.smalllittleappviewer.custom.view.OverlayWindowLinearLayout

/**
 * Created by kogahirotaka on 2017/06/03.
 */
class OverlayWindowManager(val context: Context) {
	
	val overlayWindowMap: MutableMap<String, OverlayWindowSizeInfo> = LinkedHashMap()
	val windowManager: WindowManager by lazy {
		context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
	}
	
	// ディスプレイのサイズを格納する
	val defaultDisplaySize: Point by lazy {
		val display = windowManager.defaultDisplay
		val size = Point()
		display.getSize(size)
		size
	}
	
	init {}
	
	fun put(name: String, params: WindowManager.LayoutParams, overlayWindow: OverlayWindowLinearLayout, overlayMiniView: ViewGroup, miniMode: Boolean) {
		put(name, params, OverlayWindowSizeInfo(overlayWindow, overlayMiniView, miniMode))
	}
	fun put(name: String, params: WindowManager.LayoutParams, overlayInfo: OverlayWindowSizeInfo) {
		if(overlayWindowMap[name] != null)
			return
		
		overlayWindowMap.put(name, overlayInfo)  // 管理に追加
		windowManager.addView(overlayInfo.getActiveOverlay(), params)   // WindowManagerに追加
		
		updateActive(name)  // 追加したWindowをActiveに
		updateDeActive(name)  // 追加したWindow以外をDeactiveに
	}
	
	fun update(name: String, params: WindowManager.LayoutParams) {
		val overlayInfo = overlayWindowMap[name]
		if(overlayInfo != null)
			windowManager.updateViewLayout(overlayInfo.getActiveOverlay(), params)
	}
	
	fun remove(name: String) {
		val overlayInfo = overlayWindowMap[name]
		if(overlayInfo != null){
			overlayWindowMap.remove(name)
			windowManager.removeViewImmediate(overlayInfo.getActiveOverlay())
		}
		updateDeActive(name)
	}
	
	fun switchWindowSize(name: String, params: WindowManager.LayoutParams, miniMode: Boolean) {
		val overlayInfo = overlayWindowMap[name]
		if(overlayInfo != null){
			remove(name)
			overlayInfo.miniMode = miniMode
			put(name, params, overlayInfo)
		}
	}
	
	fun changeActive(name: String, params: WindowManager.LayoutParams) {
		val overlayInfo = overlayWindowMap[name]
		if(overlayInfo != null) {
			remove(name)
			put(name, params, overlayInfo)
		}
		
		//val dummy = LinearLayout(context)
		
		
		// 一番上に表示するViewをダミーViewに入れ替え（元View①は保持）
		// 一番上のViewを①に入れ替え、元View②は保持
		// ダミーViewを②に入れ替え
		// → 指定したname同士を入れ替える関数Aを用意して処理させる
		
		// 上記を②のViewが２番目に来る様に繰り返したい
		// → 関数Aを下から繰り返し実行して入れ替えする
		
//		overlayWindowMap.forEach { overlayName, overlayInfo ->
//			if(name == overlayName) {
//
//			} else if(overlayInfo.overlayWindow.activeFlag) {
//
//			} else {
//
//			}
//		}
	}
	
	private fun updateActive(name: String) {
		val overlayInfo = overlayWindowMap[name]
		overlayInfo?.overlayWindow?.activeFlag = true
		overlayInfo?.overlayWindow?.onActive()
	}
	private fun updateDeActive(name: String) {
		overlayWindowMap.forEach { overlayName, overlayInfo ->
			if(overlayName != name) {
				overlayInfo.overlayWindow.activeFlag = false
				overlayInfo.overlayWindow.onDeActive()
			}
		}
	}
	
	class OverlayWindowSizeInfo(val overlayWindow: OverlayWindowLinearLayout, val overlayMiniView: ViewGroup, var miniMode: Boolean) {
		init {}
		fun getActiveOverlay(): ViewGroup {
			if(this.miniMode)
				return this.overlayMiniView
			else
				return this.overlayWindow
		}
	}

	fun changeOtherActive(name: String, event: MotionEvent) {
		for ((overlayName, overlayInfo) in overlayWindowMap) {
			if (overlayName != name && overlayInfo.overlayWindow.isOnTouchEvent(event)) {
				overlayInfo.overlayWindow.changeActive()
				return@changeOtherActive
			}
		}
	}
}