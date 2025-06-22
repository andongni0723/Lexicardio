package com.andongni.vcblearn.ui.theme

import android.graphics.Color
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.andongni.vcblearn.R

val MyFont = FontFamily(
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
)

val AppTypography = Typography().withFont(MyFont)

fun Typography.withFont(fontFamily: FontFamily) = Typography(
    displayLarge   = displayLarge.copy(fontFamily = fontFamily),
    displayMedium  = displayMedium.copy(fontFamily = fontFamily),
    displaySmall   = displaySmall.copy(fontFamily = fontFamily),

    headlineLarge  = headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall  = headlineSmall.copy(fontFamily = fontFamily),

    titleLarge     = titleLarge.copy(fontFamily = fontFamily),
    titleMedium    = titleMedium.copy(fontFamily = fontFamily),
    titleSmall     = titleSmall.copy(fontFamily = fontFamily),

    bodyLarge      = bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium     = bodyMedium.copy(fontFamily = fontFamily),
    bodySmall      = bodySmall.copy(fontFamily = fontFamily),

    labelLarge     = labelLarge.copy(fontFamily = fontFamily),
    labelMedium    = labelMedium.copy(fontFamily = fontFamily),
    labelSmall     = labelSmall.copy(fontFamily = fontFamily),
)
