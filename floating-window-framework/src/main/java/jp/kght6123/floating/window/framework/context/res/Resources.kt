package jp.kght6123.floating.window.framework.context.res

import android.content.res.*
import android.graphics.Movie
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import java.io.InputStream

/**
 * Resourcesにアクセスする為のインターフェース
 *
 * @author    kght6123
 * @copyright 2017/11/04 Hirotaka Koga
 * @license   http://www.apache.org/licenses/LICENSE-2.0 Apache-2.0
 */
interface Resources {
    fun finishPreloading()
    fun flushLayoutCache()
    fun getAnimation(id: Int): XmlResourceParser
    fun getAssets(): AssetManager
    fun getBoolean(id: Int): Boolean
    fun getColor(id: Int, theme: android.content.res.Resources.Theme): Int
    fun getColorStateList(id: Int, theme: android.content.res.Resources.Theme): ColorStateList
    fun getConfiguration(): Configuration
    fun getDimension(id: Int): Float
    fun getDimensionPixelOffset(id: Int): Int
    fun getDimensionPixelSize(id: Int): Int
    fun getDisplayMetrics(): DisplayMetrics
    fun getDrawable(id: Int, theme: android.content.res.Resources.Theme): Drawable
    fun getDrawableForDensity(id: Int, density: Int, theme: android.content.res.Resources.Theme): Drawable
    fun getFraction(id: Int, base: Int, pbase: Int): Float
    fun getIdentifier(name: String, defType: String, defPackage: String): Int
    fun getIntArray(id: Int): IntArray
    fun getInteger(id: Int): Int
    fun getLayout(id: Int): XmlResourceParser
    fun getMovie(id: Int): Movie
    fun getQuantityString(id: Int, quantity: Int): String
    fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String
    fun getQuantityText(id: Int, quantity: Int): CharSequence
    fun getResourceEntryName(resid: Int): String
    fun getResourceName(resid: Int): String
    fun getResourcePackageName(resid: Int): String
    fun getResourceTypeName(resid: Int): String
    fun getString(id: Int, vararg formatArgs: Any): String
    fun getString(id: Int): String
    fun getStringArray(id: Int): Array<String>
    fun getText(id: Int, def: CharSequence): CharSequence
    fun getText(id: Int): CharSequence
    fun getTextArray(id: Int): Array<CharSequence>
    fun getValue(name: String, outValue: TypedValue, resolveRefs: Boolean)
    fun getValue(id: Int, outValue: TypedValue, resolveRefs: Boolean)
    fun getValueForDensity(id: Int, density: Int, outValue: TypedValue, resolveRefs: Boolean)
    fun getXml(id: Int): XmlResourceParser
    fun newTheme(): android.content.res.Resources.Theme
    fun obtainAttributes(set: AttributeSet, attrs: IntArray): TypedArray
    fun obtainTypedArray(id: Int): TypedArray
    fun openRawResource(id: Int, value: TypedValue): InputStream
    fun openRawResource(id: Int): InputStream
    fun openRawResourceFd(id: Int): AssetFileDescriptor
    fun parseBundleExtra(tagName: String, attrs: AttributeSet, outBundle: Bundle)
    fun parseBundleExtras(parser: XmlResourceParser, outBundle: Bundle)
}

/**
 * Resourcesにアクセスするクラス
 *
 * Created by kght6123 on 2017/11/04.
 */
