package jp.kght6123.smallappbrowser.application

import java.util.ArrayList
import java.util.Collections

import android.app.Application
import android.content.ClipData
import android.content.ClipData.Item
import android.graphics.Bitmap
import android.service.notification.StatusBarNotification
import android.util.Log

/**

 * @author Hirotaka
 */
class SharedDataApplication : Application() {
    private val TAG = SharedDataApplication::class.java.simpleName

    var statusBarNotifications: Array<StatusBarNotification>? = null
    //private List<StatusBarNotification> statusBarNotificationTempList = Collections.synchronizedList(new LinkedList<StatusBarNotification>());
    private var statusBarNotificationTemp: StatusBarNotification? = null
    private val clipDataList = Collections.synchronizedList(ArrayList<ClipData>())

    val webHistoryItemList = ArrayList<WebHistoryItem>()
    val webPinItemList = ArrayList<WebHistoryItem>()

    override fun onCreate() {
        /** Called when the Application-class is first created.  */
        Log.v(TAG, "--- onCreate() in ---")
        super.onCreate()
    }

    override fun onTerminate() {
        /** This Method Called when this Application finished.  */
        Log.v(TAG, "--- onTerminate() in ---")
        super.onTerminate()
    }

    fun popStatusBarNotification(): StatusBarNotification? {
        return this.statusBarNotificationTemp
        //return this.statusBarNotificationTempList.size() > 0 ? this.statusBarNotificationTempList.remove(0) : null;
    }

    fun pushStatusBarNotification(statusBarNotification: StatusBarNotification) {
        this.statusBarNotificationTemp = statusBarNotification
        //this.statusBarNotificationTempList.add(statusBarNotification);
    }

    fun putClipData(primaryClip: ClipData?) {
        if (primaryClip != null
                && primaryClip.itemCount > 0
                &&
                (primaryClip.getItemAt(0).text != null
                        || primaryClip.getItemAt(0).htmlText != null
                        || primaryClip.getItemAt(0).intent != null
                        || primaryClip.getItemAt(0).uri != null))
            this.clipDataList.add(primaryClip)
    }

    //			final int itemConut = clipData.getItemCount();
    //			for(int index = 0; index < itemConut; index++)
    //			{
    //				final Item item = clipData.getItemAt(index);
    //				clipDataItemList.add(new ClipDataItem(clipData, item));
    //			}
    val clipDataItemList: List<ClipDataItem>
        get() {
            val clipDataItemList = ArrayList<ClipDataItem>()

            var index = 1
            for (clipData in this.clipDataList) {
                clipDataItemList.add(ClipDataItem(index, clipData, clipData.getItemAt(0)))
                index++
            }
            return clipDataItemList
        }

    fun addWebHistoryItemList(item: WebHistoryItem) {
        webHistoryItemList.remove(item)
        webHistoryItemList.add(0, item)
    }

    fun addWebPinItemList(item: WebHistoryItem) {
        webPinItemList.remove(item)
        webPinItemList.add(0, item)
    }

    fun removeWebPinItemList(item: WebHistoryItem) {
        webPinItemList.remove(item)
    }

    inner class ClipDataItem(val index: Int, val clipData: ClipData, val item: Item)

    class WebHistoryItem(var url: String, var title: String, var favicon: Bitmap?) : Comparable<WebHistoryItem> {
        override fun compareTo(other: WebHistoryItem): Int {
            return this.url.compareTo(other.url)
        }
        override fun equals(other: Any?): Boolean {
            return this.compareTo((other as WebHistoryItem?)!!) == 0
        }
    }
}
