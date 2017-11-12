package jp.kght6123.multiwindow

import jp.kght6123.multiwindowframework.MultiFloatWindowBaseActivity

/**
 * MultiFloatWindowApplicationの起点Activity
 *
 * @author    kght6123
 * @copyright 2017/10/01 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class MultiFloatWindowMainActivity : MultiFloatWindowBaseActivity() {
    override fun onServiceConnected() {
        finish()
    }
}