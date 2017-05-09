package jp.kght6123.smalllittleappviewer

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

/**
 * Created by koga.hirotaka on 2017/05/09.
 */
class SystemOverlayLayerActivity : Activity() {

    val start_button: Button by lazy {
        findViewById(R.id.start_button) as Button
    }

    val stop_button: Button by lazy {
        findViewById(R.id.stop_button) as Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_overlay_layer)

        start_button.setOnClickListener({
            startService(Intent(this@SystemOverlayLayerActivity, SystemOverlayLayerService::class.java))
        })
        stop_button.setOnClickListener({
            stopService(Intent(this@SystemOverlayLayerActivity, SystemOverlayLayerService::class.java))
        })
    }
}
