package jp.kght6123.floating.window.framework

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.Toast
import org.jetbrains.anko.*

@SuppressLint("SetTextI18n")
class FloatWindowHelloApplication : FloatWindowApplication() {
    /**
     * Ankoでウィンドウのレイアウトを定義
     */
    class HelloUi: AnkoComponent<FloatWindowHelloApplication> {
        override fun createView(ui: AnkoContext<FloatWindowHelloApplication>) = with(ui) {
            verticalLayout {
                textView {
                    text = "Hello, Floating Window!!"
                }
            }
        }
    }
    /**
     * Ankoで最小化時のアイコンのレイアウトを定義
     */
    class HelloMiniUi: AnkoComponent<FloatWindowHelloApplication> {
        override fun createView(ui: AnkoContext<FloatWindowHelloApplication>) = with(ui) {
            imageView {
                imageResource = R.mipmap.ic_launcher
                isFocusableInTouchMode = true
                isFocusable = true
            }
        }
    }
    /**
     * メインウィンドウのファクトリークラス（MultiFloatWindowViewFactory）を実装して返す。
     */
    override fun onCreateFactory(index: Int): MultiFloatWindowViewFactory {
        return object : MultiFloatWindowViewFactory(multiWindowContext) {
            /**
             * メインウィンドウに表示するViewを生成し、Viewにイベントや初期値を設定して返してください。
             * 引数のindexは、0から始まる生成するウィンドウの一意の番号です。
             */
            override fun createWindowView(arg: Int): View {
                return HelloUi().createView(AnkoContext.Companion.create(sharedContext!!, this@FloatWindowHelloApplication, setContentView = false))
            }
            /**
             * ImageViewなどを生成し、ImageViewに最小化時のアイコン画像を設定して返してください。
             * アイコン画像は75dp×75dpで表示されます。
             */
            override fun createMinimizedView(arg: Int): View {
                return HelloMiniUi().createView(AnkoContext.Companion.create(sharedContext!!, this@FloatWindowHelloApplication, setContentView = false))
            }
            /**
             * 起動時に設定されたIntent情報を元に、初期化する処理を実装してください。
             */
            override fun start(intent: Intent?) {
                Toast.makeText(applicationContext, "start", Toast.LENGTH_SHORT).show()
            }
            /**
             * 更新時に設定されたIntent情報を元に、初期化する処理を実装してください。
             * positionNameはMultiWindowUpdatePositionの名称で、更新方法によって変化します。
             */
            override fun update(intent: Intent?, index: Int, positionName: String) {
                Toast.makeText(applicationContext, "update", Toast.LENGTH_SHORT).show()
            }
            /**
             * ウィンドウのイベント発生時に実行されるイベントメソッドです。
             */
            override fun onActive() {
                Toast.makeText(applicationContext, "onActive", Toast.LENGTH_SHORT).show()
            }
            override fun onDeActive() {
                Toast.makeText(applicationContext, "onDeActive", Toast.LENGTH_SHORT).show()
            }
            override fun onDeActiveAll() {
                Toast.makeText(applicationContext, "onDeActiveAll", Toast.LENGTH_SHORT).show()
            }
            override fun onChangeMiniMode() {
                Toast.makeText(applicationContext, "onChangeMiniMode", Toast.LENGTH_SHORT).show()
            }
            override fun onChangeWindowMode() {
                Toast.makeText(applicationContext, "onChangeWindowMode", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * ウィンドウの初期設定クラス（MultiFloatWindowInitSettings）のコンストラクタに値を設定して返してください。
     */
    override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
        return object : MultiFloatWindowSettingsFactory(multiWindowContext) {
            override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
                return MultiFloatWindowInitSettings(
                        width = getDimensionPixelSize(R.dimen.width),// dp単位の指定を推奨
                        height = getDimensionPixelSize(R.dimen.height),// dp単位の指定を推奨
                        theme = MultiFloatWindowConstants.Theme.Light,// `Light`または`Dark`
                        anchor = MultiFloatWindowConstants.Anchor.Edge// `Edge`または`SinglePoint`
                )
            }
        }
    }
}