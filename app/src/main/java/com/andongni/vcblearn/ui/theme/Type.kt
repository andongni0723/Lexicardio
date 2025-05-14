package com.andongni.vcblearn.ui.theme

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.andongni.vcblearn.R

val MyFont = FontFamily(
//    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
)

val color: Color = Color(0xFFe6e6e6)

val Typography = Typography().withFont(MyFont)

fun Typography.withFont(fontFamily: FontFamily) = Typography(
    displayLarge   = displayLarge.copy(fontFamily = fontFamily, color = color),
    displayMedium  = displayMedium.copy(fontFamily = fontFamily, color = color),
    displaySmall   = displaySmall.copy(fontFamily = fontFamily, color = color),
    headlineLarge  = headlineLarge.copy(fontFamily = fontFamily, color = color),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily, color = color),
    headlineSmall  = headlineSmall.copy(fontFamily = fontFamily, color = color),
    titleLarge     = titleLarge.copy(fontFamily = fontFamily, color = color),
    titleMedium    = titleMedium.copy(fontFamily = fontFamily, color = color),
    titleSmall     = titleSmall.copy(fontFamily = fontFamily, color = color),
    bodyLarge      = bodyLarge.copy(fontFamily = fontFamily, color = color),
    bodyMedium     = bodyMedium.copy(fontFamily = fontFamily, color = color),
    bodySmall      = bodySmall.copy(fontFamily = fontFamily, color = color),
    labelLarge     = labelLarge.copy(fontFamily = fontFamily, color = color),
    labelMedium    = labelMedium.copy(fontFamily = fontFamily, color = color),
    labelSmall     = labelSmall.copy(fontFamily = fontFamily, color = color),
)
