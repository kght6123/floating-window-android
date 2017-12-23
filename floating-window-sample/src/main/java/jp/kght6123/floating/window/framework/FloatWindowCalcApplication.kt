package jp.kght6123.floating.window.framework

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import org.jetbrains.anko.*

/**
 * サンプルプラグインアプリケーション
 *
 * @author    kght6123
 * @copyright 2017/12/17 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class FloatWindowCalcApplication : FloatWindowApplication() {

    private val tag = FloatWindowCalcApplication::class.java.name

    class CalcUi: AnkoComponent<FloatWindowCalcApplication> {

        override fun createView(ui: AnkoContext<FloatWindowCalcApplication>) = with(ui) {
            val buttonSize = 48

            gridLayout {
                rowCount = 6
                columnCount = 4
                lparams {
                    width = GridLayout.LayoutParams.MATCH_PARENT
                    height = GridLayout.LayoutParams.MATCH_PARENT
                }

                var rowIndex = 0
                var columnIndex = 0

                textView("11+22+33+444444444444444") {
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
                }.lparams {
                    width = GridLayout.LayoutParams.MATCH_PARENT
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex, columnCount)
                }
                rowIndex++

                button("AC") {
                    backgroundResource = R.drawable.flatbutton_gray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("+/-") {
                    backgroundResource = R.drawable.flatbutton_gray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("%") {
                    backgroundResource = R.drawable.flatbutton_gray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("÷") {
                    backgroundResource = R.drawable.flatbutton_accent
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("1") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("2") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("3") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("X") {
                    backgroundResource = R.drawable.flatbutton_accent
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("4") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("5") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("6") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("-") {
                    backgroundResource = R.drawable.flatbutton_accent
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("7") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("8") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("9") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("+") {
                    backgroundResource = R.drawable.flatbutton_accent
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                rowIndex++
                columnIndex = 0

                button("0") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize*2)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++, columnCount-2)
                }
                columnIndex++

                button(".") {
                    backgroundResource = R.drawable.flatbutton_lightgray
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
                }
                button("=") {
                    backgroundResource = R.drawable.flatbutton_accent
                }.lparams {
                    width = dip(buttonSize)
                    height = dip(buttonSize)
                    rowSpec = GridLayout.spec(rowIndex)
                    columnSpec = GridLayout.spec(columnIndex++)
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
                        getDimensionPixelSize(R.dimen.x),
                        getDimensionPixelSize(R.dimen.y),
                        getDimensionPixelSize(R.dimen.width),
                        getDimensionPixelSize(R.dimen.height),
                        MultiFloatWindowConstants.Theme.Dark,
                        MultiFloatWindowConstants.Anchor.Edge
                )
            }
        }
    }
}