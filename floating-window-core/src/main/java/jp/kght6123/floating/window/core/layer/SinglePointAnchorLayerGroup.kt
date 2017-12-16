package jp.kght6123.floating.window.core.layer

import jp.kght6123.floating.window.core.FloatWindowInfo
import jp.kght6123.floating.window.core.R
import jp.kght6123.floating.window.framework.utils.UnitUtils

/**
 *
 * @author    kght6123
 * @copyright 2017/12/10 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class SinglePointAnchorLayerGroup(private val info: FloatWindowInfo) : AnchorLayerGroup {

    private val leftBottomAnchorWidth = UnitUtils.convertDp2Px(48f, info.context).toInt()

    private val anchorLayerTop: AnchorLayer by lazy { SinglePointAnchorLayer(AnchorLayer.Position.TOP, info) }
    private val anchorLayerRight: AnchorLayer by lazy { SinglePointEdgeAnchorLayer(AnchorLayer.Position.RIGHT, info, leftBottomAnchorWidth) }
    private val anchorLayerBottom: AnchorLayer by lazy { SinglePointEdgeAnchorLayer(AnchorLayer.Position.BOTTOM, info, leftBottomAnchorWidth) }

    init {
        anchorLayerTop
        anchorLayerRight
        anchorLayerBottom
    }
    override fun add() {
        anchorLayerTop.add()
        anchorLayerBottom.add()
        anchorLayerRight.add()
    }
    override fun onChangeStrokeMode(strokeMode: FloatWindowInfo.Stroke) {
        when (strokeMode) {
            FloatWindowInfo.Stroke.TOP -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize)
            }
            FloatWindowInfo.Stroke.BOTTOM -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize)
            }
            FloatWindowInfo.Stroke.LEFT -> {

            }
            FloatWindowInfo.Stroke.RIGHT -> {
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize)
            }
            FloatWindowInfo.Stroke.TOP_LEFT -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize)
            }
            FloatWindowInfo.Stroke.TOP_RIGHT -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize)
            }
            FloatWindowInfo.Stroke.BOTTOM_LEFT -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize)
            }
            FloatWindowInfo.Stroke.BOTTOM_RIGHT -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize)
            }
            FloatWindowInfo.Stroke.ALL -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_move)
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_move)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_move)
            }
            FloatWindowInfo.Stroke.UNKNOWN -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color)
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color)
            }
        }
    }
    override fun onChangePosition(x: Int, y: Int, update: Boolean) {
        anchorLayerTop.updatePosition(x, y, update)
        anchorLayerRight.updatePosition(x, y, update)
        anchorLayerBottom.updatePosition(x, y, update)
    }
    override fun remove() {
        anchorLayerTop.remove()
        anchorLayerBottom.remove()
        anchorLayerRight.remove()
    }
    override fun getX(): Int = anchorLayerTop.getX()
    override fun getY(): Int = anchorLayerTop.getY()
}