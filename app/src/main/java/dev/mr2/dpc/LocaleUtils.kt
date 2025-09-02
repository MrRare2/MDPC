package dev.mr2.dpc

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.*

data class Language(
    val lang: String,
    val region: String,
    val name: String
)

fun Context.setLocale(language: String, region: String = ""): Context {
    val locale = if (region.isNotBlank()) Locale(language, region) else Locale(language)
    Locale.setDefault(locale)

    val config = Configuration(resources.configuration)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(locale)
        config.setLocales(android.os.LocaleList(locale))
        return createConfigurationContext(config)
    } else {
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        return this
    }
}

fun Context.resetLocale(): Context {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = this.getSystemService(Context.LOCALE_SERVICE) as? android.app.LocaleManager
        localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
        return this
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        val systemLocale = Locale.getDefault()
        return setLocale(systemLocale.language, systemLocale.country)
    }
}

object LocaleHelper {
    fun wrap(base: Context, language: String, region: String = ""): Context {
        val locale = if (region.isNotBlank()) Locale(language, region) else Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(android.os.LocaleList(locale))
            return base.createConfigurationContext(config)
        } else {
            config.locale = locale
            base.resources.updateConfiguration(config, base.resources.displayMetrics)
            return base
        }
    }

    fun reset(base: Context): Context {
        val def = Locale.getDefault()
        val config = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(def)
            config.setLocales(android.os.LocaleList(def))
            return base.createConfigurationContext(config)
        } else {
            config.locale = def
            base.resources.updateConfiguration(config, base.resources.displayMetrics)
            return base
        }
    }
}
