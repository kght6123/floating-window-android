package jp.kght6123.floating.window.core.inside.application

import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import jp.kght6123.floating.window.core.R
import jp.kght6123.floating.window.framework.FloatWindowApplication
import jp.kght6123.floating.window.framework.MultiFloatWindowConstants
import jp.kght6123.floating.window.framework.MultiWindowUpdatePosition
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * サンプルプラグインアプリケーション
 *
 * @author    kght6123
 * @copyright 2017/12/17 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowCalcApplication : FloatWindowApplication() {

    //private val tag = FloatWindowCalcApplication::class.java.name

    enum class CalcType(val text: String, val calc: (BigDecimal, BigDecimal) -> BigDecimal) {
        DIVIDE  ("%", fun(b1 :BigDecimal, b2 :BigDecimal) = b1.divide(b2)),
        MULTIPLY("X", fun(b1 :BigDecimal, b2 :BigDecimal) = b1.multiply(b2)),
        SUBTRACT("-", fun(b1 :BigDecimal, b2 :BigDecimal) = b1.subtract(b2)),
        ADD     ("+", fun(b1 :BigDecimal, b2 :BigDecimal) = b1.plus(b2)),
        UNKNOWN ( "", fun(b1 :BigDecimal, _:BigDecimal) = b1),
        EQUAL   ("=", fun(b1 :BigDecimal, _:BigDecimal) = b1)
    }

    class CalcUi: AnkoComponent<FloatWindowCalcApplication> {

        override fun createView(ui: AnkoContext<FloatWindowCalcApplication>) = with(ui) {
            val buttonSize = 0

            gridLayout {
                rowCount = 6
                columnCount = 4
                lparams {
                    width = LinearLayout.LayoutParams.MATCH_PARENT
                    height = LinearLayout.LayoutParams.MATCH_PARENT
                    //useDefaultMargins = true
                }
                var rowIndex = 0
                var columnIndex = 0
                var numberValueTemp = ""
                var calcResult = BigDecimal(0)
                var prevCalcType = CalcType.UNKNOWN

                val initLayoutParams = fun(lparams :GridLayout.LayoutParams, width: Int, height: Int, rowIndex: Int, columnIndex: Int) {
                    lparams.width = width
                    lparams.height = height
                    lparams.rowSpec = GridLayout.spec(rowIndex, GridLayout.FILL, 1f)
                    lparams.columnSpec = GridLayout.spec(columnIndex, GridLayout.FILL, 1f)
                }

                val calcView = textView("") {
                    setTextColor(resources.getColor(android.R.color.primary_text_dark, null))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // API26から
                        setAutoSizeTextTypeUniformWithConfiguration(5,
                                32,
                                1,
                                TypedValue.COMPLEX_UNIT_SP)
                    } else {
                        // API26以前
                        singleLine = true
                        ellipsize = TextUtils.TruncateAt.START
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                    }
                    topPadding = dip(6)
                    bottomPadding = dip(6)

                }.lparams {
                    width = buttonSize
                    height = dip(60)
                    rowSpec = GridLayout.spec(rowIndex, GridLayout.FILL)
                    columnSpec = GridLayout.spec(columnIndex, columnCount, GridLayout.FILL, 1f)
                }
                rowIndex++
                columnIndex = 0

                fun clearAll() {
                    prevCalcType = CalcType.UNKNOWN
                    calcView.text = ""
                    numberValueTemp = ""
                    calcResult = BigDecimal(0)
                }
                fun updateCalcView(_button: Button, clear: Boolean = false) {
                    calcView.text = if(prevCalcType != CalcType.EQUAL)
                        "${calcView.text}${_button.text}"
                    else {
                        clearAll()
                        _button.text
                    }
                    numberValueTemp = if(clear)
                        ""
                    else
                        numberValueTemp + _button.text
                }
                fun executeCalc(_button: Button, calcType: CalcType) {
                    if (numberValueTemp.isNotEmpty()) {
                        calcResult = when (prevCalcType) {
                            CalcType.UNKNOWN, CalcType.EQUAL -> {
                                prevCalcType = CalcType.UNKNOWN // updateCalcViewでclearAllさせない
                                BigDecimal(numberValueTemp)
                            }
                            else ->
                                prevCalcType.calc(calcResult, BigDecimal(numberValueTemp))
                        }
                        updateCalcView(_button, true)
                        prevCalcType = calcType
                    }
                }

                button("AC") {
                    backgroundResource = R.drawable.flatbutton_gray
                    onClick {
                        clearAll()
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button("C") {
                    backgroundResource = R.drawable.flatbutton_gray
                    onClick {
                        calcView.text = calcView.text.removeSuffix(numberValueTemp)
                        numberValueTemp = ""
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button(".") {
                    backgroundResource = R.drawable.flatbutton_gray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button(CalcType.DIVIDE.text) {
                    backgroundResource = R.drawable.flatbutton_accent
                    onClick {
                        executeCalc(this@button, CalcType.DIVIDE)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("1") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button("2") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button("3") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button(CalcType.MULTIPLY.text) {
                    backgroundResource = R.drawable.flatbutton_accent
                    onClick {
                        executeCalc(this@button, CalcType.MULTIPLY)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("4") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button("5") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button("6") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button(CalcType.SUBTRACT.text) {
                    backgroundResource = R.drawable.flatbutton_accent
                    onClick {
                        executeCalc(this@button, CalcType.SUBTRACT)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("7") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button("8") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button("9") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button(CalcType.ADD.text) {
                    backgroundResource = R.drawable.flatbutton_accent
                    onClick {
                        executeCalc(this@button, CalcType.ADD)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("0") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    width = buttonSize
                    height = buttonSize
                    rowSpec = GridLayout.spec(rowIndex, GridLayout.FILL, 1f)
                    columnSpec = GridLayout.spec(columnIndex++, columnCount-2, GridLayout.FILL, 1f)
                }
                columnIndex++

                button("00") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                    onClick {
                        updateCalcView(this@button)
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
                button(CalcType.EQUAL.text) {
                    backgroundResource = R.drawable.flatbutton_accent
                    onClick {
                        executeCalc(this@button, CalcType.EQUAL)
                        calcView.text = DecimalFormat("#,###,###,###,##0.##########").format(calcResult.toDouble())
                        numberValueTemp = calcResult.toPlainString()
                    }
                }.lparams {
                    initLayoutParams(this, buttonSize, buttonSize, rowIndex, columnIndex++)
                }
            }
//            verticalLayout {
//                padding = dip(32)
//
//                imageView(android.R.drawable.ic_menu_edit).lparams {
//                    margin = dip(16)
//                    gravity = Gravity.CENTER
//                }
//                val name = editText {
//                    hint = "名前を入れて下さい"
//                }
//                val password = editText {
//                    hint = "パスワードを入れて下さい"
//                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                }
//                button("Log in") {
//                    textColor = ContextCompat.getColor(ctx, android.R.color.white)
//                    backgroundColor = ContextCompat.getColor(ctx, android.R.color.holo_blue_light)
//                    onClick {
//                        //ui.owner.tryLogin(ui, name.text, password.text)
//                    }
//                }.lparams(width = dip(160), height = dip(80)) {
//                    topMargin = dip(32)
//                    gravity = Gravity.CENTER
//                }
//            }
        }
    }

    override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
        return object : MultiFloatWindowViewFactory(multiWindowContext) {

            val view by lazy {
                CalcUi().createView(AnkoContext.Companion.create(sharedContext!!, this@FloatWindowCalcApplication, setContentView = false))
//                UI {
//                    verticalLayout {
//                        textView {
//                            text = "Hello, Anko!"
//                        }
//                    }
//                }.view
            }

            val mMinimizedView by lazy {
                val imageView = ImageView(sharedContext)
                imageView.setImageResource(R.mipmap.ic_launcher)
                imageView
            }

            override fun createWindowView(arg: Int): View {
                return view
            }
            override fun createMinimizedView(arg: Int): View {
                mMinimizedView.isFocusableInTouchMode = true
                mMinimizedView.isFocusable = true
                return mMinimizedView
            }
            override fun start(intent: Intent?) {

            }
            override fun update(intent: Intent?, index: Int, positionName: String) {
                if(positionName == MultiWindowUpdatePosition.FIRST.name
                        || positionName == MultiWindowUpdatePosition.INDEX.name) {
                    start(intent)
                }
            }
            override fun onActive() {
                //Toast.makeText(applicationContext, "onActive", Toast.LENGTH_SHORT).show()
            }
            override fun onDeActive() {
                //Toast.makeText(applicationContext, "onDeActive", Toast.LENGTH_SHORT).show()
            }
            override fun onDeActiveAll() {
                //Toast.makeText(applicationContext, "onDeActiveAll", Toast.LENGTH_SHORT).show()
            }
            override fun onChangeMiniMode() {
                //Toast.makeText(applicationContext, "onChangeMiniMode", Toast.LENGTH_SHORT).show()
            }
            override fun onChangeWindowMode() {
                //Toast.makeText(applicationContext, "onChangeWindowMode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
        return object : MultiFloatWindowSettingsFactory(multiWindowContext) {
            override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
                return MultiFloatWindowInitSettings(
                        width = getDimensionPixelSize(R.dimen.width),
                        height = getDimensionPixelSize(R.dimen.height),
                        theme = MultiFloatWindowConstants.Theme.Dark,
                        anchor = MultiFloatWindowConstants.Anchor.Edge
                )
            }
        }
    }
}