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
open class EdgeAnchorLayer(private val position: Position, private val info: FloatWindowInfo, private val fixedWidth: Int = 0, borderDpWidth: Float = info.initSettings.windowBorderDpWidth): AnchorLayer(position, info) {

    //private val tag = this.javaClass.name
    private val borderWidth = UnitUtils.convertDp2Px(borderDpWidth, info.context).toInt()

    override fun getAnchorLayoutResource(): Int {
        return R.layout.edge_layer
    }
    override fun getAnchorLayerId(): Int {
        return R.id.anchor
    }
    override fun getBorderWidth(): Int {
        return borderWidth
    }
    override fun updatePosition(x: Int, y: Int) {
        val params = info.getWindowLayoutParams()

        when (position) {
            Position.TOP -> {
                if(fixedWidth > 0) {
                    anchorLayerParams.width = fixedWidth// No debug
                } else {
                    anchorLayerParams.width = params.width + borderWidth - shadowWidth * 2
                }
                anchorLayerParams.height = borderWidth
                anchorLayerParams.x = x - borderWidth + shadowWidth
                anchorLayerParams.y = y - borderWidth + shadowWidth
            }
            Position.BOTTOM -> {
                if(fixedWidth > 0) {
                    anchorLayerParams.width = fixedWidth
                    anchorLayerParams.x = x + shadowWidth * 2 + (params.width - fixedWidth)
                } else {
                    anchorLayerParams.width = params.width + borderWidth - shadowWidth * 2
                    anchorLayerParams.x = x + shadowWidth
                }
                anchorLayerParams.height = borderWidth
                anchorLayerParams.y = y + params.height - shadowWidth
            }
            Position.LEFT -> {
                anchorLayerParams.width = borderWidth
                if(fixedWidth > 0) {
                    anchorLayerParams.height = fixedWidth// No debug
                } else {
                    anchorLayerParams.height = params.height + borderWidth - shadowWidth * 2
                }
                anchorLayerParams.x = x - borderWidth + shadowWidth
                anchorLayerParams.y = y + shadowWidth
            }
            Position.RIGHT -> {
                anchorLayerParams.width = borderWidth
                if(fixedWidth > 0) {
                    anchorLayerParams.height = fixedWidth / 2
                    anchorLayerParams.y = y - borderWidth + shadowWidth * 2 + (params.height - fixedWidth / 2)
                } else {
                    anchorLayerParams.height = params.height + borderWidth - shadowWidth * 2
                    anchorLayerParams.y = y - borderWidth + shadowWidth
                }
                anchorLayerParams.x = x + params.width - shadowWidth
            }
        }
    }
    override fun updateStroke(event: MotionEvent) {

        val params = info.getWindowLayoutParams()

        val left = event.x in 0..(getBorderWidth())
        val right = event.x in (params.width - shadowWidth*2)..(params.width - shadowWidth*2 + getBorderWidth())
        val top = event.y in 0..(getBorderWidth())
        val bottom = event.y in (params.height - shadowWidth*2)..(params.height - shadowWidth*2 + getBorderWidth())

        when(this@EdgeAnchorLayer.position){
            AnchorLayer.Position.TOP -> {
                when {
                    left ->
                        info.strokeMode = FloatWindowInfo.Stroke.TOP_LEFT
                //right ->
                //    info.strokeMode = FloatWindowInfo.Stroke.TOP_RIGHT
                    else ->
                        info.strokeMode = FloatWindowInfo.Stroke.TOP
                }
            }
            AnchorLayer.Position.BOTTOM -> {
                when {
                //left ->
                //    info.strokeMode = FloatWindowInfo.Stroke.BOTTOM_LEFT
                    right ->
                        info.strokeMode = FloatWindowInfo.Stroke.BOTTOM_RIGHT
                    else ->
                        info.strokeMode = FloatWindowInfo.Stroke.BOTTOM
                }
            }
            AnchorLayer.Position.LEFT -> {
                when {
                //top ->
                //    info.strokeMode = FloatWindowInfo.Stroke.TOP_LEFT
                    bottom ->
                        info.strokeMode = FloatWindowInfo.Stroke.BOTTOM_LEFT
                    else ->
                        info.strokeMode = FloatWindowInfo.Stroke.LEFT
                }
            }
            AnchorLayer.Position.RIGHT -> {
                when {
                    top ->
                        info.strokeMode = FloatWindowInfo.Stroke.TOP_RIGHT
                //bottom ->
                //    info.strokeMode = FloatWindowInfo.Stroke.BOTTOM_RIGHT
                    else ->
                        info.strokeMode = FloatWindowInfo.Stroke.RIGHT
                }
            }
        }
    }
    override fun updateBackgroundResource(resId: Int, alpha: Float) {
        anchorLayer.setBackgroundResource(resId)
    }
}