package jp.kght6123.smalllittleappviewer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.IBinder
import android.widget.ImageView
import android.widget.LinearLayout
import jp.kght6123.multiwindow.MultiFloatWindowManager
import jp.kght6123.multiwindow.utils.UnitUtils

/**
 * マルチウィンドウライブラリのテスト用サービスクラス
 *
 * Created by kght6123 on 2017/05/09.
 */
class MultiFloatWidgetTestService : Service() {

	//private val TAG = this.javaClass.name

	val manager: MultiFloatWindowManager by lazy { MultiFloatWindowManager(applicationContext) }
	val iconView1 by lazy {
		val iconView = ImageView(applicationContext)
		iconView.setImageResource(R.mipmap.ic_launcher)
		iconView.isFocusableInTouchMode = true
		iconView.isFocusable = true
		iconView
	}
	val iconViewLayoutParam1 by lazy {
		LinearLayout.LayoutParams(
				UnitUtils.convertDp2Px(75f, applicationContext).toInt(),
				UnitUtils.convertDp2Px(75f, applicationContext).toInt())
	}

	val appWidgetHost by lazy {
		AppWidgetHost(this, MultiFloatWindowTestActivity.APP_WIDGET_HOST_ID)
	}
	var appWidgetId :Int = -1

	override fun onBind(intent: Intent?): IBinder {
		throw UnsupportedOperationException("Not yet implemented")
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

		val appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: appWidgetId
		val appWidgetProviderInfo = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)

		appWidgetHost.startListening()

		val appWidgetHostView = appWidgetHost.createView(this, appWidgetId, appWidgetProviderInfo)
		appWidgetHostView.layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,//appWidgetProviderInfo.minWidth,
				LinearLayout.LayoutParams.MATCH_PARENT)//appWidgetProviderInfo.minHeight)
		appWidgetHostView.setAppWidget(appWidgetId, appWidgetProviderInfo)
		//appWidgetHostView.setBackgroundResource(R.color.float_window_background_color)

		val margin = UnitUtils.convertDp2Px(25f, applicationContext).toInt()
		val initWidth = UnitUtils.convertDp2Px(300f, applicationContext).toInt()
		val initHeight = UnitUtils.convertDp2Px(450f, applicationContext).toInt()

		val info1 = manager.add(1, margin, margin, false, getColor(R.color.float_window_background_color), initWidth, initHeight)
		info1.miniWindowFrame.addView(iconView1, iconViewLayoutParam1)
		info1.windowInlineFrame.addView(appWidgetHostView)

		// 前面で起動する
		val pendingIntent =
				PendingIntent.getActivity(this, 0, Intent(this, MultiFloatWindowTestActivity::class.java), 0)
		val notification = Notification.Builder(this)
				.setContentTitle("マルチウィジットアプリ")
				.setContentText("起動中")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.build()
		startForeground(startId, notification)

		return START_NOT_STICKY // 強制終了後に再起動されない
	}

	override fun onDestroy() {
		super.onDestroy()
		manager.finish()
	}
}