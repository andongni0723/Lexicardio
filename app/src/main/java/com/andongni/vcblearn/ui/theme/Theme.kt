package com.andongni.vcblearn.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    /* ===== 1. 主要背景 ===== */
    background       = Color(0xFF14161D),   // #14161D

    onBackground     = Color(0xFFE6E6E6),   // #E6E6E6

    /* ===== 2. 文字／圖示 ===== */
    surface          = Color(0xFF0F0F0F),   // Nav Bar、Dialog 用
    onSurface        = Color(0xFFE6E6E6),

    /* ===== 3. 欄位底色（TextField 等）===== */
    surfaceVariant       = Color(0xFF2B2E3A),   // #2B2E3A
    onSurfaceVariant     = Color(0xFFE6E6E6),

    /* ===== 4. 主品牌色（Mini Button 文字用） ===== */
    primary          = Color(0xFF8080FF),   // #8080FF
    primaryContainer = Color(0xFF14161D),
    onPrimary        = Color(0xFFE6E6E6),   // 白字／淺灰字

    /* ===== 5. Tint & 其他 ===== */
    surfaceTint      = Color(0xFF8080FF),   // 影響 Elevated 元件疊加色
    outline          = Color(0xFF2B2E3A),   // Slider 軌道 / Divider
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun VCBLearnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}