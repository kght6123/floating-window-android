package jp.kght6123.multiwindow.recycler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.kght6123.multiwindow.MultiFloatWindowManager
import jp.kght6123.multiwindow.R
import jp.kght6123.multiwindowframework.MultiFloatWindowConstants


/**
 * アプリ一覧を作成するアダプタークラス
 *
 * Created by kght6123 on 2017/07/28.
 */
class IconAppListRecyclerAdapter(val context: Context, private val manager: MultiFloatWindowManager) : RecyclerView.Adapter<IconAppListRecyclerAdapter.ViewHolder>() {

    private val resolveInfoAllList: MutableList<ResolveInfo> = mutableListOf()
    private val packageManager by lazy { context.packageManager }

    init {
        val packageInfoList =
                packageManager.getPackagesHoldingPermissions(arrayOf(MultiFloatWindowConstants.PERMISSION_MULTI_WINDOW_APPS), PackageManager.GET_SERVICES)

        for (packageInfo in packageInfoList) {
            // packageInfo.servicesではIntent-Filterで絞り込めないので、絞り込む
            val intent = Intent(MultiFloatWindowConstants.ACTION_MULTI_WINDOW_MAIN)
            intent.addCategory(MultiFloatWindowConstants.CATEGORY_MULTI_WINDOW_LAUNCHER)
            intent.`package` = packageInfo.packageName

            val resolveInfoList: MutableList<ResolveInfo> =
                    packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA)

            resolveInfoAllList.addAll(resolveInfoList)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.multiwindow_thumbnail_icon, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val resolveInfo = resolveInfoAllList[position]
        holder?.thumbIconButton?.setOnClickListener {
            // 選択されたアプリを開く（次インデックス）
            val nextIndex = manager.nextIndex()
            if(manager.factoryMap.containsKey(nextIndex))
                manager.openWindow(nextIndex, resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name, true)
            else
                manager.openWindow(nextIndex, resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name, false)

            manager.factoryMap.getValue(nextIndex).start(null)

            // FIXME 今後、MetaDataでフレームワークの動きを設定したい
            //val bundle = resolveInfo.serviceInfo.metaData
            //val value = getResources().getString(bundle.getInt("your.key"))
        }
        holder?.thumbLabel?.text = resolveInfo.loadLabel(packageManager)

        holder?.thumbIconButton?.setImageDrawable(null)  // 解放（BitmapをImageViewに設定するとき）
        holder?.thumbIconButton?.setImageDrawable(resolveInfo.loadIcon(packageManager))
    }

    override fun getItemCount(): Int {
        return resolveInfoAllList.size
    }

    class ViewHolder(private val thumbIconView: View?) : RecyclerView.ViewHolder(thumbIconView) {
        val thumbIconButton : ImageView by lazy {
            thumbIconView?.findViewById(R.id.thumbIconButton) as ImageView
        }
        val thumbLabel : TextView by lazy {
            thumbIconView?.findViewById(R.id.thumbLabel) as TextView
        }
        init {
            thumbIconButton
            thumbLabel
        }
    }
}