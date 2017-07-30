package jp.kght6123.multiwindow.utils

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

/**
 * 画面系のユーティリティクラス
 *
 * Created by kght6123 on 2017/07/29.
 */
class DisplayUtils {

    companion object {

        fun defaultDisplaySize(context: Context): Point {
            val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            return size
        }
    }
}