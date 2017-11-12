package jp.kght6123.multiwindowframework.utils

import android.content.Context

/**
 * 数値の単位変換ユーティリティ
 *
 * @author    kght6123
 * @copyright 2017/11/04 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class UnitUtils {

    companion object {

        /**
         * dpからpixelへの変換
         * @param dp
         * @param context
         * @return float pixel
         */
        fun convertDp2Px(dp: Float, context: Context): Float {
            val metrics = context.resources.displayMetrics
            return dp * metrics.density
        }

        /**
         * pixelからdpへの変換
         * @param px
         * @param context
         * @return float dp
         */
        fun convertPx2Dp(px: Int, context: Context): Float {
            val metrics = context.resources.displayMetrics
            return px / metrics.density
        }
    }
}