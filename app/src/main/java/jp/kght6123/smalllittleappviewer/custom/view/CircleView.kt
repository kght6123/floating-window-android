package jp.kght6123.smalllittleappviewer.custom.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.content.res.TypedArray
import android.view.ViewGroup
import android.widget.LinearLayout
import jp.kght6123.smalllittleappviewer.R


/**
 * 正円のViewを描画するクラス
 * Created by kght6123 on 2017/05/14.
 */
class CircleView : LinearLayout {
	private val paint: Paint = Paint()

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {

		val attrsArray: TypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleView, defStyleAttr, 0)

		paint.color = attrsArray.getColor(R.styleable.CircleView_color, resources.getColor(android.R.color.black))

		attrsArray.recycle()
	}
	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {}

	constructor(context: Context) : this(context, null, 0) {}

	/*fun setColor(color: Int) {
		paint.color = color
	}*/

	override fun onDraw(canvas: Canvas) {
		paint.isAntiAlias = true
		canvas.drawCircle(canvas.height / 2f, canvas.height / 2f, canvas.width / 2f - 2f, paint)
	}
}