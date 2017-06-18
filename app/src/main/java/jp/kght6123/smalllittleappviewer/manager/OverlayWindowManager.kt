package jp.kght6123.smalllittleappviewer.manager

import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import jp.kght6123.smalllittleappviewer.custom.view.OverlayWindowLinearLayout

/**
 * オーバーレイ表示ウィンドウの破棄／追加／切替等の管理クラス
 *
 * Created by kght6123 on 2017/06/03.
 */
class OverlayWindowManager(context: Context) {
	
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
		updateDeActive(name)  // 追加したWindow以外をDeActiveに
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
			if (overlayName != name && !overlayInfo.miniMode && overlayInfo.overlayWindow.isOnTouchEvent(event)) {
				this.changeActive(overlayName, overlayInfo.overlayWindow.getActiveParams())
				return@changeOtherActive
			}
		}
	}

	fun  addOverlayView(context: Context, index: Int, layoutId: Int): View {
		val overlayLayout = OverlayWindowLinearLayout(context, this, index, layoutId)
		// ここでビューをオーバーレイ領域に追加する
		this.put(overlayLayout.name, overlayLayout.params, overlayLayout, overlayLayout.overlayMiniView, false)
		return overlayLayout.mainLayoutView
	}

	fun finish() {
		for (overlayName in overlayWindowMap.keys.toList()) {
			this.remove(overlayName)
		}
	}
}