package jp.kght6123.multiwindow

/**
 * MultiFloatWindowApplicationのWidget追加用のActivity
 *
 * Created by kght6123 on 2017/10/01.
 */
class MultiFloatWindowWidgetActivity : MultiFloatWindowBaseActivity() {
    override fun onServiceConnected() {
        launcher.nextIndex(1)
    }
    override fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {
        super.onFindNextIndex(nextIndex, returnCommand)
        if (returnCommand == 1) {
            startAppWidgetWindowView(nextIndex)
        }
    }
}