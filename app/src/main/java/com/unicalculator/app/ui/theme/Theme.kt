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
    val someLink: Color
)

// ========== DARK MODE COLORS ==========
private val DarkCalculatorColors = CalculatorColors(
    background = Color(0xFF09111A),        // background color
    displayBackground = Color(0xFF0E171F), // Display color
    displayDivider = Color(0xFF2A3E42),    // Divider color
    numberButton = Color(0xFF253036),      // Number row button color
    functionButton = Color(0xFF375E69),    // tan, cos, * etc...
    converterButton = Color(0xFF1B2529),   // Converter button color (something like dark blue)
    converterIconTint = Color(0xFF00FFFF), // Converter icon color (cyan)
    accentButton = Color(0xFF00C2D1),      // =, Enter, etc...
    textPrimary = Color(0xFFFFFFFF),       // Number text color
    textSecondary = Color(0xFFFFFFFF),     // every text color
    textTertiary = Color(0xFFADADAD),      // Sub heading color
    accentText = Color(0xFF00D9FF),        // Output color
    toolbarActive = Color(0xFF00FFFF),     // Top, active icon color (cyan)
    someLink = Color(0xFF00D9FF)           // Some link color
)

// ========== PURE BLACK (AMOLED) MODE COLORS ==========
private val PureBlackCalculatorColors = CalculatorColors(
    background = Color(0xFF000000),        // background color (True black)
    displayBackground = Color(0xFF0A0A0A), // Display color (Near-black panel)
    displayDivider = Color(0xFF1A1A1A),    // divider
    numberButton = Color(0xFF141414),      // Number row button color
    functionButton = Color(0xFF2A2E30),    // tan, cos, * etc...
    converterButton = Color(0xFF1A1A1A),   // Converter button color
    converterIconTint = Color(0xFF00FFFF), // Converter icon color (cyan)
    accentButton = Color(0xFF00C2D1),      // =, Enter, etc...
    textPrimary = Color(0xFFFFFFFF),       // Number text color
    textSecondary = Color(0xFFFFFFFF),     // every text color
    textTertiary = Color(0xFF6E6E6E),      // Sub heading color
    accentText = Color(0xFF00D9FF),        // Output color
    toolbarActive = Color(0xFF00FFFF),     // Same top, active icon color (cyan)
    someLink = Color(0xFF00D9FF)           // Some link color
)

// ========== LIGHT MODE COLORS ==========
private val LightCalculatorColors = CalculatorColors(
    background = Color(0xFFFFFFFF),        // background color
    displayBackground = Color(0xFFF2FEFF), // Display color
    displayDivider = Color(0xFFD0DCDA),    // divider
    numberButton = Color(0xFFDDFFFF),      // Number row button color
    functionButton = Color(0xFFB4FFFF),    // tan, cos, * etc...
    converterButton = Color(0xFFE6FCFC),   // onverter button color
    converterIconTint = Color(0xFF3DD1CE), // Converter icon color
    accentButton = Color(0xFF6EFFFF),      // =, Enter, etc...
    textPrimary = Color(0xFF000000),       // Number text color
    textSecondary = Color(0xFF333333),     // every text color
    textTertiary = Color(0xFF333333),      // Sub heading color
    accentText = Color(0xFF00D9FF),        // Output color
    toolbarActive = Color(0xFF2CE1E6),     // ive icon color (cyan)
    someLink = Color(0xFF1543D6)           // Some link color
)

// CompositionLocal to provide calculator colors
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
            onSurface = calculatorColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = calculatorColors.accentButton,
            secondary = calculatorColors.accentText,
            background = calculatorColors.background,
            surface = calculatorColors.displayBackground,
            onBackground = calculatorColors.textPrimary,
            onSurface = calculatorColors.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalCalculatorColors provides calculatorColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}