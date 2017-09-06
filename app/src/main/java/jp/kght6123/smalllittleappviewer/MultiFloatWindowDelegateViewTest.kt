package jp.kght6123.smalllittleappviewer

import android.content.Context
import android.view.View

/**
 * Created by kogahirotaka on 2017/09/06.
 */
class MultiFloatWindowDelegateViewTest(val applicationContext: Context) {

    public fun onCreate(): View {
        val view = View.inflate(applicationContext, R.layout.remote_mini_view, null)//LayoutInflater.from(applicationContext).inflate(R.layout.remote_mini_view, null)
        return view
    }
}