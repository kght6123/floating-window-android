package jp.kght6123.smallappbrowser.activity

// FIXME Frameworkから呼び出せるようにする。ServiceをキックするLauncherやBaseActivityはコアじゃなくてFramework側で良いような。
///**
// * Activity経由でスモールブラウザを起動するためのデリゲーター
// *
// * Created by kght6123 on 2017/08/04.
// */
//class HttpActionDelegateActivity : MultiFloatWindowBaseActivity() {
//
//    private val initIntent by lazy {
//        val intent = Intent()
//        intent.data = Uri.parse("http://google.co.jp/")
//        intent
//    }
//
//    override fun onFindNextIndex(nextIndex: Int, returnCommand: Int) {
//        super.onFindNextIndex(nextIndex, returnCommand)
//
//        if (returnCommand == 0) {
//            // ブラウザを起動する
//            launcher.openWindow(nextIndex, MultiWindowOpenType.NEW, SmallBrowserApplicationService::class.java)
//
//            if(intent.dataString == null) {
//                launcher.startWindow(nextIndex, initIntent) // アプリ一覧から呼び出すとき
//            } else {
//                launcher.startWindow(nextIndex, intent) // 他のアプリから呼び出されるとき
//            }
//        }
//    }
//
//    override fun onCheckOverlayPermissionResult(result: Boolean) {
//        if (result) {
//            launcher.nextIndex(0)
//        }
//    }
//}