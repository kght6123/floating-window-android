package jp.kght6123.smalllittleappviewer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.IBinder
import android.view.*

/**
 * Created by koga.hirotaka on 2017/05/09.
 */
class SystemOverlayLayerService : Service() {

    // オーバーレイ表示させるビュー
    val overlayView: ViewGroup by lazy { LayoutInflater.from(this).inflate(R.layout.service_system_overlay_layer, null) as ViewGroup }

    // WindowManager
    val windowManager: WindowManager by lazy { applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    // WindowManagerに設定するレイアウトパラメータ
    var params: WindowManager.LayoutParams? = null

    // ディスプレイのサイズを格納する
    val displaySize: Point by lazy {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        size
    }

    // ロングタップ判定用
    var isLongClick: Boolean = false

// 中略 //

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // applyを共通関数？で実行、apply()の戻り値はレシーバオブジェクト本体、つまり呼び出したインスタンスそのもの
        overlayView.apply(clickListener())

        // オーバーレイViewの設定をする
        params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT)

        // ここでビューをオーバーレイ領域に追加する
        windowManager.addView(overlayView, params)

        return START_STICKY
    }

    // レシーバはView型で引数はなし、戻り値はUnit型（voidと同等）
    //  => あたかも、View.clickListener()の様に呼び出せる
    //  => clickListener()はapplyの引数を共通関数化？した関数
    private fun clickListener(): View.() -> Unit {
        return {
            setOnLongClickListener { view ->
                // ロングタップ状態にする
                isLongClick = true
                // ロングタップ状態が分かりやすいように背景色を変える
                view.setBackgroundResource(android.R.color.holo_red_light)
                false

            }.apply {// Unit型にapply？？、以前のapplyのスコープが引き継がれるっぽい？
                setOnTouchListener { view, motionEvent ->

                    // タップした位置を取得する
                    val x = motionEvent.rawX.toInt()
                    val y = motionEvent.rawY.toInt()

                    // motionEvent.getX()はタップされたViewの座標系での座標、これが使えそう。
                    // view.getWidth()、view.getHeight()はViewのサイズ、上と組み合わせて・・・

                    when (motionEvent.action) {
                        // Viewを移動させてるときに呼ばれる
                        MotionEvent.ACTION_MOVE -> {
                           // if (isLongClick) {    // 長押しで移動を無効化してみた

                                // 中心からの移動量を計算する
                                val centerX = x - (displaySize.x / 2)
                                val centerY = y - (displaySize.y / 2)

                                // オーバーレイ表示領域を移動量分だけ移動させる
                                params?.x = centerX
                                params?.y = centerY

                                // 移動した分を更新する
                                windowManager.updateViewLayout(overlayView, params)
                            //}
                        }

                        // Viewの移動が終わったときに呼ばれる
                        MotionEvent.ACTION_UP -> {
                            if (isLongClick) {
                                // 背景色を戻す
                                view.setBackgroundResource(android.R.color.white) // android.R.color.transparent
                            }
                            isLongClick = false
                        }
                    }
                    false
                }
            }
        }
    }
}