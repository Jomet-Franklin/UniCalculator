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

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeChanged: (String) -> Unit = {},
    onPureBlackChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    var selectedTheme by remember {
        mutableStateOf(prefs.getString("selected_theme", "System") ?: "System")
    }
    var isPureBlackEnabled by remember {
        mutableStateOf(prefs.getBoolean("pure_black_enabled", false))
    }

    val isDarkModeActive = when (selectedTheme) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    fun saveTheme(theme: String) {
        prefs.edit { putString("selected_theme", theme) }
        selectedTheme = theme
        onThemeChanged(theme)
    }

    fun savePureBlack(enabled: Boolean) {
        prefs.edit { putBoolean("pure_black_enabled", enabled) }
        isPureBlackEnabled = enabled
        onPureBlackChanged(enabled)
    }

    val colors = LocalCalculatorColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ---- Theme Section ----
            item {
                SectionHeader(title = "Theme")
            }

            item {
                SettingBlock(
                    icon = Icons.Outlined.DarkMode,
                    title = "Choose theme",
                    description = "Light or dark theme for your app",
                    control = {
                        SegmentedControl(
                            options = listOf("System", "Light", "Dark"),
                            selected = selectedTheme,
                            onOptionSelected = { saveTheme(it) }
                        )
                    }
                )
            }

            item {
                ToggleBlock(
                    icon = Icons.Outlined.Colorize,
                    title = "Pure black",
                    description = "Use pure black background in dark mode",
                    checked = isPureBlackEnabled,
                    onCheckedChange = { savePureBlack(it) },
                    enabled = isDarkModeActive
                )
            }

            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = LocalCalculatorColors.current
    Text(
        text = title,
        color = colors.accentButton,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .padding(top = 24.dp, bottom = 12.dp)
    )
}

@Composable
private fun SettingBlock(
    icon: ImageVector,
    title: String,
    description: String,
    control: @Composable () -> Unit
) {
    val colors = LocalCalculatorColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        control()
    }
}

@Composable
private fun ToggleBlock(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val colors = LocalCalculatorColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) colors.textSecondary else colors.textSecondary.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) colors.textPrimary else colors.textPrimary.copy(alpha = 0.3f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = if (enabled) colors.textSecondary else colors.textSecondary.copy(alpha = 0.3f),
                fontSize = 14.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = if (enabled) Color.DarkGray else Color.DarkGray.copy(alpha = 0.3f),
                uncheckedThumbColor = Color.DarkGray,
                uncheckedTrackColor = if (enabled) Color.Gray.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f),
                disabledCheckedThumbColor = Color.DarkGray.copy(alpha = 0.3f),
                disabledCheckedTrackColor = Color.DarkGray.copy(alpha = 0.1f),
                disabledUncheckedThumbColor = Color.DarkGray.copy(alpha = 0.3f),
                disabledUncheckedTrackColor = Color.DarkGray.copy(alpha = 0.1f)
            )
        )
    }
}

// ========== Segmented Control ==========
@Composable
private fun SegmentedControl(
    options: List<String>,
    selected: String,
    onOptionSelected: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val borderColor = colors.textSecondary.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(
                        when {
                            index == 0 -> RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)
                            index == options.size - 1 -> RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp)
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
                    .background(
                        if (isSelected) colors.accentButton else Color.Transparent
                    )
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (index < options.size - 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(borderColor)
                )
            }
        }
    }
}