class ResourcesImpl(private val resources: android.content.res.Resources) : Resources {
    override fun finishPreloading() {
        resources.finishPreloading()
    }
    override fun flushLayoutCache() {
        resources.flushLayoutCache()
    }
    override fun getAnimation(id: Int): XmlResourceParser {
        return resources.getAnimation(id)
    }
    override fun getAssets(): AssetManager {
        return resources.assets
    }
    override fun getBoolean(id: Int): Boolean {
        return resources.getBoolean(id)
    }
    override fun getColor(id: Int, theme: android.content.res.Resources.Theme): Int {
        return resources.getColor(id, theme)
    }
    override fun getColorStateList(id: Int, theme: android.content.res.Resources.Theme): ColorStateList {
        return resources.getColorStateList(id, theme)
    }
    override fun getConfiguration(): Configuration {
        return resources.configuration
    }
    override fun getDimension(id: Int): Float {
        return resources.getDimension(id)
    }
    override fun getDimensionPixelOffset(id: Int): Int {
        return resources.getDimensionPixelOffset(id)
    }
    override fun getDimensionPixelSize(id: Int): Int {
        return resources.getDimensionPixelSize(id)
    }
    override fun getDisplayMetrics(): DisplayMetrics {
        return resources.displayMetrics
    }
    override fun getDrawable(id: Int, theme: android.content.res.Resources.Theme): Drawable {
        return resources.getDrawable(id, theme)
    }
    override fun getDrawableForDensity(id: Int, density: Int, theme: android.content.res.Resources.Theme): Drawable {
        return resources.getDrawableForDensity(id, density, theme)
    }
    override fun getFraction(id: Int, base: Int, pbase: Int): Float {
        return resources.getFraction(id, base, pbase)
    }
    override fun getIdentifier(name: String, defType: String, defPackage: String): Int {
        return resources.getIdentifier(name, defType, defPackage)
    }
    override fun getIntArray(id: Int): IntArray {
        return resources.getIntArray(id)
    }
    override fun getInteger(id: Int): Int {
        return resources.getInteger(id)
    }
    override fun getLayout(id: Int): XmlResourceParser {
        return resources.getLayout(id)
    }
    override fun getMovie(id: Int): Movie {
        return resources.getMovie(id)
    }
    override fun getQuantityString(id: Int, quantity: Int): String {
        return resources.getQuantityString(id, quantity)
    }
    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String {
        return resources.getQuantityString(id, quantity, formatArgs)
    }
    override fun getQuantityText(id: Int, quantity: Int): CharSequence {
        return resources.getQuantityText(id, quantity)
    }
    override fun getResourceEntryName(resid: Int): String {
        return resources.getResourceEntryName(resid)
    }
    override fun getResourceName(resid: Int): String {
        return resources.getResourceName(resid)
    }
    override fun getResourcePackageName(resid: Int): String {
        return resources.getResourcePackageName(resid)
    }
    override fun getResourceTypeName(resid: Int): String {
        return resources.getResourceTypeName(resid)
    }
    override fun getString(id: Int, vararg formatArgs: Any): String {
        return resources.getString(id, formatArgs)
    }
    override fun getString(id: Int): String {
        return resources.getString(id)
    }
    override fun getStringArray(id: Int): Array<String> {
        return resources.getStringArray(id)
    }
    override fun getText(id: Int, def: CharSequence): CharSequence {
        return resources.getText(id, def)
    }
    override fun getText(id: Int): CharSequence {
        return resources.getText(id)
    }
    override fun getTextArray(id: Int): Array<CharSequence> {
        return resources.getTextArray(id)
    }
    override fun getValue(name: String, outValue: TypedValue, resolveRefs: Boolean) {
        return resources.getValue(name, outValue, resolveRefs)
    }
    override fun getValue(id: Int, outValue: TypedValue, resolveRefs: Boolean) {
        return resources.getValue(id, outValue, resolveRefs)
    }
    override fun getValueForDensity(id: Int, density: Int, outValue: TypedValue, resolveRefs: Boolean) {
        return resources.getValueForDensity(id, density, outValue, resolveRefs)
    }
    override fun getXml(id: Int): XmlResourceParser {
        return resources.getXml(id)
    }
    override fun newTheme(): android.content.res.Resources.Theme {
        return resources.newTheme()
    }
    override fun obtainAttributes(set: AttributeSet, attrs: IntArray): TypedArray {
        return resources.obtainAttributes(set, attrs)
    }
    override fun obtainTypedArray(id: Int): TypedArray {
        return resources.obtainTypedArray(id)
    }
    override fun openRawResource(id: Int, value: TypedValue): InputStream {
        return resources.openRawResource(id, value)
    }
    override fun openRawResource(id: Int): InputStream {
        return resources.openRawResource(id)
    }
    override fun openRawResourceFd(id: Int): AssetFileDescriptor {
        return resources.openRawResourceFd(id)
    }
    override fun parseBundleExtra(tagName: String, attrs: AttributeSet, outBundle: Bundle) {
        resources.parseBundleExtra(tagName, attrs, outBundle)
    }
    override fun parseBundleExtras(parser: XmlResourceParser, outBundle: Bundle) {
        resources.parseBundleExtras(parser, outBundle)
    }
}