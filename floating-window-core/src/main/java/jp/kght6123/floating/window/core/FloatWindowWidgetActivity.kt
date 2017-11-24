package jp.kght6123.floating.window.core

import jp.kght6123.floating.window.framework.FloatWindowBaseActivity

/**
 * MultiFloatWindowApplicationのWidget追加用のActivity
 *
 * @author    kght6123
 * @copyright 2017/10/01 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowWidgetActivity : FloatWindowBaseActivity() {
    override fun onServiceConnected() {
        launcher.nextIndex(1)
    }
    override fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {
        super.onFindNextIndex(nextIndex, returnCommand)
        if (returnCommand == 1) {
            startAppWidgetWindowView(nextIndex)
        }
    }
    override fun onAppWidgetResult(result: Boolean) {
        finish()
    }
}