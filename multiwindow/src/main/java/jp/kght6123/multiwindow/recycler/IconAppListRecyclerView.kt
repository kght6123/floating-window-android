package jp.kght6123.multiwindow.recycler

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import jp.kght6123.multiwindow.MultiFloatWindowManager

@SuppressLint("ViewConstructor")
/**
 * アプリ一覧を表示するリサイクルビュー
 *
 * Created by kght6123 on 2017/07/29.
 */
class IconAppListRecyclerView(context: Context?, windowManager: MultiFloatWindowManager) : RecyclerView(context) {
    init {
        val manager = LinearLayoutManager(context)
        manager.orientation = LinearLayoutManager.HORIZONTAL

        this.layoutManager = manager
        this.adapter = context?.let { IconAppListRecyclerAdapter(it, windowManager) }
    }
}