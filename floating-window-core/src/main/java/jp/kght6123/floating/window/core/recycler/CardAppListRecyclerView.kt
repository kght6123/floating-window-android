package jp.kght6123.floating.window.core.recycler

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager
import jp.kght6123.floating.window.core.FloatWindowManager

@SuppressLint("ViewConstructor")
/**
 * アプリ一覧を表示するリサイクルビュー
 *
 * @author    kght6123
 * @copyright 2017/07/29 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class CardAppListRecyclerView(context: Context?, manager: FloatWindowManager) : RecyclerView(context) {
    init {
        layoutManager = LinearLayoutManager(context)
        adapter = CardAppListRecyclerAdapter(context!!, manager)
    }
}