
# **Floting Multiple Window Framework α1**

**現在は、テストが十分ではない実験的なリリースです。多くのバグを含む可能性があります。**

android（ルート権限不要）でフローティングウィンドウを実現し、片手操作に最適化したライブラリです。

よく似たフレームワークは、Xperiaの*Small App API*ですが、より多くの端末で動作します。

言語は*Kotlin*を採用しています。

## **Description**
容易にマルチウィンドウのアプリを作成し、追加することが出来ます。
ウィンドウの操作は片手に最適化し、ウィンドウの拡大／縮小、移動は四隅の縁に対しての操作で行います。

小窓でフレームワークに実装したアプリを表示することが出来ます。
アプリ一覧やアプリ履歴はコアに含まれるランチャーから呼び出しします。
Activityから特定のマルチウィンドウアプリを呼び出したりことが出来ます。

Coreアプリ（マルチウィンドウ機能）と、フレームワーク（アプリ追加用）にモジュールが分かれています。

## **Demo**
サンプルアプリケーションのデモ動画です

[![Sample](https://img.youtube.com/vi/uGvzgPG2nSM/0.jpg)](https://www.youtube.com/watch?v=uGvzgPG2nSM)


## **Screen Shot**
サンプルアプリケーション操作中のスクリーンショットです

1. ランチャー起動
    * 「FloatWindow」アイコンで起動します。通知を選択すると終了します。
![ランチャー起動](screenshot/screen-01.png "ランチャー起動")

1. アプリ一覧表示
    * ランチャーをダブルタップしてアプリ一覧表示、ロングタップでランチャー表示に戻ります。
    * アプリ一覧の上はインストール済みのアプリ一覧
    * アプリ一覧の下はアプリ履歴
![アプリ一覧表示](screenshot/screen-03.png "アプリ一覧表示")

1. サンプルのブラウザ起動
    * アプリ一覧のインストールされているアプリアイコンを選択すると起動。
![サンプルブラウザ起動](screenshot/screen-02.png "サンプルブラウザ起動")

1. 非アクティブ
    * 裏側が操作できます。
    * オレンジのランチャーを選択すると、再度アクティブになります。
![非アクティブ](screenshot/screen-04.png "非アクティブ")

1. ウィジェット
    * 「FloatWidget」を選択すると、ウィジェットも追加できます。
![ウィジェット](screenshot/screen-05.png "ウィジェット")

## **特徴**
このフレームワークの基本的な特徴になります
1. Xperia以外の端末で動作
1. マルチウィンドウで動作（同じアプリを複数開ける）
1. 片手操作への最適化（ウィンドウの拡大縮小、移動）
1. 端末の領域外に移動しても、最小化しない
1. 閉じるボタンなどの固定ボタンを廃止し、画面を広く使える（四隅のフチで操作）
1. 四隅のフチはスライドするとウィンドウの移動、長押し後にスライドするとウィンドウの拡大縮小（角は近接する2辺の拡大縮小）
1. 四隅のフチはダブルタップで最小化、上の角は最大化、下の角は最小化
1. Kotlinで実装

## **制約**
非システムアプリの為、利用しているWindowManager（`TYPE_SYSTEM_ALERT`、`TYPE_APPLICATION_OVERLAY`）に制限があり、幾つかの機能に制約があります。
1. ウィンドウが全て非アクティブになった場合、ランチャーを選択しないと復帰できない。
1. コア機能を持つアプリのインストールが必須
1. 「他のアプリの上に重ねて描画」の許可が必要
1. layoutにincludeタグが使えない

## **Requirement**
* Android 6.0 marshmallow 以上
* マルチウィンドウ機能の利用について
    - コアのインストール必須
* アプリ追加について
    - フレームワーク必須

## **Development environment**
* Android
    - Android Emulator 6.0〜8.0 
    - Galaxy Note8

* Develop Machine
    - macOS High Sierra 10.13
    - Android Studio 3.0

## **How to Build**
「floating-window」リポジトリをcloneして、AndroidStudioにインポート。

デバッグ実行時は「floating-window-core」と「floating-window-sample」を実行。

## **Install**
現状はαリリースのため、Coreはデバッグ向けと署名なしAPKを公開し、
FrameworkライブラリはGitPagesの仮Mavenリポジトリで公開。

* Core
    * [デバッグAPK](./download/floating-window-core-debug.apk)
    * [リリース署名なしAPK](./download/floating-window-core-release-unsigned.apk)

* Framework
    
    build.gradleに下記のリポジトリとライブラリを追加してください。

    ```gradle
    repositories {
        maven { url 'http://kght6123.github.io/maven-repositories/android' }
    }
    dependencies {
        compile 'jp.kght6123.floating.window:floating-window-framework:0.0.1'
    }
    ```

## **Usage**
全体像はsampleモジュールを参考にしてください。
1. FloatWindowApplicationを実装するクラスを作成
    1. `onCreateFactory(index: Int): MultiFloatWindowViewFactory`メソッドを実装
        * MultiFloatWindowViewFactoryを実装し、クラスを初期化して返してください。
        * 下記の４メソッドを実装します。

        1. `createWindowView(arg: Int): View`メソッド
            * `createContentView`でViewを生成し、Viewにイベントや初期値を設定して返してください。
            * 引数のindexは0から始まる生成するウィンドウのインデックス番号です。

        1. `createMinimizedView(arg: Int): View`メソッド
            * ImageViewなどを生成し、ImageViewに最小化時のアイコン画像を設定して返してください。
            * アイコン画像は75dp×75dpで表示されます。

        1. `start(intent: Intent?)`メソッドを実装
            * 起動時に設定されたIntent情報を元に初期化する処理を実装してください。

        1. `update(intent: Intent?, index: Int, positionName: String)`メソッドを実装
            * 更新時に設定されたIntent情報を元に初期化する処理を実装してください。
            * positionNameはMultiWindowUpdatePositionの名称です。

    1. `onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory`メソッドを実装
        * MultiFloatWindowInitSettingsを初期化して、ウィンドウの初期設定（位置、サイズ）などを設定して返してください。
        ```kotlin
        override fun onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory {
            return object : MultiFloatWindowSettingsFactory(multiWindowContext) {
                override fun createInitSettings(arg: Int): MultiFloatWindowInitSettings {
                    return MultiFloatWindowInitSettings(
                            getDimensionPixelSize(R.dimen.x),
                            getDimensionPixelSize(R.dimen.y),
                            getDimensionPixelSize(R.dimen.width),
                            getDimensionPixelSize(R.dimen.height),
                            getColor(android.R.color.background_light)
                    )
                }
            }
        }
        ```


1. AndroidManifest.xmlの修正
    1. manifestタグに属性を追加
        * `android:sharedUserId="jp.kght6123"`

    1. uses-permissionタグ追加
        * `<uses-permission android:name="jp.kght6123.floating.window.core.manifest.permission.APPS" />`

    1. serviceタグ追加
        * ServiceクラスはFloatWindowApplicationクラスを継承して作成したクラスを指定
        * `android:icon`属性を指定してください、ランチャーのアイコンになります。
        * `android:exported="true"`を追加してください
    
    1. serviceタグ内にintent-filterを追加
        ```xml
        <intent-filter>
		    <action android:name="jp.kght6123.floating.window.core.intent.action.MAIN" />
		    <category android:name="jp.kght6123.floating.window.core.intent.category.LAUNCHER" />
	    </intent-filter>
        ```

## **Contribution**
Licenceに「Apache License Version 2.0」を選択しており、修正いただいた場合は「Pull Request」をお願いします。
1. Fork ([https://github.com/tcnksm/tool/fork](https://github.com/tcnksm/tool/fork))
2. Create a feature branch
3. Commit your changes
4. Rebase your local changes against the master branch
5. Run and Test
6. Create new Pull Request

## **Licence**
* [**Apache License Version 2.0, January 2004**](./LICENSE)

## **Author**
* [**@kght6123**](https://twitter.com/kght6123)

## **Contacts**
公開内容の詳細に関しては[**@kght6123**](https://twitter.com/kght6123)まで、お気軽にお問い合わせ下さい。

## **Copyright**
**```Copyright (c) 2017 Hirotaka Koga```**
