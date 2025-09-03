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
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
        val locale = Locale.getDefault()
        val resources = this.resources
	val config = Configuration(resources.configuration)
	config.locale = locale
	config.setLayoutDirection(locale)
	resources.updateConfiguration(config, resources.displayMetrics)
	return this
    }
    return this
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
}


