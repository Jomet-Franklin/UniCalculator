package com.unicalculator.app.ui.theme

/*
 * UniCalculator – a versatile calculator for Android
 * Copyright (C) 2026 Jomet Franklin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class CalculatorColors(
    val background: Color,
    val displayBackground: Color,
    val displayDivider: Color,
    val numberButton: Color,
    val functionButton: Color,
    val converterButton: Color,
    val accentButton: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentText: Color,
    val toolbarActive: Color,
    val converterIconTint: Color,
    val textTertiary: Color,
    val someLink: Color,
    val dangerText: Color,
    val menuSurface: Color,
    val menuBorder: Color,
    val onAccent: Color
)

private val DarkCalculatorColors = CalculatorColors(
    background = Color(0xFF0A0F16),
    displayBackground = Color(0xFF111923),
    displayDivider = Color(0xFF243140),
    numberButton = Color(0xFF1B2531),
    functionButton = Color(0xFF284554),
    converterButton = Color(0xFF16202A),
    converterIconTint = Color(0xFF4DD8E6),
    accentButton = Color(0xFF00A3B5),
    textPrimary = Color(0xFFF2F6F8),
    textSecondary = Color(0xFFF2F6F8),
    textTertiary = Color(0xFF9AA8B2),
    accentText = Color(0xFF4DD8E6),
    toolbarActive = Color(0xFF4DD8E6),
    someLink = Color(0xFF5CC8FF),
    dangerText = Color(0xFFFF9C94),
    menuSurface = Color(0xFF223241),
    menuBorder = Color(0xFF2E6E7A),
    onAccent = Color(0xFF06141A)
)

private val PureBlackCalculatorColors = CalculatorColors(
    background = Color(0xFF000000),
    displayBackground = Color(0xFF0A0A0A),
    displayDivider = Color(0xFF1A1A1A),
    numberButton = Color(0xFF141414),
    functionButton = Color(0xFF2A2E30),
    converterButton = Color(0xFF1A1A1A),
    converterIconTint = Color(0xFF4DD8E6),
    accentButton = Color(0xFF00A3B5),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFFFFFFF),
    textTertiary = Color(0xFF8A8A8A),
    accentText = Color(0xFF4DD8E6),
    toolbarActive = Color(0xFF4DD8E6),
    someLink = Color(0xFF5CC8FF),
    dangerText = Color(0xFFFF9C94),
    menuSurface = Color(0xFF161B1E),
    menuBorder = Color(0xFF2E6E7A),
    onAccent = Color(0xFF06141A)
)

private val LightCalculatorColors = CalculatorColors(
    background = Color(0xFFFFFFFF),
    displayBackground = Color(0xFFF2FEFF),
    displayDivider = Color(0xFFD0DCDA),
    numberButton = Color(0xFFDDFFFF),
    functionButton = Color(0xFFB4FFFF),
    converterButton = Color(0xFFE6FCFC),
    converterIconTint = Color(0xFF3DD1CE),
    accentButton = Color(0xFF6EFFFF),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF333333),
    textTertiary = Color(0xFF5A6B6B),
    accentText = Color(0xFF00A3B5),
    toolbarActive = Color(0xFF2CE1E6),
    someLink = Color(0xFF1543D6),
    dangerText = Color(0xFFC62828),
    menuSurface = Color(0xFFE6F4F6),
    menuBorder = Color(0xFFB0D8DA),
    onAccent = Color(0xFF06141A)
)

val LocalCalculatorColors = staticCompositionLocalOf { DarkCalculatorColors }

@Composable
fun UniCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val calculatorColors = when {
        pureBlack && darkTheme -> PureBlackCalculatorColors
        darkTheme -> DarkCalculatorColors
        else -> LightCalculatorColors
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = calculatorColors.accentButton,
            secondary = calculatorColors.accentText,
            background = calculatorColors.background,
            surface = calculatorColors.displayBackground,
            onBackground = calculatorColors.textPrimary,
            onSurface = calculatorColors.textPrimary,
            error = calculatorColors.dangerText
        )
    } else {
        lightColorScheme(
            primary = calculatorColors.accentButton,
            secondary = calculatorColors.accentText,
            background = calculatorColors.background,
            surface = calculatorColors.displayBackground,
            onBackground = calculatorColors.textPrimary,
            onSurface = calculatorColors.textPrimary,
            error = calculatorColors.dangerText
        )
    }

    CompositionLocalProvider(LocalCalculatorColors provides calculatorColors) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}