package jp.kght6123.smallappbrowser.activity

import android.app.Activity
import android.os.Bundle



/**
 * Activity経由でスモールブラウザを起動するためのデリゲーター
 *
 * Created by kght6123 on 2017/08/04.
 */
class HttpActionDelegateActivity :Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val intent = Intent(intent)
//        intent.setClass(this, SmallBrowserApplication::class.java)
//
//        try {
//            SmallApplicationManager.startApplication(this, intent)
//        } catch (e: SmallAppNotFoundException) {
//            Toast.makeText(this, e.getMessage(),
//                    Toast.LENGTH_SHORT).show()
//        }
        finish()
    }
}