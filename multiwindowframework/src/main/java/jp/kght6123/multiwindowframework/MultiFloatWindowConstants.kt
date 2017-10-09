package jp.kght6123.multiwindowframework

/**
 * 定数クラス
 *
 * Created by kght6123 on 2017/08/01.
 */
class MultiFloatWindowConstants {
    enum class Theme(val rgb: Int) {
        Dark(0x000000),
        Light(0xFFFFFF),
    }

    companion object {
        val APP_WIDGET_HOST_ID: Int = 798756856

        val PERMISSION_MULTI_WINDOW_APPS = "jp.kght6123.multiwindow.manifest.permission.APPS"
        val ACTION_MULTI_WINDOW_MAIN = "jp.kght6123.multiwindow.intent.action.MAIN"
        val CATEGORY_MULTI_WINDOW_LAUNCHER = "jp.kght6123.multiwindow.intent.category.LAUNCHER"
    }
}

enum class MultiWindowControlCommand {
    HELLO,
    START,
    OPEN,
    //ADD_REMOTE_VIEWS,
    ADD_APP_WIDGET,
    CLOSE,
    EXIT,
}

enum class MultiWindowControlParam {
    WINDOW_INDEX,
    REMOTE_WINDOW_VIEWS,
    REMOTE_MINI_VIEWS,
    APP_PACKAGE_NAME,
    APP_SERVICE_CLASS_NAME,
}

enum class MultiWindowOpenType {
    UPDATE,
    NEW,
}