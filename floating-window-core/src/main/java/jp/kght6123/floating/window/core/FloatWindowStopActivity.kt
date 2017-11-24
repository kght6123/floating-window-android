package jp.kght6123.floating.window.core

import jp.kght6123.floating.window.framework.FloatWindowBaseActivity

/**
 * MultiFloatWindowApplicationを停止するActivity
 *
 * @author    kght6123
 * @copyright 2017/11/19 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowStopActivity : FloatWindowBaseActivity() {
    override fun onServiceConnected() {
        launcher.stop()
        finish()
    }
}