package com.andongni.vcblearn.locate

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale
import javax.inject.Singleton

data class Language(
    val code: String,
    val displayName: String
)

val appLanguages = listOf(
    Language("en", "English"),
    Language("zh-TW", "繁體中文"),
    Language("zh-CN", "简体中文"),
)

@Singleton
class AppLocaleManager {

    fun changeLanguage(context: Activity, code: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(code)
        } else {
//            context.resources.configuration.setLocale(LocaleList.forLanguageTags(code)[0])
            context.resources.configuration.setLocales(LocaleList.forLanguageTags(code))
            Log.d("LocaleManager", "S: " + context.resources.configuration.locales.get(0))
//            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
            context.recreate()
        }
    }

    fun getLanguageCode(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)?.applicationLocales?.get(0)
        } else {
            AppCompatDelegate.getApplicationLocales().get(0)
        }

        Log.d("LocaleManager", "Get:" + (locale?.language ?: getDefaultLanguageCode()))
        return locale?.language ?: getDefaultLanguageCode();
    }

    private fun getDefaultLanguageCode(): String {
        return appLanguages.first().code
    }

}