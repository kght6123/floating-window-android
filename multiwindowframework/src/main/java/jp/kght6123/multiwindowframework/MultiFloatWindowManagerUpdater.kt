package jp.kght6123.multiwindowframework

/**
 * MultiFloatWindowManagerの更新を行うためのインターフェース
 *
 * @author    kght6123
 * @copyright 2017/11/04 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
interface MultiFloatWindowManagerUpdater {
    fun setMinWidth(seq: Int, value: Int)
    fun setMinHeight(seq: Int, value: Int)
    fun setMaxWidth(seq: Int, value: Int)
    fun setMaxHeight(seq: Int, value: Int)
    fun setResize(seq: Int, value: Boolean)
}

/**
 * MultiFloatWindowManagerをリフレクションで更新を行うクラス
 *
 * @author    kght6123
 * @copyright 2017/11/04 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class MultiFloatWindowManagerUpdaterImpl(private val manager: Any) : MultiFloatWindowManagerUpdater {

    private val managerClass       = manager.javaClass
    private val setMinWidthMethod  = managerClass.getMethod("setMinWidth", Int::class.java, Int::class.java)
    private val setMinHeightMethod = managerClass.getMethod("setMinHeight", Int::class.java, Int::class.java)
    private val setMaxWidthMethod  = managerClass.getMethod("setMaxWidth", Int::class.java, Int::class.java)
    private val setMaxHeightMethod = managerClass.getMethod("setMaxHeight", Int::class.java, Int::class.java)
    private val setResizeMethod    = managerClass.getMethod("setResize", Int::class.java, Boolean::class.java)

    override fun setMinWidth(seq: Int, value: Int) {
        setMinWidthMethod.invoke(manager, seq, value)
    }
    override fun setMinHeight(seq: Int, value: Int) {
        setMinHeightMethod.invoke(manager, seq, value)
    }
    override fun setMaxWidth(seq: Int, value: Int) {
        setMaxWidthMethod.invoke(manager, seq, value)
    }
    override fun setMaxHeight(seq: Int, value: Int) {
        setMaxHeightMethod.invoke(manager, seq, value)
    }
    override fun setResize(seq: Int, value: Boolean) {
        setResizeMethod.invoke(manager, seq, value)
    }
}