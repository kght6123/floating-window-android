package jp.kght6123.multiwindow.recycler

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import jp.kght6123.multiwindow.R

/**
 * アプリ一覧を作成するアダプタークラス
 *
 * Created by kght6123 on 2017/07/28.
 */
class IconAppListRecyclerAdapter(val context: Context) : RecyclerView.Adapter<IconAppListRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.multiwindow_thumbnail_icon, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        //val bitmap = manager.getThumb(position)
        //holder!!.thumbImageView.setOnClickListener({
        //    manager.changeActive(position)
        //    notifyDataSetChanged()
        //})
        //holder.thumbImageView.setImageBitmap(bitmap)
    }

    override fun getItemCount(): Int {
        return 10
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val thumbImageView : ImageView by lazy {
            itemView?.findViewById(R.id.thumbIconImageView) as ImageView
        }
        init {
            thumbImageView
        }
    }
}