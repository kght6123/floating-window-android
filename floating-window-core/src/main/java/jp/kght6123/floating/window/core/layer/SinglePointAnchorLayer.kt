package jp.kght6123.floating.window.core.layer

import android.view.MotionEvent
import jp.kght6123.floating.window.core.FloatWindowInfo
import jp.kght6123.floating.window.core.R
import jp.kght6123.floating.window.framework.utils.UnitUtils

/**
 *
 * @author    kght6123
 * @copyright 2017/12/03 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class SinglePointAnchorLayer(private val position: Position, private val info: FloatWindowInfo): AnchorLayer(position, info) {

    //private val tag = this.javaClass.name
    private val borderWidth = UnitUtils.convertDp2Px(48f, info.context).toInt()

    init {
        anchorLayerParams.width = borderWidth
        anchorLayerParams.height = borderWidth
    }
    override fun getAnchorLayoutResource(): Int {
        return R.layout.point_layer
    }
    override fun getAnchorLayerId(): Int {
        return R.id.pointAnchor
    }
    override fun getBorderWidth(): Int {
        return UnitUtils.convertDp2Px(24f, info.context).toInt()
    }
    override fun updatePosition(x: Int, y: Int) {
        val params = info.getWindowLayoutParams()

        when (position) {
            Position.TOP -> {
                anchorLayerParams.x = x - borderWidth + (borderWidth / 2) + shadowWidth
                anchorLayerParams.y = y - borderWidth + (borderWidth / 2) + shadowWidth
            }
            Position.BOTTOM -> {// No Debug
                anchorLayerParams.x = x + shadowWidth
                anchorLayerParams.y = y + params.height - shadowWidth
            }
            Position.LEFT -> {// No Debug
                anchorLayerParams.x = x - borderWidth + shadowWidth
                anchorLayerParams.y = y + shadowWidth
            }
            Position.RIGHT -> {// No Debug
                anchorLayerParams.x = x + params.width - shadowWidth
                anchorLayerParams.y = y - borderWidth + shadowWidth
            }
        }
    }
    override fun updateStroke(event: MotionEvent) {
        when(this@SinglePointAnchorLayer.position){
            AnchorLayer.Position.TOP -> {
                info.strokeMode = FloatWindowInfo.Stroke.TOP_LEFT
            }
            AnchorLayer.Position.BOTTOM -> {
                info.strokeMode = FloatWindowInfo.Stroke.BOTTOM_RIGHT
            }
            AnchorLayer.Position.LEFT -> {
                info.strokeMode = FloatWindowInfo.Stroke.TOP_LEFT
            }
            AnchorLayer.Position.RIGHT -> {
                info.strokeMode = FloatWindowInfo.Stroke.BOTTOM_RIGHT
            }
        }
    }
    override fun updateBackgroundResource(resId: Int) {
        anchorLayer.setBackgroundResource(resId)
    }
}