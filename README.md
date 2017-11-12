
# **Floting Multiple Window Framework α1**
====

**現在は、実験的なリリースです。多くのバグを含む可能性が高いです。**

android（ルート権限不要）でフローティングウィンドウを実現し、片手操作に最適化したライブラリです。

よく似たフレームワークは、Xperiaの*Small App API*ですが、より多くの端末で動作します。

言語は*Kotlin*を採用しています。

## **Description**
容易にマルチウィンドウのアプリを作成し、追加することが出来ます。
ウィンドウの操作は片手に最適化し、ウィンドウの拡大／縮小、移動は四隅の縁に対しての操作で行います。

小窓でフレームワークに実装したアプリを表示することが出来ます。
アプリ一覧やアプリ履歴はコアに含まれるランチャーから呼び出しします。
Activityから特定のマルチウィンドウアプリを呼び出したりことが出来ます。

Coreアプリ（マルチウィンドウ機能）と、フレームワーク（アプリ追加用）にモジュールが分かれています。

## **Demo**
サンプルアプリケーションのデモになります
FIXME アニメGIFでサンプルが動作しているところを表示

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
1. 「他のアプリの上に重ねて描画」の許可が必要
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

## **Install**
* **インストール用のライブラリ（aar）は準備中。**

## **Usage**
全体像はsampleモジュールを参考にしてください。
1. MultiFloatWindowApplicationを実装するクラスを作成
    1. `onCreateFactory(index: Int): MultiFloatWindowViewFactory`メソッドを実装
        * MultiFloatWindowViewFactoryを実装し、クラスを初期化して返してください。
        * 下記の４メソッドを実装します。

        1. `createWindowView(arg: Int): View`メソッド
            * `createContentView`でViewを生成し、Viewにイベントや初期値を設定して返してください。
            * 引数のindexは0から始まる生成するウィンドウのインデックス番号です。

        1. `createMinimizedView(arg: Int): View`メソッド
            * ImageViewなどを生成し、ImageViewに最小化時のアイコン画像を設定して返してください。
            * アイコン画像は75dp×75dpで表示されます。

        1. `start(intent: Intent?)`メソッドを実装
            * 起動時に設定されたIntent情報を元に初期化する処理を実装してください。

        1. `update(intent: Intent?, index: Int, positionName: String)`メソッドを実装
            * 更新時に設定されたIntent情報を元に初期化する処理を実装してください。
            * positionNameはMultiWindowUpdatePositionの名称です。

    1. `onCreateSettingsFactory(index: Int): MultiFloatWindowSettingsFactory`メソッドを実装
        * MultiFloatWindowInitSettingsを初期化して、ウィンドウの初期設定（位置、サイズ）などを設定して返してください。


1. AndroidManifest.xmlの修正
    1. manifestタグに属性を追加
        * `android:sharedUserId="jp.kght6123"`

    1. uses-permissionタグ追加
        * `<uses-permission android:name="jp.kght6123.multiwindow.manifest.permission.APPS" />`

    1. serviceタグ追加
        * ServiceクラスはMultiFloatWindowApplicationクラスを継承して作成したクラスを指定
        * `android:icon`属性を指定してください、ランチャーのアイコンになります。
        * `android:exported="true"`を追加してください
    
    1. serviceタグ内にintent-filterを追加
        ```xml
        <intent-filter>
		    <action android:name="jp.kght6123.multiwindow.intent.action.MAIN" />
		    <category android:name="jp.kght6123.multiwindow.intent.category.LAUNCHER" />
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
