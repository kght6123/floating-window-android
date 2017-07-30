package jp.kght6123.multiwindow.recycler

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import jp.kght6123.multiwindow.MultiFloatWindowManager

@SuppressLint("ViewConstructor")
/**
 * Created by kogahirotaka on 2017/07/29.
 */
class CardAppListRecyclerView(context: Context?, manager: MultiFloatWindowManager) : RecyclerView(context) {
    init {
        layoutManager = LinearLayoutManager(context)
        adapter = CardAppListRecyclerAdapter(context!!, manager)
    }
}