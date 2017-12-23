package jp.kght6123.floating.window.framework

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
        val APP_WIDGET_HOST_ID: Int = 798756856

        val PERMISSION_MULTI_WINDOW_APPS = "jp.kght6123.floating.window.core.manifest.permission.APPS"
        val ACTION_MULTI_WINDOW_MAIN = "jp.kght6123.floating.window.core.intent.action.MAIN"
        val CATEGORY_MULTI_WINDOW_LAUNCHER = "jp.kght6123.floating.window.core.intent.category.LAUNCHER"

        val PACKAGE_NAME_CORE = "jp.kght6123.floating.window.core"
        val ACTION_SERVICE_CORE = "jp.kght6123.floating.window.core.FloatWindowService.ACTION"

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