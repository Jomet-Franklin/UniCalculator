package com.unicalculator.app.ui.components

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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unicalculator.app.ui.theme.LocalCalculatorColors

@Composable
fun <T> CustomDropdown(
    items: List<T>,
    selected: T?,
    onItemSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = ""
) {
    val colors = LocalCalculatorColors.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Trigger
        val shape = if (expanded) {
            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        } else {
            RoundedCornerShape(28.dp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.background)
                .clickable(enabled = enabled) { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected?.let { label(it) } ?: placeholder,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textPrimary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(max = 250.dp)
                .heightIn(max = 300.dp)
                .background(
                    colors.background,
                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
            offset = DpOffset(0.dp, 0.dp),
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            containerColor = colors.background,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = item == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) colors.functionButton else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onItemSelected(item)
                            expanded = false
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label(item),
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}