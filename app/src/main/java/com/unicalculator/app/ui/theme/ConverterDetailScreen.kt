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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unicalculator.app.calculator.UnitConverter
import com.unicalculator.app.ui.components.CustomDropdown

@Composable
fun ConverterDetailScreen(category: String, onBack: () -> Unit) {
    val colors = LocalCalculatorColors.current
    BackHandler(onBack = onBack)

    val units = UnitConverter.getUnitsForCategory(category)
    var fromUnit by remember { mutableStateOf(units.firstOrNull() ?: "") }
    var toUnit by remember { mutableStateOf(units.getOrElse(1) { units.firstOrNull() ?: "" }) }
    var inputValue by remember { mutableStateOf("1") }
    var resultValue by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(inputValue, fromUnit, toUnit) {
        if (category == "numericbase") {
            val fromBase = UnitConverter.getBaseForUnit(fromUnit)
            val toBase = UnitConverter.getBaseForUnit(toUnit)
            val result = UnitConverter.convertNumericBase(inputValue, fromBase, toBase)
            resultValue = result
        } else {
            val value = inputValue.toDoubleOrNull()
            if (value != null && fromUnit.isNotEmpty() && toUnit.isNotEmpty()) {
                val result = UnitConverter.convert(value, fromUnit, toUnit, category)
                resultValue = if (result != null) {
                    if (result == result.toLong().toDouble()) {
                        result.toLong().toString()
                    } else {
                        String.format("%.6f", result).trimEnd('0').trimEnd('.')
                    }
                } else {
                    ""
                }
            } else {
                resultValue = ""
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Text(
                text = "Convert $category",
                color = colors.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = inputValue,
            onValueChange = { newValue ->
                val filtered = if (category == "numericbase") {
                    newValue.uppercase().filter { it.isDigit() || it in 'A'..'F' }
                } else {
                    newValue.filter { it.isDigit() || it == '.' }
                }
                if (filtered.count { it == '.' } <= 1) {
                    inputValue = filtered
                }
            },
            label = { Text("Value", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 20.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("From", color = colors.textSecondary, fontSize = 14.sp)
                // ✅ Using new CustomDropdown
                CustomDropdown(
                    items = units,
                    selected = fromUnit,
                    onItemSelected = { selectedItem -> fromUnit = selectedItem },
                    label = { unit -> unit }
                )
            }

            IconButton(
                onClick = {
                    val temp = fromUnit
                    fromUnit = toUnit
                    toUnit = temp
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.SwapVert,
                    contentDescription = "Swap units",
                    tint = colors.accentButton
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("To", color = colors.textSecondary, fontSize = 14.sp)
                CustomDropdown(
                    items = units,
                    selected = toUnit,
                    onItemSelected = { selectedItem -> toUnit = selectedItem },
                    label = { unit -> unit }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        if (resultValue.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.functionButton)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$inputValue $fromUnit =",
                        color = colors.textSecondary,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "$resultValue $toUnit",
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = "Enter a value to convert",
                color = colors.textSecondary,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.weight(1f))
    }
}