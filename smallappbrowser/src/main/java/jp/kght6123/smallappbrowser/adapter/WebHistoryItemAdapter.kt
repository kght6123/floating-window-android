package jp.kght6123.smallappbrowser.adapter

import java.util.ArrayList

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import jp.kght6123.smallappbrowser.R
import jp.kght6123.smallappbrowser.application.SharedDataApplication

class WebHistoryItemAdapter(
        private val webView :WebView,
        context :Context,
        textViewResourceId :Int,
        objects :ArrayList<SharedDataApplication.WebHistoryItem>,
        private val additionalHttpHeaders :Map<String, String>,
        private val history :Boolean
    ): ArrayAdapter<SharedDataApplication.WebHistoryItem>(context, textViewResourceId, objects) {

    private val sharedData:SharedDataApplication by lazy { this.context.getApplicationContext() as SharedDataApplication }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup):View {

        val item = getItem(position) as SharedDataApplication.WebHistoryItem
        val listItem = LayoutInflater.from(this.context).inflate(R.layout.smallapp_browser_listitem, null)
        run({
            val title = listItem.findViewById(R.id.webPageTitle) as TextView
            title.setText(item.title)

            val url = listItem.findViewById(R.id.webPageUrl) as TextView
            url.setText(item.url)

            listItem.setClickable(true)
            listItem.setOnClickListener(
                    object:View.OnClickListener {
                        @Synchronized public override fun onClick(v:View) {
                            this@WebHistoryItemAdapter.webView.loadUrl(item.url, additionalHttpHeaders)
                        }
                    })
            listItem.setLongClickable(true)

            if (this.history)
                listItem.setOnLongClickListener(
                        object:View.OnLongClickListener {
                            @Synchronized public override fun onLongClick(v:View):Boolean {
                                sharedData.addWebPinItemList(item)
                                Toast.makeText(this@WebHistoryItemAdapter.context, R.string.pin_completed, Toast.LENGTH_SHORT).show()
                                return true
                            }
                        })
            else
                listItem.setOnLongClickListener(
                        object:View.OnLongClickListener {
                            @Synchronized public override fun onLongClick(v:View):Boolean {
                                sharedData.removeWebPinItemList(item)
                                remove(item)
                                Toast.makeText(this@WebHistoryItemAdapter.context, R.string.pin_deleted, Toast.LENGTH_SHORT).show()
                                return true
                            }
                        })

        })
        return listItem
    }
}
