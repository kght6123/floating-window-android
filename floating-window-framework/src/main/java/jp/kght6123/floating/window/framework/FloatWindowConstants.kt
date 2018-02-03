package jp.kght6123.floating.window.framework

import android.content.pm.ResolveInfo
import android.os.Build
import android.view.WindowManager

/**
 * 定数クラス
 *
 * @author    kght6123
 * @copyright 2017/08/01 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
class MultiFloatWindowConstants {
    enum class Theme(val rgb: Int) {
        Dark(0x000000),
        Light(0xFFFFFF),
    }
    enum class Anchor {
        Edge,
        SinglePoint,
    }
    companion object {
        const val APP_WIDGET_HOST_ID: Int = 798756856

        const val PERMISSION_MULTI_WINDOW_APPS = "jp.kght6123.floating.window.core.manifest.permission.APPS"
        const val ACTION_MULTI_WINDOW_MAIN = "jp.kght6123.floating.window.core.intent.action.MAIN"
        const val CATEGORY_MULTI_WINDOW_LAUNCHER = "jp.kght6123.floating.window.core.intent.category.LAUNCHER"

        const val PACKAGE_NAME_CORE = "jp.kght6123.floating.window.core"
        const val ACTION_SERVICE_CORE = "jp.kght6123.floating.window.core.FloatWindowService.ACTION"

        val API_VERSION = MultiWindowApiVersion.DEV_1

        @Suppress("DEPRECATION")
        val WINDOW_MANAGER_OVERLAY_TYPE =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    }
}

enum class MultiWindowApiVersion {
    DEV_1,
}

/**
 * MultiFloatWindowInitSettingsでデフォルト値やMetaData取得に利用
 */
enum class MultiWindowMetaDataName(val default: Any) {
    WINDOW_MODE(MultiFloatWindowConstants.Anchor.Edge.name),
    WINDOW_BORDER_DP_WIDTH(24f),
    WINDOW_BORDER_OPACITY(0.25f),
    WINDOW_BORDER_TOUCH_OPACITY(0.75f),
    WINDOW_THEME(MultiFloatWindowConstants.Theme.Light.name),
    WINDOW_RESIZE(true),
    WINDOW_DP_MIN_WIDTH(150f),
    WINDOW_DP_MIN_HEIGHT(150f),
    WINDOW_DP_MAX_WIDTH(-1f),
    WINDOW_DP_MAX_HEIGHT(-1f),
    ;
    fun getIntValue(): Int {
        return this.default as Int
    }
    fun getFloatValue(): Float {
        return this.default as Float
    }
    fun getBooleanValue(): Boolean {
        return this.default as Boolean
    }
    fun getStringValue(): String {
        return this.default as String
    }
    companion object {
        fun getDefaultMap(): Map<String, Any> {
            val paramMap = hashMapOf<String, Any>()
            for (metaDataName in MultiWindowMetaDataName.values()) {
                paramMap[metaDataName.name] = metaDataName.default
            }
            return paramMap
        }
        fun getParamMap(resolveInfo: ResolveInfo): HashMap<String, Any> {
            val paramMap = hashMapOf<String, Any>()
            val metaData = resolveInfo.serviceInfo.metaData
            if(metaData != null) {
                for (metaDataName in MultiWindowMetaDataName.values()) {
                    when {
                        metaDataName.default is String ->
                            paramMap[metaDataName.name] = metaData.getString(metaDataName.name, metaDataName.getStringValue())
                        metaDataName.default is Int ->
                            paramMap[metaDataName.name] = metaData.getInt(metaDataName.name, metaDataName.getIntValue())
                        metaDataName.default is Float ->
                            paramMap[metaDataName.name] = metaData.getFloat(metaDataName.name, metaDataName.getFloatValue())
                        metaDataName.default is Boolean ->
                            paramMap[metaDataName.name] = metaData.getBoolean(metaDataName.name, metaDataName.getBooleanValue())
                    }
                }
            }
            return paramMap
        }
    }
}

enum class MultiWindowControlCommand {
    HELLO,
    START,
    OPEN,
    //ADD_REMOTE_VIEWS,
    UPDATE,
    NEXT_INDEX,
    PREV_INDEX,
    ADD_APP_WIDGET,
    CLOSE,
    EXIT,
}

enum class MultiWindowControlParam {
    WINDOW_INDEX,
    WINDOW_CLASS_NAMES,
    //REMOTE_WINDOW_VIEWS,
    //REMOTE_MINI_VIEWS,
    APP_PACKAGE_NAME,
    APP_SERVICE_CLASS_NAME,
}

enum class MultiWindowOpenType {
    UPDATE,
    NEW,
}

enum class MultiWindowUpdatePosition {
    INDEX,
    FIRST,
    LAST,
    MIDDLE
}