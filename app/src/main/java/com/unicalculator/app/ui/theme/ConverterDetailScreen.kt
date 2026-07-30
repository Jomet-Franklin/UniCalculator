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

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unicalculator.app.calculator.UnitConverter
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
@Composable
fun ConverterDetailScreen(
    category: String,
    onBack: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val categoryDisplayNames = mapOf(
        "length" to "Length", "area" to "Area", "volume" to "Volume",
        "mass" to "Mass", "temperature" to "Temperature", "storage" to "Storage",
        "pressure" to "Pressure", "heat" to "Heat/Energy", "speed" to "Speed",
        "time" to "Time", "angle" to "Angle", "power" to "Power",
        "force" to "Force", "density" to "Density", "frequency" to "Frequency",
        "torque" to "Torque", "viscosity" to "Viscosity", "fuel" to "Fuel",
        "date" to "Date", "bmi" to "BMI", "shopping" to "Shopping"
    )

    val categoryDisplayName = categoryDisplayNames[category] ?: category

    val units = UnitConverter.getUnitsForCategory(category)

    var fromUnit by remember { mutableStateOf(units.firstOrNull() ?: "") }
    var toUnit by remember { mutableStateOf(units.getOrElse(1) { units.firstOrNull() ?: "" }) }

    var inputText by remember { mutableStateOf(TextFieldValue("1", selection = TextRange(1))) }
    var resultText by remember { mutableStateOf("") }
    var swapRotation by remember { mutableFloatStateOf(0f) }

    val unitAbbreviations = mapOf(
        // Length
        "Nanometer" to "nm", "Micrometer" to "µm", "Millimeter" to "mm",
        "Centimeter" to "cm", "Decimeter" to "dm", "Meter" to "m",
        "Kilometer" to "km", "Mile" to "mi", "Yard" to "yd",
        "Foot" to "ft", "Inch" to "in", "Nautical Mile" to "nmi",
        // Area
        "Square Millimeter" to "mm²", "Square Centimeter" to "cm²",
        "Square Decimeter" to "dm²", "Square Meter" to "m²",
        "Square Kilometer" to "km²", "Hectare" to "ha",
        "Acre" to "ac", "Square Yard" to "yd²", "Square Foot" to "ft²",
        "Square Inch" to "in²",
        // Volume
        "Cubic Millimeter" to "mm³", "Cubic Centimeter" to "cm³",
        "Cubic Decimeter" to "dm³", "Cubic Meter" to "m³",
        "Liter" to "L", "Milliliter" to "mL", "Kiloliter" to "kL",
        "Gallon" to "gal", "Quart" to "qt", "Pint" to "pt",
        "Cup" to "cup", "Fluid Ounce" to "fl oz",
        "Tablespoon" to "tbsp", "Teaspoon" to "tsp",
        // Mass
        "Milligram" to "mg", "Gram" to "g", "Kilogram" to "kg",
        "Tonne" to "t", "Pound" to "lb", "Ounce" to "oz",
        "Stone" to "st",
        // Temperature
        "Celsius" to "°C", "Fahrenheit" to "°F", "Kelvin" to "K",
        // Storage
        "Bit" to "b", "Byte" to "B", "Kilobit" to "kb",
        "Kilobyte" to "KB", "Kibibit" to "Kib", "Kibibyte" to "KiB",
        "Megabit" to "Mb", "Megabyte" to "MB", "Mebibit" to "Mib",
        "Mebibyte" to "MiB", "Gigabit" to "Gb", "Gigabyte" to "GB",
        "Gibibit" to "Gib", "Gibibyte" to "GiB", "Terabit" to "Tb",
        "Terabyte" to "TB", "Tebibit" to "Tib", "Tebibyte" to "TiB",
        "Petabit" to "Pb", "Petabyte" to "PB", "Pebibit" to "Pib",
        "Pebibyte" to "PiB",
        // Pressure
        "Pascal" to "Pa", "Kilopascal" to "kPa", "Megapascal" to "MPa",
        "Bar" to "bar", "Millibar" to "mbar", "Atmosphere" to "atm",
        "PSI" to "psi", "mmHg" to "mmHg",
        // Heat/Energy
        "Joule" to "J", "Kilojoule" to "kJ", "Megajoule" to "MJ",
        "Calorie" to "cal", "Kilocalorie" to "kcal",
        "Watt-hour" to "Wh", "Kilowatt-hour" to "kWh",
        // Speed
        "Meter per second" to "m/s", "Kilometer per hour" to "km/h",
        "Kilometer per second" to "km/s", "Mile per hour" to "mph",
        "Knot" to "kn", "Mach" to "Mach", "Speed of light" to "c",
        // Time
        "Millisecond" to "ms", "Second" to "s", "Minute" to "min",
        "Hour" to "h", "Day" to "d", "Week" to "wk",
        "Month" to "mo", "Year" to "yr",
        // Angle
        "Degree" to "°", "Arcminute" to "'", "Arcsecond" to "\"",
        "Radian" to "rad", "Gradian" to "grad", "Turn" to "turn",
        // Power
        "Watt" to "W", "Kilowatt" to "kW", "Megawatt" to "MW",
        "Horsepower" to "hp", "Metric horsepower" to "PS",
        "Kilocalorie per second" to "kcal/s",
        "Newton-meter per second" to "Nm/s",
        "Kilogram-meter per second" to "kgm/s",
        "BTU per second" to "BTU/s",
        "Foot-pound per second" to "ft·lb/s",
        // Force
        "Newton" to "N", "Kilonewton" to "kN", "Dyne" to "dyn",
        "Pound-force" to "lbf", "Ounce-force" to "ozf",
        // Density
        "Kilogram per cubic meter" to "kg/m³",
        "Gram per cubic centimeter" to "g/cm³",
        "Pound per cubic foot" to "lb/ft³",
        "Pound per gallon" to "lb/gal",
        // Frequency
        "Hertz" to "Hz", "Kilohertz" to "kHz", "Megahertz" to "MHz",
        "Gigahertz" to "GHz",
        // Torque
        "Newton meter" to "N·m", "Kilonewton meter" to "kN·m",
        "Pound-foot" to "lb·ft", "Ounce-inch" to "oz·in",
        // Viscosity
        "Pascal-second" to "Pa·s", "Centipoise" to "cP",
        "Poise" to "P", "Poiseuille" to "Pl",
        // Fuel
        "Liter" to "L", "Gallon (US)" to "gal (US)",
        "Gallon (UK)" to "gal (UK)", "Barrel" to "bbl",
        "Cubic meter" to "m³",
        // Date
        "Days" to "d", "Weeks" to "wk", "Months" to "mo", "Years" to "yr",
        // BMI
        "kg/m²" to "kg/m²",
        // Shopping
        "Percent" to "%", "Currency" to "curr"
    )

    fun getAbbreviation(unitName: String): String {
        return unitAbbreviations[unitName] ?: unitName.take(3)
    }

    fun formatWithCommas(raw: String): String {
        val parts = raw.split(".")
        val integerPart = parts[0]
        val formattedInt = if (integerPart.isNotEmpty()) {
            val reversed = integerPart.reversed().chunked(3).joinToString(",")
            reversed.reversed()
        } else "0"
        return if (parts.size > 1) "$formattedInt.${parts[1]}" else formattedInt
    }

    fun parseFromDisplay(display: String): String {
        return display.replace(",", "")
    }

    fun updateInput(newText: String, cursorPosition: Int = newText.length) {
        val clean = parseFromDisplay(newText)
        if (clean.matches(Regex("-?\\d*\\.?\\d*"))) {
            val formatted = formatWithCommas(clean)
            val newCursor = cursorPosition.coerceIn(0, formatted.length)
            inputText = TextFieldValue(formatted, selection = TextRange(newCursor))
        }
    }

    fun insertDigit(digit: Char) {
        val text = inputText.text
        val cursor = inputText.selection.start
        val clean = parseFromDisplay(text)

        if ((clean == "0" || clean == "1") && digit != '.') {
            val formatted = formatWithCommas(digit.toString())
            inputText = TextFieldValue(formatted, selection = TextRange(formatted.length))
            return
        }

        val cleanCursor = text.substring(0, cursor).count { it != ',' }

        val before = clean.substring(0, cleanCursor)
        val after = clean.substring(cleanCursor)
        val newClean = before + digit + after

        val formatted = formatWithCommas(newClean)

        var cleanCount = 0
        var newCursor = 0
        for (i in formatted.indices) {
            if (formatted[i] != ',') {
                cleanCount++
                if (cleanCount == cleanCursor + 1) {
                    newCursor = i + 1
                    break
                }
            }
        }
        if (newCursor == 0) newCursor = formatted.length

        inputText = TextFieldValue(formatted, selection = TextRange(newCursor))
    }

    fun deleteChar() {
        val text = inputText.text
        val cursor = inputText.selection.start
        if (cursor > 0) {
            val clean = parseFromDisplay(text)
            val cleanCursor = clean.length - (text.length - cursor)
            val newClean = clean.substring(0, cleanCursor - 1) + clean.substring(cleanCursor)
            val formatted = formatWithCommas(newClean)
            val newCursor = formatted.length - (text.length - cursor)
            inputText = TextFieldValue(formatted.ifEmpty { "0" }, selection = TextRange(newCursor.coerceIn(0, formatted.length)))
        }
    }

    fun clearAll() {
        inputText = TextFieldValue("0", selection = TextRange(1))
    }

    fun toggleSign() {
        val text = inputText.text
        val clean = parseFromDisplay(text)
        val newClean = if (clean.startsWith("-")) clean.drop(1) else "-$clean"
        val formatted = formatWithCommas(newClean)
        inputText = TextFieldValue(formatted, selection = TextRange(formatted.length))
    }

    fun insertDecimal() {
        val text = inputText.text
        val clean = parseFromDisplay(text)
        if (!clean.contains('.')) {
            val cursor = inputText.selection.start
            val cleanCursor = text.substring(0, cursor).count { it != ',' }
            val before = clean.substring(0, cleanCursor)
            val after = clean.substring(cleanCursor)
            val newClean = "$before.$after"
            val formatted = formatWithCommas(newClean)
            val newCursor = cleanCursor + 1 + formatted.substring(0, cleanCursor + 1).count { it == ',' }
            inputText = TextFieldValue(formatted, selection = TextRange(newCursor.coerceIn(0, formatted.length)))
        }
    }

    fun copyToClipboard(text: String, label: String = "Copied") {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("converter", text))
        android.widget.Toast.makeText(context, label, android.widget.Toast.LENGTH_SHORT).show()
    }

    fun pasteFromClipboard() {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pasted = clip.getItemAt(0).text.toString()
            val cleaned = pasted
                .replace(Regex("[^0-9.\\-eE]"), "")
                .replace(Regex("\\s+"), "")
            if (cleaned.isNotEmpty()) {
                val value = cleaned.toDoubleOrNull()
                if (value != null) {
                    updateInput(cleaned, cleaned.length)
                    android.widget.Toast.makeText(context, "Pasted", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Invalid number", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(inputText.text, fromUnit, toUnit) {
        val clean = parseFromDisplay(inputText.text)
        val value = clean.toDoubleOrNull()
        if (value != null && fromUnit.isNotEmpty() && toUnit.isNotEmpty()) {
            val result = UnitConverter.convert(value, fromUnit, toUnit, category)
            if (result != null && result.isFinite() && !result.isNaN()) {
                val formattedResult = if (result == result.toLong().toDouble()) {
                    result.toLong().toString()
                } else {
                    val str = String.format("%.6f", result).trimEnd('0').trimEnd('.')
                    if (str.length > 15) str else formatWithCommas(str)
                }
                resultText = formattedResult
            } else {
                resultText = "—"
            }
        } else {
            resultText = "—"
        }
    }
    fun swapUnits() {
        val temp = fromUnit
        fromUnit = toUnit
        toUnit = temp
        scope.launch {
            swapRotation = 180f
            delay(200.milliseconds)
            swapRotation = 0f
        }
    }
    var showUnitPicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf("from") }

    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryDisplayName,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.functionButton)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.numberButton)
                            .clickable {
                                pickerTarget = "from"
                                showUnitPicker = true
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = fromUnit,
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val focusRequester = remember { FocusRequester() }
                    var hasFocus by remember { mutableStateOf(false) }

                    InterceptPlatformTextInput(
                        interceptor = { _, _ -> awaitCancellation() }
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { newValue ->
                                val clean = parseFromDisplay(newValue.text)
                                if (clean.matches(Regex("-?\\d*\\.?\\d*"))) {
                                    inputText = newValue
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = 32.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(colors.accentButton),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    hasFocus = focusState.isFocused
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            if (clipboardManager.hasPrimaryClip()) {
                                                pasteFromClipboard()
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { swapUnits() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = "Swap units",
                        tint = colors.accentButton,
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                rotationZ = swapRotation
                            }
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.functionButton)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.numberButton)
                            .clickable {
                                pickerTarget = "to"
                                showUnitPicker = true
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = toUnit,
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = resultText,
                                color = colors.textPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = getAbbreviation(toUnit),
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.wrapContentWidth()
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                copyToClipboard(resultText + " " + getAbbreviation(toUnit), "Copied")
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            if (showUnitPicker) {
                ModalBottomSheet(
                    onDismissRequest = { showUnitPicker = false },
                    containerColor = colors.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select unit",
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        units.forEach { unit ->
                            val isSelected = if (pickerTarget == "from") unit == fromUnit else unit == toUnit
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (pickerTarget == "from") {
                                            fromUnit = unit
                                        } else {
                                            toUnit = unit
                                        }
                                        showUnitPicker = false
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = unit,
                                    color = colors.textPrimary,
                                    fontSize = 16.sp
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.accentButton
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            NumericKeypad(
                onDigit = { digit -> insertDigit(digit) },
                onDelete = { deleteChar() },
                onClear = { clearAll() },
                onSign = { toggleSign() },
                onDecimal = { insertDecimal() },
                onDone = { keyboardController?.hide() },
                colors = colors
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}
@Composable
fun NumericKeypad(
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSign: () -> Unit,
    onDecimal: () -> Unit,
    onDone: () -> Unit,
    colors: CalculatorColors
) {
    val rows = listOf(
        listOf("7", "8", "9", "⌫"),
        listOf("4", "5", "6", "C"),
        listOf("1", "2", "3", "±"),
        listOf("00", "0", ".", "⏎")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val isNumber = label.matches(Regex("\\d+"))
                    val isAction = label == "⌫" || label == "C" || label == "±" || label == "⏎"
                    val isDecimal = label == "."
                    val bg = when {
                        isNumber -> colors.numberButton
                        isAction -> colors.functionButton
                        isDecimal -> colors.numberButton
                        else -> colors.numberButton
                    }
                    val textColor = when (label) {
                        "⌫", "C" -> colors.textSecondary
                        "⏎" -> colors.textPrimary
                        else -> colors.textPrimary
                    }
                    Button(
                        onClick = {
                            when (label) {
                                "⌫" -> onDelete()
                                "C" -> onClear()
                                "±" -> onSign()
                                "." -> onDecimal()
                                "⏎" -> onDone()
                                "00" -> { onDigit('0'); onDigit('0') }
                                else -> {
                                    if (label.length == 1 && label[0].isDigit()) {
                                        onDigit(label[0])
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bg,
                            contentColor = textColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = if (label == "±" || label == "⏎") 18.sp else 24.sp,
                            fontWeight = if (isAction) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}