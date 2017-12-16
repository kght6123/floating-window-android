package jp.kght6123.floating.window.core.layer

import android.view.MotionEvent
import jp.kght6123.floating.window.core.FloatWindowInfo
import jp.kght6123.floating.window.core.R
import jp.kght6123.floating.window.framework.utils.UnitUtils

/**
 *
 * @author    kght6123
 * @copyright 2017/12/16 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class SinglePointEdgeAnchorLayer(private val position: Position, private val info: FloatWindowInfo, private val fixedWidth: Int): EdgeAnchorLayer(position, info, fixedWidth) {

    override fun updateStroke(event: MotionEvent) {
        when(this@SinglePointEdgeAnchorLayer.position){
            AnchorLayer.Position.TOP,
            AnchorLayer.Position.LEFT -> {
                info.strokeMode = FloatWindowInfo.Stroke.TOP_LEFT
            }
            AnchorLayer.Position.BOTTOM,
            AnchorLayer.Position.RIGHT -> {
                info.strokeMode = FloatWindowInfo.Stroke.BOTTOM_RIGHT
            }
        }
    }
}