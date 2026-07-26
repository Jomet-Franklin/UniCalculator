package com.unicalculator.app.ui.theme

/*
 * UniCalculator – a versatile calculator for Android
 * Copyright (C) 2025 Jomet Franklin
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

// Custom color attributes
data class CalculatorColors(
    val background: Color,
    val displayBackground: Color,
    val displayDivider: Color,
    val numberButton: Color,
    val functionButton: Color,
    val accentButton: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentText: Color,
    val toolbarActive: Color
)

// ========== DARK MODE COLORS ==========
private val DarkCalculatorColors = CalculatorColors(
    background = Color(0xFF0D1418),      // Near-black navy
    displayBackground = Color(0xFF16262B), // Dark teal panel
    displayDivider = Color(0xFF2A3E42),    // Lighter divider
    numberButton = Color(0xFF1B2529),      // Dark slate/charcoal
    functionButton = Color(0xFF3D5459),    // Muted slate-teal
    accentButton = Color(0xFF0E8C93),      // Bright cyan-teal
    textPrimary = Color(0xFFE8F1F2),       // Off-white
    textSecondary = Color(0xFF8A9A9A),     // Dim text
    accentText = Color(0xFF0E8C93),        // Cyan-teal accent
    toolbarActive = Color(0xFF0E8C93)      // Same cyan-teal
)

// ========== PURE BLACK (AMOLED) MODE COLORS ==========
private val PureBlackCalculatorColors = CalculatorColors(
    background = Color(0xFF000000),      // True black
    displayBackground = Color(0xFF0A0A0A), // Near-black panel
    displayDivider = Color(0xFF1A1A1A),    // Faint divider
    numberButton = Color(0xFF1C1C1C),      // Dark charcoal
    functionButton = Color(0xFF2A2E30),    // Muted dark grey-blue
    accentButton = Color(0xFF00C2D1),      // Bright cyan-teal
    textPrimary = Color(0xFFF5F5F5),       // Bright white
    textSecondary = Color(0xFF8A9A9A),     // Dim text
    accentText = Color(0xFF00C2D1),        // Cyan-teal accent
    toolbarActive = Color(0xFF00C2D1)      // Same cyan-teal
)

// ========== LIGHT MODE COLORS ==========
private val LightCalculatorColors = CalculatorColors(
    background = Color(0xFFEAF1F0),      // Very light mint-white
    displayBackground = Color(0xFFE4EDEC), // Soft mint-grey panel
    displayDivider = Color(0xFFD0DCDA),    // Slightly darker divider
    numberButton = Color(0xFFE7EEED),      // Pale off-white/grey
    functionButton = Color(0xFFC8E3E6),    // Soft powder-blue
    accentButton = Color(0xFF6EC4D8),      // Brighter sky-blue
    textPrimary = Color(0xFF1A1A1A),       // Dark charcoal/black
    textSecondary = Color(0xFF666666),     // Medium grey
    accentText = Color(0xFF6EC4D8),        // Sky-blue accent
    toolbarActive = Color(0xFF6EC4D8)      // Same sky-blue
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