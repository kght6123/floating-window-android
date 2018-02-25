package jp.kght6123.floating.window.core.layer

import android.util.Log
import jp.kght6123.floating.window.core.FloatWindowInfo

/**
 *
 * @author    kght6123
 * @copyright 2017/12/10 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
abstract class AnchorLayerGroup {

    private val tag = this.javaClass.name
    private var addFlag: Boolean = false

    fun add() {
        Log.d(tag, "add")
        if (!addFlag) {
            Log.d(tag, "add addFlag=$addFlag")
            if(onAdd()) {
                Log.d(tag, "add addFlag=$addFlag onAdd ok")
                addFlag = true
            }
        }
    }
    abstract fun onChangeStrokeMode(strokeMode: FloatWindowInfo.Stroke)
    abstract fun onChangePosition(x: Int, y: Int, update: Boolean)
    fun remove() {
        Log.d(tag, "remove")
        if (addFlag) {
            Log.d(tag, "remove addFlag=$addFlag")
            if(onRemove()) {
                Log.d(tag, "remove addFlag=$addFlag onRemove ok")
                addFlag = false
            }
        }
    }
    abstract fun getX(): Int
    abstract fun getY(): Int

    abstract fun onAdd(): Boolean
    abstract fun onRemove(): Boolean
}