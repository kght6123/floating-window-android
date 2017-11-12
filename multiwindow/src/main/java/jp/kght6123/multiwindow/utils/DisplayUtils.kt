package jp.kght6123.multiwindow.utils

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

/**
 * 画面系のユーティリティクラス
 *
 * @author    kght6123
 * @copyright 2017/07/29 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
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