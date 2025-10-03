package dev.mr2.dpc.ui.theme

import android.app.Activity
import android.os.Build.VERSION
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.mr2.dpc.ThemeSettings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MDPCTheme(
    theme: ThemeSettings,
    content: @Composable () -> Unit
) {
    val darkTheme = theme.darkTheme == 1 || (theme.darkTheme == -1 && isSystemInDarkTheme())
    val context = LocalContext.current
    val colorScheme = when {
        theme.themeColor == 0 && VERSION.SDK_INT >= 31 -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> when (theme.themeColor) {
	        1 -> blueDarkScheme
	        2 -> redDarkScheme
	        3 -> orangeDarkScheme
	        4 -> yellowDarkScheme
	        5 -> pinkDarkScheme
	        6 -> purpleDarkScheme
	        7 -> greenDarkScheme
	        else -> blueDarkScheme
	    }
        else -> when (theme.themeColor) {
	        1 -> blueLightScheme
	        2 -> redLightScheme
	        3 -> orangeLightScheme
	        4 -> yellowLightScheme
	        5 -> pinkLightScheme
	        6 -> purpleLightScheme
	        7 -> greenLightScheme
	        else -> blueLightScheme
        }
    }.let {
        if (darkTheme && theme.blackTheme) it.copy(background = Color.Black, surface = Color.Black) else it
    }
    val view = LocalView.current
    SideEffect {
        val window = (context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        content = content,
        motionScheme = MotionScheme.expressive()
    )
}
