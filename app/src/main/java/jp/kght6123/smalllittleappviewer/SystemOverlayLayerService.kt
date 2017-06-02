package jp.kght6123.smalllittleappviewer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import jp.kght6123.smalllittleappviewer.custom.view.ExTouchFocusLinearLayout
import jp.kght6123.smalllittleappviewer.custom.view.OverlayWindowLinearLayout


/**
 * Created by koga.hirotaka on 2017/05/09.
 */
class SystemOverlayLayerService : Service() {

	private val TAG = this.javaClass.simpleName
	
	// オーバーレイ表示させるビュー
	val overlayView: OverlayWindowLinearLayout by lazy {
		val view: OverlayWindowLinearLayout = OverlayWindowLinearLayout(this)
		view
	}
	val overlayView2: OverlayWindowLinearLayout by lazy {
		val view: OverlayWindowLinearLayout = OverlayWindowLinearLayout(this)
		view
	}
	
//	val params1: WindowManager.LayoutParams by lazy { WindowManager.LayoutParams(
//			UnitUtils.convertDp2Px(100f, this).toInt(),
//			UnitUtils.convertDp2Px(100f, this).toInt(),
//			500,// X
//			100,// Y
//			WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,     // なるべく上の階層で表示
//			inactiveFlags,
//			PixelFormat.TRANSLUCENT)
//	}
//
//	val params2: WindowManager.LayoutParams by lazy { WindowManager.LayoutParams(
//			UnitUtils.convertDp2Px(150f, this).toInt(),
//			UnitUtils.convertDp2Px(150f, this).toInt(),
//			100,// X
//			500,// Y
//			WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,     // なるべく上の階層で表示
//			inactiveFlags,
//			PixelFormat.TRANSLUCENT)
//	}

	override fun onBind(intent: Intent?): IBinder {
		throw UnsupportedOperationException("Not yet implemented")
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		
		// ここでビューをオーバーレイ領域に追加する
//		windowManager.addView(createTestView1(), params1)
//		windowManager.addView(createTestView2(), params2)
		
		/**
		 * * layout xml 動的生成
		- Android　LayoutInflaterについて(生成,方法比較,実装) http://qiita.com/Bth0061/items/c4f66477979d064913e4
		- 一番TopのカスタムViewだけnewして、配下の子layoutはxmlをinflateして追加とかで何とかならないか？
		- 下記みたいな感じにして、すべてのViewをnewで追加するのは避けたい。
		- WindowManagerにaddViewしたときの各LayoutParam＋追加したViewは、Map<Key,ViewAndParam>などで保持が必要となる。
		
		```
		val windowView: View = View.inflate(context, R.layout.layout_sample/*フレームView*/, root/*親カスタムView*/, true/*rootにchild追加*/)
		View.inflate(context, R.layout.layout_sample/*アプリView*/, windowView, true/*rootにchild追加*/)
		```
		 */
		
		// Overlay用のViewを初期化
		overlayView
		overlayView2
		
		// 前面で起動する
		val pendingIntent =
				PendingIntent.getActivity(this, 0, Intent(this, SystemOverlayLayerActivity::class.java), 0)
		val notification = Notification.Builder(this)
				.setContentTitle("スモールアプリ")
				.setContentText("起動中")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.build()
		startForeground(startId, notification)
		
		return START_NOT_STICKY // 強制終了後に再起動されない
	}
	
//	fun createTestView1(): LinearLayout {
//		val layout = LinearLayout(this)
//		layout.orientation = LinearLayout.VERTICAL
//		layout.layoutParams = LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT
//				,LinearLayout.LayoutParams.MATCH_PARENT
//				)
//		layout.gravity = Gravity.CENTER
//		layout.setBackgroundResource(android.R.drawable.screen_background_light_transparent)
//		return layout
//	}
//
//	fun createTestView2(): LinearLayout {
//		val layout = LinearLayout(this)
//		layout.orientation = LinearLayout.VERTICAL
//		layout.layoutParams = LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT
//				,LinearLayout.LayoutParams.MATCH_PARENT
//		)
//		layout.gravity = Gravity.CENTER
//		layout.setBackgroundResource(android.R.drawable.screen_background_light_transparent)
//		return layout
//	}
	
	override fun onDestroy() {
		super.onDestroy()
		overlayView.finish()
		overlayView2.finish()
	}
}