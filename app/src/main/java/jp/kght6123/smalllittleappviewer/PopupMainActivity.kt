package jp.kght6123.smalllittleappviewer

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button

/**
 * It extends MainActivity so that you can do as little work here as possible. It will contain al of the functions from the
 * MainActivity, so you only have to Override the ones that should change the functionality if it is in floating window form.
 */
class PopupMainActivity : MainActivity() {
	/**
	 * This method overrides the MainActivity method to set up the actual window for the popup.
	 * This is really the only method needed to turn the app into popup form. Any other methods would change the behavior of the UI.
	 * Call this method at the beginning of the main activity.
	 * You can't call setContentView(...) before calling the window service because it will throw an error every time.
	 */
	override fun setUpWindow() {
// Creates the layout for the window and the look of it
		requestWindowFeature(Window.FEATURE_ACTION_BAR)
		//window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
		//		WindowManager.LayoutParams.FLAG_DIM_BEHIND)
		window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // 背景を暗くする
		window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
		//window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
		//window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
		window.addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
		//window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
		window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY)
		window.setFormat(PixelFormat.TRANSLUCENT)

// Params for the window.
// You can easily set the alpha and the dim behind the window from here
		val params = window.attributes
		params.alpha = 1.0f // lower than one makes it more transparent（値を減らすと透明に）
		params.dimAmount = 0f // set it higher if you want to dim behind the window （値を増やすと背景が暗く）
		window.attributes = params
// Gets the display size so that you can set the window to a percent of that
		val display = windowManager.defaultDisplay
		val size = Point()
		display!!.getSize(size)
		val width = size.x
		val height = size.y
// You could also easily used an integer value from the shared preferences to set the percent
		if (height > width) {
			window.setLayout((width * .9).toInt(), (height * .7).toInt())
		} else {
			window.setLayout((width * .7).toInt(), (height * .8).toInt())
		}
	}

	fun setUpWindow2() {
		val inflater = LayoutInflater.from(this)
		val params = WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				40/*X*/, 40/*Y*/,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,// _type
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,// _flags
				PixelFormat.TRANSLUCENT)// _format

		val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val player_view = inflater.inflate(R.layout.main, null)
		wm.addView(player_view, params)

		wm.updateViewLayout(player_view, params)

		wm.removeView(player_view)
	}
	/**
	 * This method is used to set up the button and what the outcome of pressing it will be.
	 * This is an example of a way to make the app behave differently in the windowed form.
	 * No matter how complex your Main Activity is, you can take bits and peices and change them to
	 *
	 * Another way to change items depending on if the user is in the windowed form or the full form would be to set a
	 * boolean for isPopup to true in the setUpWindow function. The boolean should be defined as public or protected in
	 * the MainActivity.
	 */
	override fun setUpButton() {
		// finds the button from my content view
		val switchMode = findViewById(R.id.switch_modes) as Button
		// sets the text to the text i want in my windowed view
		switchMode.text = resources.getString(R.string.switch_to_full)
		// makes it open the full app when you click the button
		switchMode.setOnClickListener(/*object : View.OnClickListener {
			override fun onClick(view: View?) */{
				// closes the current process.
				// You wouldn't actually have to do this if you wanted a part of your app just to come up on top of your current activity.
				finish()

				// Starts the new regular main activity
				//@Suppress("CAST_NEVER_SUCCEEDS")
				val fullApp = Intent(this@PopupMainActivity as Context, MainActivity::class.java)
				fullApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(fullApp)
			}
		/*}*/)
	}
}