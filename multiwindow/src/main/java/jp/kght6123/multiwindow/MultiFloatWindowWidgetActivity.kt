package jp.kght6123.multiwindow

import jp.kght6123.multiwindowframework.MultiFloatWindowBaseActivity

/**
 * MultiFloatWindowApplicationのWidget追加用のActivity
 *
 * @author    kght6123
 * @copyright 2017/10/01 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class MultiFloatWindowWidgetActivity : MultiFloatWindowBaseActivity() {
    override fun onServiceConnected() {
        launcher.nextIndex(1)
    }
    override fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {
        super.onFindNextIndex(nextIndex, returnCommand)
        if (returnCommand == 1) {
            startAppWidgetWindowView(nextIndex)
            finish()
        }
    }
}