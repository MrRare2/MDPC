package dev.mr2.dpc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MyViewModel(application: Application): AndroidViewModel(application) {
    val theme = MutableStateFlow(ThemeSettings(SP.themeColor, SP.darkTheme, SP.blackTheme))
    fun changeTheme(newTheme: ThemeSettings) {
        theme.value = newTheme
        SP.themeColor = newTheme.themeColor
        SP.darkTheme = newTheme.darkTheme
        SP.blackTheme = newTheme.blackTheme
    }
}

data class ThemeSettings(
    // color: 0 -> dynamic, 1 -> blue, 2 -> red, 3 -> orange, 4 -> yellow, 5 -> pink 6 -> purple, 7 -> green
    val themeColor: Int = 0,
    val darkTheme: Int = -1,
    val blackTheme: Boolean = false
)
