package jp.kght6123.floating.window.core.layer

import android.util.Log
import jp.kght6123.floating.window.core.FloatWindowInfo
import jp.kght6123.floating.window.core.R

/**
 *
 * @author    kght6123
 * @copyright 2017/12/10 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class EdgeAnchorLayerGroup(
        private val info: FloatWindowInfo,
        private val windowBorderOpacity: Float = info.initSettings.windowBorderOpacity,
        private val windowBorderTouchOpacity: Float = info.initSettings.windowBorderTouchOpacity) : AnchorLayerGroup() {

    private val tag = this.javaClass.name

    private val anchorLayerTop: AnchorLayer
            by lazy { EdgeAnchorLayer(AnchorLayer.Position.TOP, info) }
    private val anchorLayerLeft: AnchorLayer
            by lazy { EdgeAnchorLayer(AnchorLayer.Position.LEFT, info) }
    private val anchorLayerRight: AnchorLayer
            by lazy { EdgeAnchorLayer(AnchorLayer.Position.RIGHT, info) }
    private val anchorLayerBottom: AnchorLayer
            by lazy { EdgeAnchorLayer(AnchorLayer.Position.BOTTOM, info) }

    init {
        anchorLayerTop
        anchorLayerLeft
        anchorLayerRight
        anchorLayerBottom
    }
    override fun onAdd(): Boolean {
        Log.d(tag, "onAdd windowBorderOpacity=$windowBorderOpacity, windowBorderTouchOpacity=$windowBorderTouchOpacity")
        anchorLayerTop.add()
        anchorLayerBottom.add()
        anchorLayerLeft.add()
        anchorLayerRight.add()
        return true
    }
    override fun onChangeStrokeMode(strokeMode: FloatWindowInfo.Stroke) {
        Log.d(tag, "onChangeStrokeMode strokeMode=$strokeMode")
        when (strokeMode) {
            FloatWindowInfo.Stroke.TOP -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.BOTTOM -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.LEFT -> {
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.RIGHT -> {
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.TOP_LEFT -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.TOP_RIGHT -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.BOTTOM_LEFT -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.BOTTOM_RIGHT -> {
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_resize, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.ALL -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color_move, windowBorderTouchOpacity)
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color_move, windowBorderTouchOpacity)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color_move, windowBorderTouchOpacity)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color_move, windowBorderTouchOpacity)
            }
            FloatWindowInfo.Stroke.UNKNOWN -> {
                anchorLayerTop.updateBackgroundResource(R.color.float_window_anchor_color, windowBorderOpacity)
                anchorLayerBottom.updateBackgroundResource(R.color.float_window_anchor_color, windowBorderOpacity)
                anchorLayerLeft.updateBackgroundResource(R.color.float_window_anchor_color, windowBorderOpacity)
                anchorLayerRight.updateBackgroundResource(R.color.float_window_anchor_color, windowBorderOpacity)
            }
        }
    }
    override fun onChangePosition(x: Int, y: Int, update: Boolean) {
        Log.d(tag, "onChangeStrokeMode x=$x, y=$y, update=$update")
        anchorLayerTop.updatePosition(x, y, update)
        anchorLayerLeft.updatePosition(x, y, update)
        anchorLayerRight.updatePosition(x, y, update)
        anchorLayerBottom.updatePosition(x, y, update)
    }
    override fun onRemove(): Boolean {
        Log.d(tag, "onRemove windowBorderOpacity=$windowBorderOpacity, windowBorderTouchOpacity=$windowBorderTouchOpacity")
        anchorLayerTop.remove()
        anchorLayerBottom.remove()
        anchorLayerLeft.remove()
        anchorLayerRight.remove()
        return true
    }
    override fun getX(): Int = anchorLayerTop.getX()
    override fun getY(): Int = anchorLayerTop.getY()
}