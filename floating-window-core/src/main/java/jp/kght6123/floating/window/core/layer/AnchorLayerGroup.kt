package jp.kght6123.floating.window.core.layer

import jp.kght6123.floating.window.core.FloatWindowInfo

/**
 *
 * @author    kght6123
 * @copyright 2017/12/10 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
interface AnchorLayerGroup {
    fun add()
    fun onChangeStrokeMode(strokeMode: FloatWindowInfo.Stroke)
    fun onChangePosition(x: Int, y: Int, update: Boolean)
    fun remove()
    fun getX(): Int
    fun getY(): Int
}