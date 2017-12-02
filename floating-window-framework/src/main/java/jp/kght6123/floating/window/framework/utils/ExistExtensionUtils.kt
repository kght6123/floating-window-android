package jp.kght6123.floating.window.framework.utils

import android.appwidget.AppWidgetProviderInfo
import android.util.Log

/**
 * 既存クラスの拡張メソッド用クラス
 *
 * @author    kght6123
 * @copyright 2017/11/28 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */

/**
 * 初期表示の横幅を取得する
 */
fun AppWidgetProviderInfo.initMinWidth(defaultWidth: Int): Int {
    Log.d(".initMinWidth", "this.minWidth=${this.minWidth}, this.minResizeWidth=${this.minResizeWidth}")

    return when {
        this.minWidth > defaultWidth -> this.minWidth
        this.minResizeWidth > defaultWidth -> this.minResizeWidth
        else -> defaultWidth
    }
}

/**
 * 初期表示の縦幅を取得する
 */
fun AppWidgetProviderInfo.initMinHeight(defaultHeight: Int): Int {
    Log.d(".initMinHeight", "this.minHeight=${this.minHeight}, this.minResizeHeight=${this.minResizeHeight}")

    return when {
        this.minHeight > defaultHeight -> this.minHeight
        this.minResizeHeight > defaultHeight -> this.minResizeHeight
        else -> defaultHeight
    }
}

/**
 * リサイズ時の最小横幅が設定されているか？
 */
fun AppWidgetProviderInfo.isResizeWidth(): Boolean {
    return this.minResizeWidth > 0
}

/**
 * リサイズ時の最小縦幅が設定されているか？
 */
fun AppWidgetProviderInfo.isResizeHeight(): Boolean {
    return this.minResizeHeight > 0
}