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
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_regular, FontWeight.Normal)
)

val Impact = FontFamily(
    Font(R.font.impact, FontWeight.Normal),
)

val AppTypography = Typography().withFont(MyFont)

fun Typography.withFont(fontFamily: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    displayMedium = displayMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    displaySmall = displaySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),

    headlineLarge = headlineLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    headlineSmall = headlineSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),

    titleLarge = titleLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = titleMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    titleSmall = titleSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),

    bodyLarge = bodyLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    bodyMedium = bodyMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    bodySmall = bodySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),

    labelLarge = labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    labelMedium = labelMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
    labelSmall = labelSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
)
