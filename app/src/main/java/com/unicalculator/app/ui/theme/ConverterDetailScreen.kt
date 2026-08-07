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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unicalculator.app.calculator.UnitConverter
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ConverterDetailScreen(
    category: String,
    onBack: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val units = remember(category) {
        UnitConverter.getUnitsForCategory(category)
    }

    val defaultUnits = remember(category) {
        UnitConverter.getDefaultUnits(category)
    }

    val defaultFrom = defaultUnits?.first ?: units.firstOrNull() ?: ""
    val defaultTo = defaultUnits?.second ?: units.getOrElse(1) {
        units.firstOrNull() ?: ""
    }

    var fromUnit by remember(category) {
        mutableStateOf(defaultFrom)
    }

    var toUnit by remember(category) {
        mutableStateOf(defaultTo)
    }

    var inputText by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange(0)
            )
        )
    }

    var resultText by remember {
        mutableStateOf("")
    }

    var swapRotation by remember {
        mutableFloatStateOf(0f)
    }

    var fromDropdownExpanded by remember {
        mutableStateOf(false)
    }

    var toDropdownExpanded by remember {
        mutableStateOf(false)
    }

    val unitAbbreviations = remember {
        mapOf(
            "Nanometer" to "nm",
            "Micrometer" to "µm",
            "Millimeter" to "mm",
            "Centimeter" to "cm",
            "Decimeter" to "dm",
            "Meter" to "m",
            "Foot" to "ft",
            "Yard" to "yd",
            "Fathom" to "ftm",
            "Furlong" to "fur",
            "Kilometer" to "km",
            "Mile" to "mi",
            "Nautical Mile" to "nmi",
            "Astronomical Unit" to "AU",
            "Light-year" to "ly",
            "Parsec" to "pc",
            "Inch" to "in",
            "Square Millimeter" to "mm²",
            "Square Centimeter" to "cm²",
            "Square Decimeter" to "dm²",
            "Are" to "a",
            "Square Meter" to "m²",
            "Square Foot" to "ft²",
            "Square Yard" to "yd²",
            "Hectare" to "ha",
            "Acre" to "ac",
            "Square Kilometer" to "km²",
            "Square Mile" to "mi²",
            "Square Inch" to "in²",
            "Cubic Millimeter" to "mm³",
            "Cubic Centimeter" to "cm³",
            "Milliliter" to "mL",
            "Cubic Inch" to "in³",
            "Cubic Decimeter" to "dm³",
            "Liter" to "L",
            "Teaspoon (US)" to "tsp (US)",
            "Tablespoon (US)" to "tbsp (US)",
            "Fluid Ounce (US)" to "fl oz (US)",
            "Fluid Ounce (UK)" to "fl oz (UK)",
            "Cup (US)" to "cup (US)",
            "Pint (US)" to "pt (US)",
            "Pint (UK)" to "pt (UK)",
            "Quart (US)" to "qt (US)",
            "Quart (UK)" to "qt (UK)",
            "Cubic Foot" to "ft³",
            "Gallon (US)" to "gal (US)",
            "Gallon (UK)" to "gal (UK)",
            "Cubic Yard" to "yd³",
            "Cubic Meter" to "m³",
            "Kiloliter" to "kL",
            "Barrel (oil, US)" to "bbl",
            "Milligram" to "mg",
            "Gram" to "g",
            "Carat" to "ct",
            "Grain" to "gr",
            "Kilogram" to "kg",
            "Slug" to "slug",
            "Pound" to "lb",
            "Ounce" to "oz",
            "Stone" to "st",
            "Tonne (metric)" to "t",
            "Short Ton (US)" to "ton (US)",
            "Long Ton (UK)" to "ton (UK)",
            "Celsius" to "°C",
            "Fahrenheit" to "°F",
            "Kelvin" to "K",
            "Rankine" to "°R",
            "Bit" to "b",
            "Byte" to "B",
            "Kilobit" to "kb",
            "Kibibit" to "Kib",
            "Kilobyte" to "kB",
            "Kibibyte" to "KiB",
            "Megabit" to "Mb",
            "Mebibit" to "Mib",
            "Megabyte" to "MB",
            "Mebibyte" to "MiB",
            "Gigabit" to "Gb",
            "Gibibit" to "Gib",
            "Gigabyte" to "GB",
            "Gibibyte" to "GiB",
            "Terabit" to "Tb",
            "Tebibit" to "Tib",
            "Terabyte" to "TB",
            "Tebibyte" to "TiB",
            "Petabit" to "Pb",
            "Pebibit" to "Pib",
            "Petabyte" to "PB",
            "Pebibyte" to "PiB",
            "Exabit" to "Eb",
            "Exbibit" to "Eib",
            "Exabyte" to "EB",
            "Exbibyte" to "EiB",
            "bps" to "bit/s",
            "Kbps" to "kbit/s",
            "Byte per second" to "B/s",
            "KB/s (decimal)" to "kB/s",
            "Kibit/s" to "Kibit/s",
            "KiB/s" to "KiB/s",
            "Mbps" to "Mbit/s",
            "MB/s (decimal)" to "MB/s",
            "Mibit/s" to "Mibit/s",
            "MiB/s" to "MiB/s",
            "Gbps" to "Gbit/s",
            "GB/s (decimal)" to "GB/s",
            "Gibit/s" to "Gibit/s",
            "GiB/s" to "GiB/s",
            "Tbps" to "Tbit/s",
            "TB/s (decimal)" to "TB/s",
            "Pascal" to "Pa",
            "Millibar" to "mbar",
            "Kilopascal" to "kPa",
            "Kilogram-force per cm²" to "kgf/cm²",
            "mmHg" to "mmHg",
            "Torr" to "Torr",
            "inHg" to "inHg",
            "Bar" to "bar",
            "Atmosphere" to "atm",
            "PSI" to "psi",
            "Megapascal" to "MPa",
            "Electronvolt" to "eV",
            "Erg" to "erg",
            "Joule" to "J",
            "Foot-pound (energy)" to "ft·lbf",
            "Calorie (thermochemical)" to "cal",
            "Kilojoule" to "kJ",
            "Kilocalorie" to "kcal",
            "Watt-hour" to "Wh",
            "BTU" to "BTU",
            "Kilowatt-hour" to "kWh",
            "Megajoule" to "MJ",
            "Meter per second" to "m/s",
            "Foot per second" to "ft/s",
            "Kilometer per hour" to "km/h",
            "Knot" to "kn",
            "Mile per hour" to "mph",
            "Kilometer per second" to "km/s",
            "Speed of light" to "c",
            "Millisecond" to "ms",
            "Second" to "s",
            "Minute" to "min",
            "Hour" to "h",
            "Day" to "d",
            "Week" to "wk",
            "Fortnight" to "fn",
            "Month (average)" to "mo",
            "Quarter (average)" to "qtr",
            "Year (Julian)" to "yr",
            "Decade" to "dec",
            "Century" to "cent",
            "Arcsecond" to "″",
            "Arcminute" to "′",
            "Mil (NATO)" to "mil",
            "Degree" to "°",
            "Gradian" to "gon",
            "Radian" to "rad",
            "Turn" to "turn",
            "Milliwatt" to "mW",
            "Watt" to "W",
            "Kilowatt" to "kW",
            "Megawatt" to "MW",
            "Gigawatt" to "GW",
            "Horsepower (mechanical)" to "hp",
            "Horsepower (metric)" to "PS",
            "Foot-pound per second" to "ft·lbf/s",
            "Kilogram-force meter per second" to "kgf·m/s",
            "BTU per second" to "BTU/s",
            "Kilocalorie per second" to "kcal/s",
            "Dyne" to "dyn",
            "Poundal" to "pdl",
            "Ounce-force" to "ozf",
            "Newton" to "N",
            "Pound-force" to "lbf",
            "Kilogram-force" to "kgf",
            "Kilonewton" to "kN",
            "Gram per liter" to "g/L",
            "Kilogram per cubic meter" to "kg/m³",
            "Gram per cubic centimeter" to "g/cm³",
            "Pound per cubic foot" to "lb/ft³",
            "Pound per gallon (US)" to "lb/gal (US)",
            "Pound per cubic inch" to "lb/in³",
            "RPM (revolutions/min)" to "rpm",
            "Hertz" to "Hz",
            "Kilohertz" to "kHz",
            "Megahertz" to "MHz",
            "Gigahertz" to "GHz",
            "Terahertz" to "THz",
            "Ounce-inch" to "oz·in",
            "Pound-inch" to "lb·in",
            "Newton meter" to "N·m",
            "Pound-foot" to "lb·ft",
            "Kilogram-force meter" to "kgf·m",
            "Kilonewton meter" to "kN·m",
            "Centipoise" to "cP",
            "Poise" to "P",
            "Pascal-second" to "Pa·s",
            "Poiseuille" to "Pl",
            "Reyn" to "reyn",
            "L/100km" to "L/100 km",
            "km/L" to "km/L",
            "MPG (US)" to "mpg (US)",
            "MPG (UK)" to "mpg (UK)",
            "Milligray" to "mGy",
            "Centigray" to "cGy",
            "Rad" to "rad",
            "Gray" to "Gy",
            "Millisievert" to "mSv",
            "Rem" to "rem",
            "Sievert" to "Sv",
            "Lux" to "lx",
            "Foot-candle" to "fc",
            "dB SPL" to "dB SPL",
            "Twip" to "twip",
            "Point" to "pt",
            "Pica" to "pc",
            "Pixel (@96 DPI)" to "px",
            "mg/dL" to "mg/dL",
            "mmol/L" to "mmol/L"
        )
    }

    fun getAbbreviation(unitName: String): String {
        return unitAbbreviations[unitName] ?: unitName.take(3)
    }

    fun formatWithCommas(raw: String): String {
        if (raw.isEmpty()) return ""

        val negative = raw.startsWith("-")
        val number = if (negative) raw.substring(1) else raw
        val parts = number.split(".", limit = 2)

        val integerPart = parts[0]

        val formattedInteger = if (integerPart.isEmpty()) {
            "0"
        } else {
            integerPart
                .reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()
        }

        val result = if (parts.size == 2) {
            "$formattedInteger.${parts[1]}"
        } else {
            formattedInteger
        }

        return if (negative) "-$result" else result
    }

    fun parseFromDisplay(display: String): String {
        return display.replace(",", "")
    }

    fun getCleanCursorPosition(
        formattedText: String,
        cursorPosition: Int
    ): Int {
        return formattedText
            .substring(0, cursorPosition.coerceIn(0, formattedText.length))
            .count { it != ',' }
    }

    fun getFormattedCursorPosition(
        formattedText: String,
        cleanCursorPosition: Int
    ): Int {
        if (cleanCursorPosition <= 0) return 0

        var cleanCount = 0

        formattedText.forEachIndexed { index, char ->
            if (char != ',') {
                cleanCount++

                if (cleanCount == cleanCursorPosition) {
                    return index + 1
                }
            }
        }

        return formattedText.length
    }

    fun updateInput(
        newText: String,
        cursorPosition: Int = newText.length
    ) {
        val clean = parseFromDisplay(newText)

        if (
            clean.isEmpty() ||
            clean.matches(Regex("-?\\d*\\.?\\d*"))
        ) {
            val formatted = formatWithCommas(clean)
            val cleanCursor = cursorPosition
                .coerceAtMost(newText.length)
                .let {
                    getCleanCursorPosition(newText, it)
                }

            val formattedCursor = getFormattedCursorPosition(
                formatted,
                cleanCursor
            )

            inputText = TextFieldValue(
                text = formatted,
                selection = TextRange(formattedCursor)
            )
        }
    }

    fun insertDigit(digit: Char) {
        val display = inputText.text
        val cursor = inputText.selection.start
        val clean = parseFromDisplay(display)
        val cleanCursor = getCleanCursorPosition(display, cursor)

        val newClean = if (clean.isEmpty()) {
            digit.toString()
        } else {
            clean.substring(0, cleanCursor) +
                    digit +
                    clean.substring(cleanCursor)
        }

        val formatted = formatWithCommas(newClean)

        val newCursor = getFormattedCursorPosition(
            formatted,
            cleanCursor + 1
        )

        inputText = TextFieldValue(
            text = formatted,
            selection = TextRange(newCursor)
        )
    }

    fun deleteChar() {
        val display = inputText.text
        val cursor = inputText.selection.start

        if (cursor <= 0) return

        val clean = parseFromDisplay(display)
        val cleanCursor = getCleanCursorPosition(display, cursor)

        if (cleanCursor <= 0) return

        val deleteIndex = cleanCursor - 1

        val newClean =
            clean.removeRange(deleteIndex, deleteIndex + 1)

        if (newClean.isEmpty() || newClean == "-") {
            inputText = TextFieldValue(
                text = "",
                selection = TextRange(0)
            )
            return
        }

        val formatted = formatWithCommas(newClean)
        val newCursor = getFormattedCursorPosition(
            formatted,
            deleteIndex
        )

        inputText = TextFieldValue(
            text = formatted,
            selection = TextRange(newCursor)
        )
    }

    fun clearAll() {
        inputText = TextFieldValue(
            text = "",
            selection = TextRange(0)
        )
    }

    fun toggleSign() {
        val clean = parseFromDisplay(inputText.text)

        if (clean.isEmpty()) {
            inputText = TextFieldValue(
                text = "-",
                selection = TextRange(1)
            )
            return
        }

        val newClean = if (clean.startsWith("-")) {
            clean.substring(1)
        } else {
            "-$clean"
        }

        val formatted = formatWithCommas(newClean)

        inputText = TextFieldValue(
            text = formatted,
            selection = TextRange(formatted.length)
        )
    }

    fun insertDecimal() {
        val display = inputText.text
        val clean = parseFromDisplay(display)

        if (clean.contains(".")) return

        val cursor = inputText.selection.start
        val cleanCursor = getCleanCursorPosition(display, cursor)

        val newClean = if (clean.isEmpty()) {
            "0."
        } else {
            clean.substring(0, cleanCursor) +
                    "." +
                    clean.substring(cleanCursor)
        }

        val formatted = formatWithCommas(newClean)

        val newCursor = getFormattedCursorPosition(
            formatted,
            cleanCursor + 1
        )

        inputText = TextFieldValue(
            text = formatted,
            selection = TextRange(newCursor)
        )
    }

    fun copyToClipboard(
        text: String,
        label: String = "Copied"
    ) {
        if (text.isBlank()) return

        clipboardManager.setPrimaryClip(
            ClipData.newPlainText("converter", text)
        )

        android.widget.Toast
            .makeText(
                context,
                label,
                android.widget.Toast.LENGTH_SHORT
            )
            .show()
    }

    fun pasteFromClipboard() {
        val clip = clipboardManager.primaryClip ?: return

        if (clip.itemCount <= 0) return

        val pasted = clip
            .getItemAt(0)
            .coerceToText(context)
            .toString()
            .trim()

        val cleaned = pasted
            .replace(",", "")
            .replace(" ", "")

        val value = cleaned.toDoubleOrNull()

        if (value == null || !value.isFinite()) {
            android.widget.Toast
                .makeText(
                    context,
                    "Invalid number",
                    android.widget.Toast.LENGTH_SHORT
                )
                .show()
            return
        }

        val normalized = when {
            cleaned.startsWith("+") -> cleaned.substring(1)
            else -> cleaned
        }

        updateInput(
            normalized,
            normalized.length
        )

        android.widget.Toast
            .makeText(
                context,
                "Pasted",
                android.widget.Toast.LENGTH_SHORT
            )
            .show()
    }

    LaunchedEffect(
        inputText.text,
        fromUnit,
        toUnit,
        category
    ) {
        val clean = parseFromDisplay(inputText.text)
        val value = clean.toDoubleOrNull()

        if (
            value != null &&
            value.isFinite() &&
            fromUnit.isNotEmpty() &&
            toUnit.isNotEmpty()
        ) {
            val result = UnitConverter.convert(
                value = value,
                fromUnit = fromUnit,
                toUnit = toUnit,
                category = category
            )

            resultText = if (
                result != null &&
                result.isFinite()
            ) {
                formatResult(result)
            } else {
                ""
            }
        } else {
            resultText = ""
        }
    }

    fun swapUnits() {
        val oldFrom = fromUnit

        fromUnit = toUnit
        toUnit = oldFrom

        scope.launch {
            swapRotation = 180f
            delay(200.milliseconds)
            swapRotation = 0f
        }
    }

    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector =
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
                colors = CardDefaults.cardColors(
                    containerColor = colors.functionButton
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    UnitDropdownPill(
                        label = fromUnit,
                        units = units,
                        expanded = fromDropdownExpanded,
                        onExpandedChange = {
                            fromDropdownExpanded = it
                        },
                        onUnitSelected = {
                            fromUnit = it
                        },
                        colors = colors
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    val focusRequester = remember {
                        FocusRequester()
                    }

                    var hasFocus by remember {
                        mutableStateOf(false)
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    InterceptPlatformTextInput(
                        interceptor = { _, _ ->
                            awaitCancellation()
                        }
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { newValue ->
                                val clean =
                                    parseFromDisplay(newValue.text)

                                if (
                                    clean.isEmpty() ||
                                    clean.matches(
                                        Regex("-?\\d*\\.?\\d*")
                                    )
                                ) {
                                    val cleanCursor =
                                        getCleanCursorPosition(
                                            newValue.text,
                                            newValue.selection.start
                                        )

                                    val formatted =
                                        formatWithCommas(clean)

                                    val formattedCursor =
                                        getFormattedCursorPosition(
                                            formatted,
                                            cleanCursor
                                        )

                                    inputText = TextFieldValue(
                                        text = formatted,
                                        selection =
                                            TextRange(formattedCursor)
                                    )
                                }
                            },
                            textStyle = TextStyle(
                                fontSize = 32.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush =
                                SolidColor(colors.accentButton),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    hasFocus = it.isFocused
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            if (
                                                clipboardManager
                                                    .hasPrimaryClip()
                                            ) {
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
                horizontalArrangement =
                    Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        swapUnits()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
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
                colors = CardDefaults.cardColors(
                    containerColor = colors.functionButton
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    UnitDropdownPill(
                        label = toUnit,
                        units = units,
                        expanded = toDropdownExpanded,
                        onExpandedChange = {
                            toDropdownExpanded = it
                        },
                        onUnitSelected = {
                            toUnit = it
                        },
                        colors = colors
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.End,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .horizontalScroll(
                                    rememberScrollState()
                                )
                        ) {
                            Text(
                                text = resultText,
                                color = colors.textPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = getAbbreviation(toUnit),
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        IconButton(
                            onClick = {
                                copyToClipboard(
                                    resultText +
                                            " " +
                                            getAbbreviation(toUnit)
                                )
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.ContentCopy,
                                contentDescription =
                                    "Copy result",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            NumericKeypad(
                onDigit = {
                    insertDigit(it)
                },
                onDelete = {
                    deleteChar()
                },
                onClear = {
                    clearAll()
                },
                onSign = {
                    toggleSign()
                },
                onDecimal = {
                    insertDecimal()
                },
                onDone = {
                    keyboardController?.hide()
                },
                colors = colors
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatResult(
    result: Double
): String {
    if (!result.isFinite()) return ""

    if (result == 0.0) {
        return "0"
    }

    val absolute = kotlin.math.abs(result)

    if (
        absolute !in 1e-6..<1e15
    ) {
        return String.format(
            "%.6e",
            result
        )
            .replace(
                Regex("0+e"),
                "e"
            )
            .replace(
                Regex("\\.0+e"),
                "e"
            )
    }

    val rounded =
        String.format(
            "%.10f",
            result
        )
            .trimEnd('0')
            .trimEnd('.')

    val parts = rounded.split(
        ".",
        limit = 2
    )

    val integerPart = parts[0]
    val decimalPart =
        if (parts.size > 1) parts[1] else ""

    val negative = integerPart.startsWith("-")
    val unsignedInteger =
        if (negative) {
            integerPart.substring(1)
        } else {
            integerPart
        }

    val formattedInteger =
        if (unsignedInteger.isEmpty()) {
            "0"
        } else {
            unsignedInteger
                .reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()
        }

    return buildString {
        if (negative) {
            append("-")
        }

        append(formattedInteger)

        if (decimalPart.isNotEmpty()) {
            append(".")
            append(decimalPart)
        }
    }
}

@Composable
private fun UnitDropdownPill(
    label: String,
    units: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onUnitSelected: (String) -> Unit,
    colors: CalculatorColors
) {
    val shape =
        if (expanded) {
            RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            )
        } else {
            RoundedCornerShape(28.dp)
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(
                Alignment.Start
            )
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(colors.numberButton)
                .clickable {
                    onExpandedChange(!expanded)
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Icon(
                imageVector =
                    if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            },
            modifier = Modifier
                .widthIn(max = 250.dp)
                .heightIn(max = 300.dp),
            offset = DpOffset(
                x = 0.dp,
                y = 0.dp
            ),
            shape = RoundedCornerShape(
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            containerColor = colors.background,
            tonalElevation = 0.dp
        ) {
            units.forEach { unit ->
                val selected = unit == label

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selected) {
                                colors.functionButton
                            } else {
                                Color.Transparent
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onUnitSelected(unit)
                            onExpandedChange(false)
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text = unit,
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.Medium
                    )

                    if (selected) {
                        Icon(
                            imageVector =
                                Icons.Default.Check,
                            contentDescription = null,
                            tint =
                                colors.textPrimary,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }
                }
            }
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
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val isNumber =
                        label.matches(
                            Regex("\\d+")
                        )

                    val isAction =
                        label == "⌫" ||
                                label == "C" ||
                                label == "±" ||
                                label == "⏎"

                    val background =
                        when {
                            isNumber ->
                                colors.numberButton

                            isAction ->
                                colors.functionButton

                            else ->
                                colors.numberButton
                        }

                    val textColor =
                        when (label) {
                            "⌫", "C" ->
                                colors.textSecondary

                            else ->
                                colors.textPrimary
                        }

                    Button(
                        onClick = {
                            when (label) {
                                "⌫" ->
                                    onDelete()

                                "C" ->
                                    onClear()

                                "±" ->
                                    onSign()

                                "." ->
                                    onDecimal()

                                "⏎" ->
                                    onDone()

                                "00" -> {
                                    onDigit('0')
                                    onDigit('0')
                                }

                                else -> {
                                    if (
                                        label.length == 1 &&
                                        label[0].isDigit()
                                    ) {
                                        onDigit(label[0])
                                    }
                                }
                            }
                        },
                        shape =
                            RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    background,
                                contentColor =
                                    textColor
                            ),
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize =
                                if (
                                    label == "±" ||
                                    label == "⏎"
                                ) {
                                    18.sp
                                } else {
                                    24.sp
                                },
                            fontWeight =
                                if (isAction) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                        )
                    }
                }
            }
        }
    }
}