package jp.kght6123.smallappbrowser.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import jp.kght6123.multiwindow.utils.UnitUtils

/**
 * HTTP送信のIntentを受け取ってブラウザを選択させるActivity、ブラウザチェンジャー
 *
 * Created by kght6123 on 2017/08/04.
 */
class BrowserChangerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ブラウザアプリ一覧を取得する
        val pm = this.packageManager

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = this.intent.data

        // カテゴリとアクションに一致するアクティビティの情報を取得する
        val items :List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        val adapter : ResolveInfoListAdapter = ResolveInfoListAdapter(this, objects = items.toMutableList())

        // リストダイアログを表示する
        // 選択→そのブラウザで表示、長押し選択→ブラウザのアプリ設定
        val builder : AlertDialog.Builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog)
        builder.setCustomTitle(null)
                .setAdapter(adapter) { _, which ->
                    val info :ResolveInfo = items[which]
                    val intent2 :Intent = Intent ()
                    intent2.setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    intent2.action = Intent.ACTION_VIEW
                    intent2.addCategory(Intent.CATEGORY_DEFAULT)
                    intent2.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent2.data = this@BrowserChangerActivity.intent.data
                    startActivity(intent2)
                    finish()
                }.setOnDismissListener {
                    finish()
                }.setOnCancelListener {
                    finish()
                }

        //builder.show()

        val dialog :AlertDialog = builder.create()
        val param : WindowManager.LayoutParams = dialog.window.attributes
        param.gravity = Gravity.BOTTOM
        //param.y=50    //中心から下方向に50pxずらす
        dialog.window.attributes = param
        dialog.show()
    }
}
/**
 * アイコン付き用リストのアダプター
 */
class ResolveInfoListAdapter(val activity: Activity, resource: Int = android.R.layout.select_dialog_item, objects: MutableList<ResolveInfo>?)
    : ArrayAdapter<ResolveInfo>(activity, resource, objects) {

    val TAG :String = this.javaClass.name
    val packageManager :PackageManager? by lazy { activity.packageManager }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val item: ResolveInfo = this.getItem(position)
        val title = item.loadLabel(this.packageManager).toString()

        val icon: Drawable = item.loadIcon(this.packageManager)
        // 元々の画像サイズを指定
        // icon.setBounds(0, 0, (icon.intrinsicWidth / 1.25f).toInt(), (icon.intrinsicHeight / 1.25f).toInt())
        icon.setBounds(0, 0, UnitUtils.convertDp2Px(35f, activity).toInt(), UnitUtils.convertDp2Px(35f, activity).toInt())

        val v: View = super.getView(position, convertView, parent)
        val tv: TextView = v.findViewById(android.R.id.text1) as TextView

        Log.d(TAG, item.activityInfo.name)
//        if(item.activityInfo.name == BrowserChangerActivity::class.java.name)
//            v.visibility = View.GONE    // 自アプリは非表示に

        //Put the image on the TextView
        tv.text = title
        tv.setCompoundDrawables/*WithIntrinsicBounds*/(icon, null, null, null)
        //tv.setTextColor(this.context.getResources().getColorStateList(android.R.color.secondary_text_light))
        tv.setTextColor(0xFF666666.toInt())
        tv.textSize = 6 * this.activity.resources.displayMetrics.density + 0.5f

        //Add margin between image and text (support various screen densities)
        val dp5: Int = (9 * this.activity.resources.displayMetrics.density + 0.5f).toInt()
        tv.compoundDrawablePadding = dp5

        tv.setOnLongClickListener{ _ ->
            val intent = Intent()
            intent.data = Uri.fromParts("package",item.activityInfo.packageName,null)
            intent.component = ComponentName.unflattenFromString("com.android.settings/.applications.InstalledAppDetails")
            activity.startActivity(intent)
            activity.finish()
            return@setOnLongClickListener true
        }
        return v
    }
}