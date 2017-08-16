package jp.kght6123.smallappbrowser.utils

import java.util.Arrays
import java.util.HashSet

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View

object PrefUtils {
    fun setVisibility(resIdKey: Int, view: View, defaultValue: Boolean, context: Context, pref: SharedPreferences) {
        if (!pref.getBoolean(context.getString(resIdKey), defaultValue))
            view.visibility = View.GONE
    }

    fun setInt(key: String, value: Int, classObj: Class<*>, context: Context) {
        val sPref = context.getSharedPreferences(classObj.name, Context.MODE_PRIVATE)

        val editor = sPref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, value: Int, classObj: Class<*>, context: Context): Int {
        val sPref = context.getSharedPreferences(classObj.name, Context.MODE_PRIVATE)

        return sPref.getInt(key, value)
    }

    fun setLong(key: String, value: Long, classObj: Class<*>, context: Context) {
        val sPref = context.getSharedPreferences(classObj.name, Context.MODE_PRIVATE)

        val editor = sPref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String, value: Long, classObj: Class<*>, context: Context): Long {
        val sPref = context.getSharedPreferences(classObj.name, Context.MODE_PRIVATE)

        return sPref.getLong(key, value)
    }

    fun getDefaultPercent(resIdKey: Int, value: Float, context: Context): Float {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val floatValue = pref.getString(context.getString(resIdKey), java.lang.Float.toString(value * 100))

        return (java.lang.Float.parseFloat(floatValue).toFloat() / 100f).toFloat()
    }

    fun getDefaultBoolean(resIdKey: Int, value: Boolean, context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getBoolean(context.getString(resIdKey), value)
    }

    fun getDefaultString(resIdKey: Int, value: String, context: Context): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getString(context.getString(resIdKey), value)
    }

    fun getDefaultStringArray(resIdKey: Int, resIdDefValues: Int, context: Context): Set<String> {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val hashSet = HashSet(Arrays.asList(*context.resources.getStringArray(resIdDefValues)))

        return pref.getStringSet(context.getString(resIdKey), hashSet)
    }

}
