package com.klodit.almizan.util

import android.content.Context
import android.content.res.Configuration
import com.klodit.almizan.ui.theme.AppLanguage
import java.util.Locale

object LocaleHelper {

    private const val PREF_NAME = "locale_pref"
    private const val KEY_LANG  = "selected_lang"

    fun setLocale(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, language.locale)
            .apply()
    }

    fun currentLanguage(context: Context): AppLanguage {
        val saved = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANG, null)
        return AppLanguage.entries.firstOrNull { it.locale == saved }
            ?: AppLanguage.FRENCH
    }

    // Returns a new Context with the locale baked in
    fun applyLocale(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.locale)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}