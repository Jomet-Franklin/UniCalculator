package com.unicalculator.app

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

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import com.unicalculator.app.calculator.StandardCalculatorEngine
import com.unicalculator.app.calculator.QalculateEngine
import com.unicalculator.app.calculator.HistoryItem
import com.unicalculator.app.calculator.HistoryManager
import com.unicalculator.app.ui.screens.LicensesScreen
import com.unicalculator.app.ui.theme.LocalCalculatorColors
import com.unicalculator.app.ui.theme.UniCalculatorTheme
import com.unicalculator.app.ui.theme.AboutScreen
import com.unicalculator.app.ui.theme.ConverterDetailScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.InterceptPlatformTextInput
import kotlinx.coroutines.awaitCancellation
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.jherkenhoff.libqalculate.AngleUnit
import androidx.compose.runtime.saveable.rememberSaveable
import com.unicalculator.app.ui.theme.SettingsScreen
import androidx.compose.ui.unit.DpOffset

// ========== HELPER FUNCTIONS & DATA ==========
@Composable
fun getModeIcon(mode: CalculatorMode, isDarkTheme: Boolean): Int {
    return when (mode) {
        CalculatorMode.SCIENTIFIC -> if (isDarkTheme) R.drawable.ic_scientific_dark else R.drawable.ic_scientific_light
        CalculatorMode.STANDARD   -> if (isDarkTheme) R.drawable.ic_standard_dark else R.drawable.ic_standard_light
        CalculatorMode.CONVERTER  -> if (isDarkTheme) R.drawable.ic_converter_dark else R.drawable.ic_converter_light
    }
}

data class ConverterCategory(
    val id: String,
    val name: String,
    val icon: String,
    val units: List<String>
)

val converterCategories = listOf(
    ConverterCategory("length", "Length", "📏", listOf("Nanometer", "Micrometer", "Millimeter", "Centimeter", "Decimeter", "Meter", "Kilometer", "Mile", "Yard", "Foot", "Inch", "Nautical Mile")),
    ConverterCategory("area", "Area", "📐", listOf("Square Millimeter", "Square Centimeter", "Square Decimeter", "Square Meter", "Square Kilometer", "Hectare", "Acre", "Square Yard", "Square Foot", "Square Inch")),
    ConverterCategory("volume", "Volume", "🧊", listOf("Cubic Millimeter", "Cubic Centimeter", "Cubic Decimeter", "Cubic Meter", "Liter", "Milliliter", "Kiloliter", "Gallon", "Quart", "Pint", "Cup", "Fluid Ounce", "Tablespoon", "Teaspoon")),
    ConverterCategory("mass", "Mass", "⚖️", listOf("Milligram", "Gram", "Kilogram", "Tonne", "Pound", "Ounce", "Stone")),
    ConverterCategory("temperature", "Temperature", "🌡️", listOf("Celsius", "Fahrenheit", "Kelvin")),
    ConverterCategory("storage", "Storage", "💾", listOf("Bit", "Byte", "Kilobit", "Kilobyte", "Kibibit", "Kibibyte", "Megabit", "Megabyte", "Mebibit", "Mebibyte", "Gigabit", "Gigabyte", "Gibibit", "Gibibyte", "Terabit", "Terabyte", "Tebibit", "Tebibyte", "Petabit", "Petabyte", "Pebibit", "Pebibyte")),
    ConverterCategory("pressure", "Pressure", "💨", listOf("Pascal", "Kilopascal", "Megapascal", "Bar", "Millibar", "Atmosphere", "PSI", "mmHg")),
    ConverterCategory("heat", "Heat/Energy", "🔥", listOf("Joule", "Kilojoule", "Megajoule", "Calorie", "Kilocalorie", "Watt-hour", "Kilowatt-hour")),
    ConverterCategory("speed", "Speed", "🚀", listOf("Meter per second", "Kilometer per hour", "Kilometer per second", "Mile per hour", "Knot", "Mach", "Speed of light")),
    ConverterCategory("time", "Time", "⏰", listOf("Millisecond", "Second", "Minute", "Hour", "Day", "Week", "Month", "Year")),
    ConverterCategory("angle", "Angle", "📐", listOf("Degree", "Arcminute", "Arcsecond", "Radian", "Gradian", "Turn")),
    ConverterCategory("power", "Power", "⚡", listOf("Watt", "Kilowatt", "Megawatt", "Horsepower", "Metric horsepower", "Kilocalorie per second", "Newton-meter per second", "Kilogram-meter per second", "BTU per second", "Foot-pound per second")),
    ConverterCategory("force", "Force", "💪", listOf("Newton", "Kilonewton", "Dyne", "Pound-force", "Ounce-force")),
    ConverterCategory("density", "Density", "📊", listOf("Kilogram per cubic meter", "Gram per cubic centimeter", "Pound per cubic foot", "Pound per gallon")),
    ConverterCategory("frequency", "Frequency", "📶", listOf("Hertz", "Kilohertz", "Megahertz", "Gigahertz")),
    ConverterCategory("torque", "Torque", "🔧", listOf("Newton meter", "Kilonewton meter", "Pound-foot", "Ounce-inch")),
    ConverterCategory("viscosity", "Viscosity", "🧴", listOf("Pascal-second", "Centipoise", "Poise", "Poiseuille")),
    ConverterCategory("fuel", "Fuel", "⛽", listOf("Liter", "Gallon (US)", "Gallon (UK)", "Barrel", "Cubic meter")),
    ConverterCategory("date", "Date", "📅", listOf("Days", "Weeks", "Months", "Years")),
    ConverterCategory("bmi", "BMI", "💪", listOf("kg/m²")),
    ConverterCategory("shopping", "Shopping", "🛒", listOf("Percent", "Currency")),
   )

enum class KeyStyle { NUMBER, FUNCTION, ACCENT, SCIENTIFIC }

data class KeyDef(
    val label: String,
    val style: KeyStyle,
    val secondaryLabel: String? = null,
    val isDropdown: Boolean = false,
    val dropdownItems: List<String>? = null,
    val span: Int = 1
)

// ========== COLOR HELPER FOR INTERPRETED OUTPUT ==========
private fun colorizeParsedExpression(text: String): AnnotatedString {
    val cyan = Color(0xFF84D2E6)
    val functionGray = Color(0xFFB6BDBF)
    val unitGray = Color(0xFFA7AEC7)

    val pattern = Regex("""\d+(?:\.\d+)?|[a-zA-Z]+\(|[()]|·|\S+""")
    val tokens = pattern.findAll(text).toList()

    return buildAnnotatedString {
        var lastIndex = 0
        for (token in tokens) {
            if (token.range.first > lastIndex) {
                append(text.substring(lastIndex, token.range.first))
            }
            lastIndex = token.range.last + 1

            val value = token.value
            val color = when {
                value.matches(Regex("""\d+(?:\.\d+)?""")) || value == "·" -> cyan
                value.matches(Regex("""[a-zA-Z]+\(""")) || value in listOf("(", ")") -> functionGray
                else -> unitGray
            }
            pushStyle(SpanStyle(color = color))
            append(value)
            pop()
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

// ========== SCIENTIFIC KEY HANDLING ==========
private val scientificInsertMap = mapOf(
    "sin" to "sin(",
    "cos" to "cos(",
    "tan" to "tan(",
    "ln" to "ln(",
    "log" to "log(",
    "asin" to "asin(",
    "acos" to "acos(",
    "atan" to "atan(",
    "√" to "sqrt(",
    "π" to "π",
    "e" to "e",
    "∞" to "∞",
    "x²" to "^2",
    "xʸ" to "^",
    "!" to "!",
    "%" to "%",
    "∫" to "integral(",
    "dx" to "diff(",
    "Σ" to "sum(",
    "Π" to "product(",
    "i" to "i",
    "∠" to "∠",
    "(" to "(",
    ")" to ")",
    "[" to "[",
    "]" to "]",
    "_" to "_",
    ";" to ";",
    "=" to "=",
    "," to ",",
    "E" to "E",
    "0" to "0", "1" to "1", "2" to "2", "3" to "3", "4" to "4",
    "5" to "5", "6" to "6", "7" to "7", "8" to "8", "9" to "9",
    "." to ".",
    "+" to "+", "-" to "-", "·" to "·", "/" to "/",
    "X" to "X", "Y" to "Y", "Z" to "Z",
    "giga" to "G",
    "mega" to "M",
    "kilo" to "k",
    "milli" to "m",
    "micro" to "µ",
    "nano" to "n",
    "pico" to "p",
    "ampere" to "A",
    "gram" to "g",
    "joule" to "J",
    "kelvin" to "K",
    "liter" to "L",
    "meter" to "m",
    "newton" to "N",
    "ohm" to "Ω",
    "pascal" to "Pa",
    "second" to "s",
    "volt" to "V",
    "watt" to "W",
    "Abs." to "abs(",
    "Arg." to "arg(",
    "Real" to "re(",
    "Imag." to "im(",
    "Conj." to "conj("
)

private fun scientificHandleKeyPress(
    current: TextFieldValue,
    label: String,
    justEvaluated: Boolean
): TextFieldValue {
    val text = current.text
    val start = current.selection.start.coerceIn(0, text.length)
    val end = current.selection.end.coerceIn(0, text.length)

    return when {
        label == "AC" -> TextFieldValue("0", TextRange(1))
        label == "⌫" -> {
            if (start == 0 && end == 0) return current
            val newText = if (start != end) {
                text.substring(0, start) + text.substring(end)
            } else {
                text.substring(0, start - 1) + text.substring(start)
            }
            val newCursor = if (start != end) start else (start - 1).coerceAtLeast(0)
            TextFieldValue(if (newText.isEmpty()) "0" else newText, TextRange(newCursor))
        }
        justEvaluated && label.length == 1 && label[0].isDigit() -> {
            TextFieldValue(label, TextRange(label.length))
        }
        text == "0" -> {
            val insert = scientificInsertMap[label] ?: label
            TextFieldValue(insert, TextRange(insert.length))
        }
        else -> {
            val insert = scientificInsertMap[label] ?: label
            val newText = text.substring(0, start) + insert + text.substring(end)
            val newCursor = start + insert.length
            TextFieldValue(newText, TextRange(newCursor))
        }
    }
}

// ========== MAIN ACTIVITY ==========
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        HistoryManager.init(this)
        setContent {
            var selectedTheme by rememberSaveable { mutableStateOf("System") }
            var isPureBlackEnabled by rememberSaveable { mutableStateOf(false) }

            val darkTheme = when (selectedTheme) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            UniCalculatorTheme(
                darkTheme = darkTheme,
                pureBlack = isPureBlackEnabled
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UniCalculatorApp(
                        modifier = Modifier.padding(innerPadding),
                        selectedTheme = selectedTheme,
                        onThemeChanged = { theme -> selectedTheme = theme },
                        isPureBlackEnabled = isPureBlackEnabled,
                        onPureBlackChanged = { pureBlack -> isPureBlackEnabled = pureBlack }
                    )
                }
            }
        }
    }
}

// ========== APP NAVIGATION ==========
enum class CalculatorMode { SCIENTIFIC, STANDARD, CONVERTER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniCalculatorApp(
    modifier: Modifier = Modifier,
    selectedTheme: String,
    onThemeChanged: (String) -> Unit,
    isPureBlackEnabled: Boolean,
    onPureBlackChanged: (Boolean) -> Unit
) {
    val colors = LocalCalculatorColors.current

    var currentIndex by remember { mutableStateOf(1) }
    val modes = listOf(CalculatorMode.SCIENTIFIC, CalculatorMode.STANDARD, CalculatorMode.CONVERTER)

    var showHistory by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }
    var isFunctionExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var showConverterDetail by remember { mutableStateOf(false) }
    var selectedConverterCategory by remember { mutableStateOf("") }

    var standardTextFieldValue by remember { mutableStateOf(TextFieldValue("0")) }
    var scientificTextFieldValue by remember { mutableStateOf(TextFieldValue("0")) }

    var dragOffset by remember { mutableStateOf(0f) }
    val threshold = 150f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (!showConverterDetail) {
                            when {
                                dragOffset < -threshold && currentIndex < 2 -> currentIndex++
                                dragOffset > threshold && currentIndex > 0 -> currentIndex--
                            }
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (!showConverterDetail) {
                            dragOffset += dragAmount
                        }
                    }
                )
            }
    ) {
        val currentMode = modes[currentIndex]
        when {
            showLicenses -> LicensesScreen(
                onBack = { showLicenses = false }
            )
            showAbout -> AboutScreen(
                onBack = { showAbout = false },
                onLicensesClick = {
                    showLicenses = true
                }
            )
            showSettings -> SettingsScreen(
                onBack = { showSettings = false },
                onThemeChanged = onThemeChanged,
                onPureBlackChanged = onPureBlackChanged
            )
            showConverterDetail -> {
                when (selectedConverterCategory) {
                    "bmi" -> BMIScreen(onBack = { showConverterDetail = false })
                    "shopping" -> ShoppingScreen(onBack = { showConverterDetail = false })
                    else -> ConverterDetailScreen(
                        category = selectedConverterCategory,
                        onBack = { showConverterDetail = false }
                    )
                }
            }
            currentMode == CalculatorMode.STANDARD -> {
                StandardCalculatorScreen(
                    textFieldValue = standardTextFieldValue,
                    onTextFieldValueChange = { newValue -> standardTextFieldValue = newValue },
                    isFunctionExpanded = isFunctionExpanded,
                    onToggleFunction = { isFunctionExpanded = !isFunctionExpanded },
                    onModeChange = { newMode -> currentIndex = modes.indexOf(newMode) },
                    onHistoryClick = { showHistory = true },
                    onMenuItemClick = { item ->
                        when (item) {
                            "About" -> showAbout = true
                            "Settings" -> showSettings = true
                        }
                    }
                )
            }
            currentMode == CalculatorMode.CONVERTER -> {
                ConverterScreen(
                    onModeChange = { newMode -> currentIndex = modes.indexOf(newMode) },
                    onBack = { currentIndex = 1 },
                    onCategoryClick = { category ->
                        selectedConverterCategory = category
                        showConverterDetail = true
                    },
                    onMenuItemClick = { item ->
                        when (item) {
                            "About" -> showAbout = true
                            "Settings" -> showSettings = true
                        }
                    }
                )
            }
            currentMode == CalculatorMode.SCIENTIFIC -> {
                ScientificCalculatorScreen(
                    textFieldValue = scientificTextFieldValue,
                    onTextFieldValueChange = { newValue -> scientificTextFieldValue = newValue },
                    onModeChange = { newMode -> currentIndex = modes.indexOf(newMode) },
                    onBack = { currentIndex = 1 },
                    onHistoryClick = { showHistory = true },
                    onMenuItemClick = { item ->
                        when (item) {
                            "About" -> showAbout = true
                            "Settings" -> showSettings = true
                        }
                    }
                )
            }
        }

        if (showHistory) {
            ModalBottomSheet(
                onDismissRequest = { showHistory = false },
                sheetState = sheetState,
                containerColor = colors.displayBackground
            ) {
                HistoryContent(
                    onDismiss = { showHistory = false },
                    currentMode = currentMode.name,
                    onExpressionSelected = { expr, mode ->
                        if (currentMode.name == "STANDARD") {
                            val currentValue = standardTextFieldValue
                            val currentText = currentValue.text
                            val cursorPos = currentValue.selection.start
                            val newText = if (currentText == "0") expr
                            else currentText.substring(0, cursorPos) + expr + currentText.substring(cursorPos)
                            val newCursor = if (currentText == "0") expr.length else cursorPos + expr.length
                            standardTextFieldValue = TextFieldValue(newText, TextRange(newCursor))
                        } else {
                            val currentValue = scientificTextFieldValue
                            val currentText = currentValue.text
                            val cursorPos = currentValue.selection.start
                            val newText = if (currentText == "0") expr
                            else currentText.substring(0, cursorPos) + expr + currentText.substring(cursorPos)
                            val newCursor = if (currentText == "0") expr.length else cursorPos + expr.length
                            scientificTextFieldValue = TextFieldValue(newText, TextRange(newCursor))
                        }
                        showHistory = false
                    }
                )
            }
        }
    }
}

// ========== TOP BAR ==========
@Composable
fun CalculatorTopBar(
    mode: CalculatorMode,
    onModeChange: (CalculatorMode) -> Unit,
    onHistoryClick: (() -> Unit)? = null,
    onMenuItemClick: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    var menuExpanded by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Menu button (pill‑shaped, no shadow)
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.functionButton.copy(alpha = 0.5f))
                    .clickable { menuExpanded = true }
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .background(colors.background, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = colors.background,
                tonalElevation = 0.dp
            ) {
                DropdownMenuItem(
                    text = { Text("Settings", color = colors.textPrimary) },
                    onClick = {
                        menuExpanded = false
                        onMenuItemClick("Settings")
                    },
                    // ✅ Removed leadingIcon
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(4.dp)
                )
                DropdownMenuItem(
                    text = { Text("About", color = colors.textPrimary) },
                    onClick = {
                        menuExpanded = false
                        onMenuItemClick("About")
                    },
                    // ✅ Removed leadingIcon
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(4.dp)
                )
            }
        }

        // Mode icons (unchanged)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center)
        ) {
            val modes = listOf(
                CalculatorMode.SCIENTIFIC to "Scientific",
                CalculatorMode.STANDARD to "Standard",
                CalculatorMode.CONVERTER to "Converter"
            )
            modes.forEach { (m, name) ->
                val isSelected = m == mode
                val iconRes = getModeIcon(m, isDarkTheme)
                IconButton(
                    onClick = { onModeChange(m) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isSelected) colors.toolbarActive.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = name,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) colors.toolbarActive else colors.textSecondary
                    )
                }
            }
        }

        // History button (unchanged)
        if (onHistoryClick != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ========== STANDARD CALCULATOR SCREEN ==========
@Composable
fun StandardCalculatorScreen(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    isFunctionExpanded: Boolean,
    onToggleFunction: () -> Unit,
    onModeChange: (CalculatorMode) -> Unit,
    onHistoryClick: () -> Unit,
    onMenuItemClick: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    var justEvaluated by remember { mutableStateOf(false) }
    var keyboardEnabled by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val expression = textFieldValue.text
    val preview = StandardCalculatorEngine.evaluateExpressionOrBlank(expression)

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        CalculatorTopBar(
            mode = CalculatorMode.STANDARD,
            onModeChange = onModeChange,
            onHistoryClick = onHistoryClick,
            onMenuItemClick = onMenuItemClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .background(colors.displayBackground)
        ) {
            EditableDisplay(
                textFieldValue = textFieldValue,
                preview = preview,
                onTextFieldValueChange = onTextFieldValueChange,
                onPaste = { pastedText ->
                    val newText = textFieldValue.text.substring(0, textFieldValue.selection.start) +
                            pastedText + textFieldValue.text.substring(textFieldValue.selection.end)
                    val newCursor = textFieldValue.selection.start + pastedText.length
                    onTextFieldValueChange(TextFieldValue(newText, TextRange(newCursor)))
                },
                modifier = Modifier.weight(0.7f),
                isScientific = false,
                keyboardEnabled = keyboardEnabled
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(colors.displayDivider)
            )
        }

        CompactFunctionRow(
            isExpanded = isFunctionExpanded,
            onToggle = onToggleFunction,
            onFunctionClick = { func ->
                val newValue = standardHandleButtonPress(textFieldValue, func, justEvaluated)
                onTextFieldValueChange(newValue)
                justEvaluated = false
                keyboardEnabled = false
                keyboardController?.hide()
            }
        )

        StandardKeypad(
            onKeyPress = { label ->
                if (label == "=") {
                    val result = solveEquation(expression)
                    val newValue = TextFieldValue(result, TextRange(result.length))
                    onTextFieldValueChange(newValue)
                    HistoryManager.addEntry(expression, result, "STANDARD")
                    justEvaluated = true
                } else {
                    val newValue = standardHandleButtonPress(textFieldValue, label, justEvaluated)
                    onTextFieldValueChange(newValue)
                    justEvaluated = false
                }
                keyboardEnabled = false
                keyboardController?.hide()
            }
        )
    }
}

// ========== STANDARD KEY HANDLING ==========
private fun standardHandleButtonPress(current: TextFieldValue, label: String, justEvaluated: Boolean): TextFieldValue {
    val text = current.text
    val start = current.selection.start.coerceIn(0, text.length)
    val end = current.selection.end.coerceIn(0, text.length)

    return when {
        label == "AC" -> TextFieldValue("0", TextRange(1))
        label == "=" -> {
            val result = StandardCalculatorEngine.evaluateExpression(text)
            TextFieldValue(result, TextRange(result.length))
        }
        label == "⌫" -> {
            if (start == 0 && end == 0) return current
            val newText = if (start != end) {
                text.substring(0, start) + text.substring(end)
            } else {
                text.substring(0, start - 1) + text.substring(start)
            }
            val newCursor = if (start != end) start else (start - 1).coerceAtLeast(0)
            TextFieldValue(if (newText.isEmpty()) "0" else newText, TextRange(newCursor))
        }
        justEvaluated && label.length == 1 && label[0].isDigit() -> {
            TextFieldValue(label, TextRange(label.length))
        }
        else -> {
            val transformed = StandardCalculatorEngine.handleKeyPress("", label)
            val newText = if (text == "0") {
                transformed
            } else {
                text.substring(0, start) + transformed + text.substring(end)
            }
            val newCursor = if (text == "0") transformed.length else start + transformed.length
            TextFieldValue(newText, TextRange(newCursor))
        }
    }
}

// ========== COMPACT FUNCTION ROW ==========
@Composable
fun CompactFunctionRow(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onFunctionClick: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val isDarkTheme = isSystemInDarkTheme()
    val gridFunctions = listOf(
        "sin", "cos", "tan", "log", "ln",
        "sin⁻¹", "cos⁻¹", "tan⁻¹", "exp"
    )
    val toggleIconRes = when {
        isExpanded && isDarkTheme -> R.drawable.ic_flask_up_dark
        isExpanded && !isDarkTheme -> R.drawable.ic_flask_up_light
        !isExpanded && isDarkTheme -> R.drawable.ic_flask_down_dark
        else -> R.drawable.ic_flask_down_light
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.functionButton)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("√", "π", "xⁿ", "!").forEach { label ->
                    IconButton(
                        onClick = { onFunctionClick(label) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(
                            text = label,
                            color = colors.textPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Icon(
                painter = painterResource(id = toggleIconRes),
                contentDescription = if (isExpanded) "Hide functions" else "Show functions",
                tint = colors.textPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onToggle() }
            )
        }

        if (isExpanded) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(gridFunctions) { func ->
                    Button(
                        onClick = { onFunctionClick(func) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.functionButton,
                            contentColor = colors.textPrimary
                        ),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = func,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ========== STANDARD KEYPAD ==========
@Composable
fun StandardKeypad(onKeyPress: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalCalculatorColors.current

    val rows = listOf(
        listOf(
            KeyDef("AC", KeyStyle.FUNCTION),
            KeyDef("(", KeyStyle.FUNCTION),
            KeyDef(")", KeyStyle.FUNCTION),
            KeyDef("÷", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef("7", KeyStyle.NUMBER),
            KeyDef("8", KeyStyle.NUMBER),
            KeyDef("9", KeyStyle.NUMBER),
            KeyDef("×", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef("4", KeyStyle.NUMBER),
            KeyDef("5", KeyStyle.NUMBER),
            KeyDef("6", KeyStyle.NUMBER),
            KeyDef("−", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef("1", KeyStyle.NUMBER),
            KeyDef("2", KeyStyle.NUMBER),
            KeyDef("3", KeyStyle.NUMBER),
            KeyDef("+", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef("0", KeyStyle.NUMBER),
            KeyDef(".", KeyStyle.NUMBER),
            KeyDef("⌫", KeyStyle.ACCENT),
            KeyDef("=", KeyStyle.ACCENT)
        )
    )

    Column(modifier = modifier.fillMaxWidth().background(colors.background).padding(8.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    val bg = when (key.style) {
                        KeyStyle.NUMBER -> colors.numberButton
                        KeyStyle.FUNCTION -> colors.functionButton
                        KeyStyle.ACCENT -> colors.accentButton
                        else -> colors.numberButton
                    }
                    val fontSize = when (key.label) {
                        "AC" -> 18.sp
                        "=" -> 28.sp
                        else -> 24.sp
                    }
                    Button(
                        onClick = { onKeyPress(key.label) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = colors.textPrimary),
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp)
                    ) {
                        Text(key.label, fontSize = fontSize, fontWeight = if (key.style == KeyStyle.ACCENT) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

// ========== EDITABLE DISPLAY ==========
@Composable
fun EditableDisplay(
    textFieldValue: TextFieldValue,
    preview: String,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onPaste: (String) -> Unit,
    modifier: Modifier = Modifier,
    isScientific: Boolean = false,
    keyboardEnabled: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(textFieldValue.text.length) {
        coroutineScope.launch {
            delay(50)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(hasFocus, keyboardEnabled) {
        if (hasFocus && !keyboardEnabled) {
            while (isActive) {
                keyboardController?.hide()
                delay(50)
            }
        }
    }

    val charCount = textFieldValue.text.length
    val expressionFontSize: Float
    val previewFontSize: Float
    val maxHeight: Dp = 10000.dp

    if (isScientific) {
        val longestToken = textFieldValue.text
            .split(Regex("[^0-9A-Za-z.]+"))
            .maxOfOrNull { it.length } ?: 0
        expressionFontSize = when {
            longestToken <= 20 -> 26f
            longestToken <= 30 -> 23f
            longestToken <= 45 -> 20f
            else -> 18f
        }
        previewFontSize = 20f
    } else {
        expressionFontSize = when {
            charCount <= 6  -> 64f
            charCount <= 9  -> 56f
            charCount <= 12 -> 48f
            charCount <= 16 -> 42f
            charCount <= 20 -> 36f
            charCount <= 26 -> 32f
            else            -> 28f
        }
        previewFontSize = 26f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.displayBackground)
            .padding(
                horizontal = if (isScientific) 14.dp else 20.dp,
                vertical = 12.dp
            )
    ) {
        Column(
            horizontalAlignment = if (isScientific) Alignment.Start else Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            if (preview.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isScientific) 50.dp else 60.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = preview,
                        color = colors.textSecondary,
                        fontSize = previewFontSize.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clip = ClipData.newPlainText("result", preview)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                            },
                        textAlign = if (isScientific) TextAlign.Start else TextAlign.End,
                        overflow = TextOverflow.Visible
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    onTextFieldValueChange(newValue)
                },
                readOnly = false,
                textStyle = TextStyle(
                    fontSize = expressionFontSize.sp,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = if (isScientific) TextAlign.Start else TextAlign.End
                ),
                singleLine = false,
                cursorBrush = SolidColor(colors.accentButton),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp, max = maxHeight)
                    .verticalScroll(scrollState)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        hasFocus = focusState.isFocused
                        if (focusState.isFocused && !keyboardEnabled) {
                            keyboardController?.hide()
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                val items = mutableListOf<Pair<String, () -> Unit>>()
                                items.add("Copy Expression" to {
                                    clipboardManager.setPrimaryClip(
                                        ClipData.newPlainText(
                                            "expression",
                                            textFieldValue.text
                                        )
                                    )
                                    Toast
                                        .makeText(context, "Expression copied", Toast.LENGTH_SHORT)
                                        .show()
                                })
                                if (preview.isNotEmpty()) {
                                    items.add("Copy Result" to {
                                        clipboardManager.setPrimaryClip(
                                            ClipData.newPlainText("result", preview)
                                        )
                                        Toast
                                            .makeText(context, "Result copied", Toast.LENGTH_SHORT)
                                            .show()
                                    })
                                }
                                val clip = clipboardManager.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    clip.getItemAt(0).text?.toString()?.let { text ->
                                        if (text.isNotBlank()) {
                                            items.add("Paste" to {
                                                val formatted =
                                                    smartFormatPastedText(text)
                                                onPaste(formatted)
                                            })
                                        }
                                    }
                                }
                                if (items.isNotEmpty()) {
                                    AlertDialog.Builder(context)
                                        .setTitle("Options")
                                        .setItems(
                                            items.map { it.first }.toTypedArray()
                                        ) { _, which ->
                                            items[which].second.invoke()
                                        }
                                        .show()
                                }
                            }
                        )
                    }
            )
        }
    }
}

// ========== EQUATION SOLVER ==========
private fun solveEquation(expression: String): String {
    try {
        if (expression.contains('=')) {
            val parts = expression.split('=')
            if (parts.size == 2) {
                val left = parts[0].trim()
                val right = parts[1].trim()
                val variable = left.find { it.isLetter() }
                if (variable != null) {
                    val rightValue = StandardCalculatorEngine.evaluateExpression(right).toDoubleOrNull()
                    if (rightValue != null) {
                        return "$variable = $rightValue"
                    }
                }
            }
        }
        return StandardCalculatorEngine.evaluateExpression(expression)
    } catch (e: Exception) {
        return StandardCalculatorEngine.evaluateExpression(expression)
    }
}

// ========== SMART PASTE ==========
fun smartFormatPastedText(raw: String): String {
    var cleaned = raw
        .replace(Regex("[₹$€£¥]"), "")
        .replace(Regex(","), "")
        .replace(Regex("\\s+"), " ")
        .trim()
    cleaned = cleaned
        .replace("*", "×")
        .replace("/", "÷")
        .replace("x", "×")
        .replace("X", "×")
    cleaned = cleaned.replace(Regex("\\s+"), " ")
    return cleaned
}

// ========== HISTORY ==========
@Composable
fun HistoryContent(
    onDismiss: () -> Unit,
    currentMode: String,
    onExpressionSelected: (String, String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var selectedMode by remember { mutableStateOf(currentMode) }
    var historyItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }

    fun refreshHistory() {
        try {
            historyItems = HistoryManager.getHistory()
        } catch (e: Exception) {
            historyItems = emptyList()
        }
    }

    LaunchedEffect(Unit) { refreshHistory() }

    val filteredItems = historyItems.filter { it.mode == selectedMode }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(colors.displayBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Gray.copy(alpha = 0.5f))
                .padding(horizontal = 60.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("History", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("SCIENTIFIC", "STANDARD").forEach { mode ->
                val isSelected = selectedMode == mode
                val label = if (mode == "STANDARD") "Standard" else "Scientific"
                Button(
                    onClick = { selectedMode = mode },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) colors.accentButton else colors.functionButton,
                        contentColor = if (isSelected) colors.textPrimary else colors.textSecondary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No calculations yet", color = colors.textSecondary, fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems) { item ->
                    HistoryItemCard(
                        item = item,
                        onExpressionTap = { expr -> onExpressionSelected(expr, item.mode) },
                        onResultTap = { result -> onExpressionSelected(result, item.mode) },
                        onCopyFull = {
                            val text = "${item.expression} = ${item.result}"
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("history", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            val text = "${item.expression} = ${item.result}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        },
                        onDelete = {
                            HistoryManager.deleteItem(item.timestamp)
                            refreshHistory()
                        },
                        onLongPressExpression = { expr ->
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("expression", expr))
                            Toast.makeText(context, "Expression copied", Toast.LENGTH_SHORT).show()
                        },
                        onLongPressResult = { res ->
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("result", res))
                            Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = {
                HistoryManager.clearHistory(selectedMode)
                refreshHistory()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Clear ${if (selectedMode == "STANDARD") "Standard" else "Scientific"} History",
                color = colors.textSecondary,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentButton,
                contentColor = colors.textPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close", fontSize = 18.sp)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    onExpressionTap: (String) -> Unit,
    onResultTap: (String) -> Unit,
    onCopyFull: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onLongPressExpression: (String) -> Unit,
    onLongPressResult: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.functionButton)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.expression,
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpressionTap(item.expression) }
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.result,
                    color = colors.textSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResultTap(item.result) }
                )
            }
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = colors.textSecondary)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(colors.functionButton)
            ) {
                DropdownMenuItem(
                    text = { Text("Copy full", color = colors.textPrimary) },
                    onClick = { onCopyFull(); menuExpanded = false },
                    modifier = Modifier.background(colors.functionButton)
                )
                DropdownMenuItem(
                    text = { Text("Share", color = colors.textPrimary) },
                    onClick = { onShare(); menuExpanded = false },
                    modifier = Modifier.background(colors.functionButton)
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = colors.textPrimary) },
                    onClick = { onDelete(); menuExpanded = false },
                    modifier = Modifier.background(colors.functionButton)
                )
            }
        }
    }
}

// ========== BMI SCREEN ==========
@Composable
fun BMIScreen(onBack: () -> Unit) {
    val colors = LocalCalculatorColors.current
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<Double?>(null) }

    fun calculateBMI() {
        val h = height.toDoubleOrNull()?.div(100) // cm to meters
        val w = weight.toDoubleOrNull()
        if (h != null && w != null && h > 0) {
            bmiResult = w / (h * h)
        } else {
            bmiResult = null
        }
    }

    LaunchedEffect(height, weight) { calculateBMI() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }
            Text("BMI Calculator", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Height (cm)", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 20.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Weight (kg)", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 20.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(24.dp))

        bmiResult?.let { bmi ->
            val classification = when {
                bmi < 16.0 -> "Very severely underweight"
                bmi < 17.0 -> "Severely underweight"
                bmi < 18.5 -> "Underweight"
                bmi < 25.0 -> "Healthy"
                bmi < 30.0 -> "Overweight"
                bmi < 35.0 -> "Obese Class I"
                bmi < 40.0 -> "Obese Class II"
                else -> "Obese Class III"
            }

            val idealWeightLow = 18.5 * (height.toDoubleOrNull()?.div(100)?.let { it * it } ?: 1.0)
            val idealWeightHigh = 24.9 * (height.toDoubleOrNull()?.div(100)?.let { it * it } ?: 1.0)
            val weightToLose = if (bmi > 25) weight.toDoubleOrNull()?.minus(idealWeightHigh) ?: 0.0 else 0.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.functionButton)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = String.format("%.2f BMI", bmi),
                        color = colors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = classification,
                        color = colors.accentButton,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (weightToLose > 0) {
                        Text(
                            text = "For healthy life you have to decrease: ${String.format("%.1f", weightToLose)} kg",
                            color = colors.textSecondary,
                            fontSize = 16.sp
                        )
                    } else if (bmi < 18.5) {
                        val weightToGain = idealWeightLow - (weight.toDoubleOrNull() ?: 0.0)
                        Text(
                            text = "For healthy life you have to increase: ${String.format("%.1f", weightToGain)} kg",
                            color = colors.textSecondary,
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "You are in a healthy range. Keep it up!",
                            color = colors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.functionButton, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                listOf(
                    "< 16.0" to "Very severely underweight",
                    "16.0 ~ 16.9" to "Severely underweight",
                    "17.0 ~ 18.4" to "Underweight",
                    "18.5 ~ 24.9" to "Healthy",
                    "25.0 ~ 29.9" to "Overweight",
                    "30.0 ~ 34.9" to "Obese Class I",
                    "35.0 ~ 39.9" to "Obese Class II",
                    "≥ 40.0" to "Obese Class III"
                ).forEach { (range, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(range, color = colors.textSecondary, fontSize = 14.sp)
                        Text(label, color = colors.textPrimary, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        } ?: Text("Enter height and weight", color = colors.textSecondary, fontSize = 18.sp)

        Spacer(Modifier.weight(1f))
    }
}

// ========== SHOPPING SCREEN ==========
@Composable
fun ShoppingScreen(onBack: () -> Unit) {
    val colors = LocalCalculatorColors.current
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }
            Text("Shopping Calculator", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Discount", "Unit Price").forEachIndexed { index, label ->
                Button(
                    onClick = { selectedTab = index },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) colors.accentButton else colors.functionButton,
                        contentColor = if (selectedTab == index) colors.textPrimary else colors.textSecondary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, fontSize = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            0 -> DiscountTab()
            1 -> UnitPriceTab()
        }
    }
}

@Composable
fun DiscountTab() {
    val colors = LocalCalculatorColors.current
    var originalPrice by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var discountedPrice by remember { mutableStateOf("") }
    var savedAmount by remember { mutableStateOf("") }

    fun calculate() {
        val orig = originalPrice.toDoubleOrNull()
        val disc = discountPercent.toDoubleOrNull()
        val discPrice = discountedPrice.toDoubleOrNull()
        val saved = savedAmount.toDoubleOrNull()

        when {
            orig != null && disc != null -> {
                val newDiscPrice = orig * (1 - disc / 100)
                discountedPrice = String.format("%.2f", newDiscPrice)
                savedAmount = String.format("%.2f", orig - newDiscPrice)
            }
            orig != null && discPrice != null -> {
                val newDisc = (1 - discPrice / orig) * 100
                discountPercent = String.format("%.2f", newDisc)
                savedAmount = String.format("%.2f", orig - discPrice)
            }
            orig != null && saved != null -> {
                val newDiscPrice = orig - saved
                discountedPrice = String.format("%.2f", newDiscPrice)
                discountPercent = String.format("%.2f", (saved / orig) * 100)
            }
            disc != null && discPrice != null -> {
                val newOrig = discPrice / (1 - disc / 100)
                originalPrice = String.format("%.2f", newOrig)
                savedAmount = String.format("%.2f", newOrig - discPrice)
            }
            disc != null && saved != null -> {
                val newOrig = saved / (disc / 100)
                originalPrice = String.format("%.2f", newOrig)
                discountedPrice = String.format("%.2f", newOrig - saved)
            }
            discPrice != null && saved != null -> {
                val newOrig = discPrice + saved
                originalPrice = String.format("%.2f", newOrig)
                discountPercent = String.format("%.2f", (saved / newOrig) * 100)
            }
        }
    }

    LaunchedEffect(originalPrice, discountPercent, discountedPrice, savedAmount) { calculate() }

    Column {
        OutlinedTextField(
            value = originalPrice,
            onValueChange = { originalPrice = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Original Price", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = discountPercent,
            onValueChange = { discountPercent = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Discount (%)", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = discountedPrice,
            onValueChange = { discountedPrice = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Discounted Price", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = savedAmount,
            onValueChange = { savedAmount = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Saved Amount", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UnitPriceTab() {
    val colors = LocalCalculatorColors.current
    var totalAmount by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    fun calculate() {
        val total = totalAmount.toDoubleOrNull()
        val qty = quantity.toDoubleOrNull()
        val price = unitPrice.toDoubleOrNull()

        when {
            total != null && qty != null && qty != 0.0 -> {
                unitPrice = String.format("%.2f", total / qty)
            }
            total != null && price != null -> {
                if (price != 0.0) {
                    quantity = String.format("%.2f", total / price)
                }
            }
            qty != null && price != null && qty != 0.0 && price != 0.0 -> {
                totalAmount = String.format("%.2f", qty * price)
            }
        }
    }

    LaunchedEffect(totalAmount, quantity, unitPrice) { calculate() }

    Column {
        OutlinedTextField(
            value = totalAmount,
            onValueChange = { totalAmount = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Total Amount", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Quantity", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = unitPrice,
            onValueChange = { unitPrice = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Unit Price", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton,
                unfocusedBorderColor = colors.textSecondary,
                focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ========== CONVERTER SCREEN ==========
@Composable
fun ConverterScreen(
    onModeChange: (CalculatorMode) -> Unit,
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onMenuItemClick: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        CalculatorTopBar(
            mode = CalculatorMode.CONVERTER,
            onModeChange = onModeChange,
            onHistoryClick = null,
            onMenuItemClick = onMenuItemClick
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(converterCategories) { category ->
                ConverterCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
fun ConverterCard(category: ConverterCategory, onClick: () -> Unit) {
    val colors = LocalCalculatorColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.functionButton)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = category.icon, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.name,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${category.units.size} units",
                color = colors.textSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ========== SCIENTIFIC TOP BAR ==========
@Composable
fun ScientificTopBar(
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onMenuItemClick: (String) -> Unit,
    angleMode: String,
    onAngleModeChange: (String) -> Unit,
    displayMode: String,
    onDisplayModeChange: (String) -> Unit,
    fractionMode: String,
    onFractionModeChange: (String) -> Unit,
    approxMode: String,
    onApproxModeChange: (String) -> Unit,
    onModeChange: (CalculatorMode) -> Unit
) {
    val colors = LocalCalculatorColors.current

    Column {
        CalculatorTopBar(
            mode = CalculatorMode.SCIENTIFIC,
            onModeChange = onModeChange,
            onHistoryClick = onHistoryClick,
            onMenuItemClick = onMenuItemClick
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary, modifier = Modifier.size(20.dp))
            }
            SettingsButton(
                label = angleMode,
                options = listOf("RAD" to "Radians", "DEG" to "Degrees", "GRA" to "Gradians"),
                onOptionSelected = onAngleModeChange
            )
            SettingsButton(
                label = displayMode,
                options = listOf("NORM" to "Normal", "SCI" to "Scientific", "ENG" to "Engineering"),
                onOptionSelected = onDisplayModeChange
            )
            SettingsButton(
                label = fractionMode,
                options = listOf(
                    "DEC" to "Decimal",
                    "DEC EXACT" to "Decimal Exact",
                    "FRACTIONAL" to "Fractional",
                    "COMBINED" to "Combined",
                    "PERCENT" to "Percent",
                    "PERMILLE" to "Permille",
                    "PERMYRIAD" to "Permyriad"
                ),
                onOptionSelected = onFractionModeChange
            )
            SettingsButton(
                label = approxMode,
                options = listOf("EXACT" to "Always Exact", "TRY EXACT" to "Try Exact", "APPROX" to "Approximate"),
                onOptionSelected = onApproxModeChange
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SettingsButton(
    label: String,
    options: List<Pair<String, String>>,
    onOptionSelected: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    var expanded by remember { mutableStateOf(false) }
    Text(
        text = label,
        color = colors.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.functionButton)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clickable { expanded = true }
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(colors.functionButton)
    ) {
        options.forEach { (short, long) ->
            DropdownMenuItem(
                text = { Text(long, color = colors.textPrimary) },
                onClick = {
                    onOptionSelected(short)
                    expanded = false
                },
                modifier = Modifier.background(colors.functionButton)
            )
        }
    }
}

// ========== EXTERNAL KEYBOARD ROW ==========
@Composable
fun ExternalKeyboardRow(
    keyboardEnabled: Boolean,
    onKeyboardEnabledChange: (Boolean) -> Unit,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    canUndo: Boolean = false,
    canRedo: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (keyboardEnabled) colors.accentButton else Color.Transparent
                )
                .clickable {
                    onKeyboardEnabledChange(!keyboardEnabled)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Keyboard,
                contentDescription = if (keyboardEnabled) "Hide Keyboard" else "Show Keyboard",
                tint = if (keyboardEnabled) colors.textPrimary else colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) colors.textSecondary else colors.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = onRedo,
            enabled = canRedo,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) colors.textSecondary else colors.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ========== SCIENTIFIC CALCULATOR SCREEN ==========
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScientificCalculatorScreen(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onModeChange: (CalculatorMode) -> Unit,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onMenuItemClick: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val keyboardController = LocalSoftwareKeyboardController.current

    var angleMode by remember { mutableStateOf("DEG") }
    var displayMode by remember { mutableStateOf("ENG") }
    var fractionMode by remember { mutableStateOf("DEC") }
    var approxMode by remember { mutableStateOf("TRY EXACT") }

    var selectedPrefix by remember { mutableStateOf("kilo") }
    var selectedUnit by remember { mutableStateOf("meter") }
    var selectedComplex by remember { mutableStateOf("Real") }

    var justEvaluated by remember { mutableStateOf(false) }
    var keyboardEnabled by remember { mutableStateOf(false) }

    data class UndoRedoState(
        val undoStack: List<TextFieldValue> = emptyList(),
        val redoStack: List<TextFieldValue> = emptyList(),
        val current: TextFieldValue
    ) {
        val canUndo: Boolean get() = undoStack.isNotEmpty()
        val canRedo: Boolean get() = redoStack.isNotEmpty()
    }

    var undoRedoState by remember {
        mutableStateOf(
            UndoRedoState(
                undoStack = emptyList(),
                redoStack = emptyList(),
                current = textFieldValue
            )
        )
    }

    LaunchedEffect(textFieldValue) {
        if (undoRedoState.current != textFieldValue) {
            undoRedoState = UndoRedoState(
                undoStack = undoRedoState.undoStack + undoRedoState.current,
                redoStack = emptyList(),
                current = textFieldValue
            )
        }
    }

    fun undo() {
        if (undoRedoState.canUndo) {
            val newUndoStack = undoRedoState.undoStack.dropLast(1)
            val previousState = undoRedoState.undoStack.last()
            val newRedoStack = undoRedoState.redoStack + undoRedoState.current
            undoRedoState = UndoRedoState(
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                current = previousState
            )
            onTextFieldValueChange(previousState)
        }
    }

    fun redo() {
        if (undoRedoState.canRedo) {
            val newRedoStack = undoRedoState.redoStack.dropLast(1)
            val nextState = undoRedoState.redoStack.last()
            val newUndoStack = undoRedoState.undoStack + undoRedoState.current
            undoRedoState = UndoRedoState(
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                current = nextState
            )
            onTextFieldValueChange(nextState)
        }
    }

    val expression = textFieldValue.text

    val answer = remember(expression, angleMode, displayMode, fractionMode, approxMode) {
        QalculateEngine.angleUnit = when (angleMode) {
            "DEG" -> AngleUnit.ANGLE_UNIT_DEGREES
            "RAD" -> AngleUnit.ANGLE_UNIT_RADIANS
            "GRA" -> AngleUnit.ANGLE_UNIT_GRADIANS
            else -> AngleUnit.ANGLE_UNIT_DEGREES
        }
        QalculateEngine.displayMode = displayMode
        QalculateEngine.fractionMode = fractionMode
        QalculateEngine.approxMode = approxMode
        QalculateEngine.evaluateExpressionOrBlank(expression)
    }

    val parsedExpr = remember(expression, angleMode) {
        QalculateEngine.parseExpression(expression)
    }

    val answerScrollState = rememberScrollState()
    val inputScrollState = rememberScrollState()
    val parsedScrollState = rememberScrollState()

    LaunchedEffect(answer) {
        if (answer.isNotEmpty()) {
            answerScrollState.animateScrollTo(answerScrollState.maxValue)
        }
    }

    LaunchedEffect(expression) {
        if (expression.isNotEmpty()) {
            inputScrollState.animateScrollTo(inputScrollState.maxValue)
        }
    }

    LaunchedEffect(parsedExpr) {
        if (parsedExpr.isNotEmpty()) {
            parsedScrollState.animateScrollTo(parsedScrollState.maxValue)
        }
    }

    val displayBg = colors.displayBackground
    val answerColor = colors.accentText
    val inputColor = colors.textPrimary
    val dividerColor = colors.displayDivider

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(keyboardEnabled) {
        if (keyboardEnabled) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    fun handleKeyPress(label: String) {
        if (label == "Enter") {
            val result = QalculateEngine.evaluateExpression(expression)
            if (result != "Error") {
                HistoryManager.addEntry(expression, result, "SCIENTIFIC")
            }
            justEvaluated = true
        } else {
            val newValue = scientificHandleKeyPress(textFieldValue, label, justEvaluated)
            onTextFieldValueChange(newValue)
            justEvaluated = false
        }
        keyboardEnabled = false
        keyboardController?.hide()
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        ScientificTopBar(
            onBack = onBack,
            onHistoryClick = onHistoryClick,
            onMenuItemClick = onMenuItemClick,
            angleMode = angleMode,
            onAngleModeChange = { angleMode = it },
            displayMode = displayMode,
            onDisplayModeChange = { displayMode = it },
            fractionMode = fractionMode,
            onFractionModeChange = { fractionMode = it },
            approxMode = approxMode,
            onApproxModeChange = { approxMode = it },
            onModeChange = onModeChange
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(displayBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
                    .horizontalScroll(answerScrollState)
            ) {
                if (answer.isNotEmpty()) {
                    Text(
                        text = answer,
                        color = answerColor,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clip = ClipData.newPlainText("answer", answer)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Answer copied", Toast.LENGTH_SHORT).show()
                            },
                        textAlign = TextAlign.End,
                        overflow = TextOverflow.Visible,
                        maxLines = 1,
                        softWrap = false
                    )
                } else {
                    Text(
                        text = "Waiting for input...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Divider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .verticalScroll(inputScrollState),
                contentAlignment = Alignment.BottomStart
            ) {
                key(keyboardEnabled) {
                    InterceptPlatformTextInput(
                        interceptor = { request, nextHandler ->
                            if (keyboardEnabled) {
                                nextHandler.startInputMethod(request)
                            } else {
                                awaitCancellation()
                            }
                        }
                    ) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChange,
                            textStyle = TextStyle(
                                fontSize = when {
                                    expression.length <= 10 -> 26.sp
                                    expression.length <= 20 -> 22.sp
                                    else -> 18.sp
                                },
                                color = inputColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(answerColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }
                }
            }

            Divider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .verticalScroll(parsedScrollState)
            ) {
                if (parsedExpr.isNotEmpty()) {
                    Text(
                        text = colorizeParsedExpression(parsedExpr),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clip = ClipData.newPlainText("parsed", parsedExpr)
                                clipboardManager.setPrimaryClip(clip)
                                Toast.makeText(context, "Expression copied", Toast.LENGTH_SHORT).show()
                            },
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Visible,
                        softWrap = true,
                        maxLines = Int.MAX_VALUE
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(colors.displayDivider)
        )

        ExternalKeyboardRow(
            keyboardEnabled = keyboardEnabled,
            onKeyboardEnabledChange = { enabled ->
                keyboardEnabled = enabled
            },
            onUndo = { undo() },
            onRedo = { redo() },
            canUndo = undoRedoState.canUndo,
            canRedo = undoRedoState.canRedo
        )

        ScientificKeypadFull(
            onKeyPress = ::handleKeyPress,
            selectedPrefix = selectedPrefix,
            onPrefixSelected = { selectedPrefix = it },
            selectedUnit = selectedUnit,
            onUnitSelected = { selectedUnit = it },
            selectedComplex = selectedComplex,
            onComplexSelected = { selectedComplex = it }
        )
    }
}

// ========== SCIENTIFIC KEYPAD FULL ==========
@Composable
fun ScientificKeypadFull(
    onKeyPress: (String) -> Unit,
    selectedPrefix: String,
    onPrefixSelected: (String) -> Unit,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    selectedComplex: String,
    onComplexSelected: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val buttonHeight = 54.dp

    val rows = listOf(
        listOf(
            KeyDef("X", KeyStyle.SCIENTIFIC, secondaryLabel = "→x"),
            KeyDef("Y", KeyStyle.SCIENTIFIC, secondaryLabel = "→y"),
            KeyDef("Z", KeyStyle.SCIENTIFIC, secondaryLabel = "→z"),
            KeyDef(selectedPrefix, KeyStyle.SCIENTIFIC, isDropdown = true, dropdownItems = listOf("giga", "mega", "kilo", "milli", "micro", "nano", "pico")),
            KeyDef(selectedUnit, KeyStyle.SCIENTIFIC, isDropdown = true, dropdownItems = listOf("ampere", "gram", "joule", "kelvin", "liter", "meter", "newton", "ohm", "pascal", "second", "volt", "watt"))
        ),
        listOf(
            KeyDef("∫", KeyStyle.SCIENTIFIC),
            KeyDef("dx", KeyStyle.SCIENTIFIC),
            KeyDef("Σ", KeyStyle.SCIENTIFIC, secondaryLabel = "Π"),
            KeyDef("i", KeyStyle.SCIENTIFIC, secondaryLabel = "∠"),
            KeyDef(selectedComplex, KeyStyle.SCIENTIFIC, isDropdown = true, dropdownItems = listOf("Abs.", "Arg.", "Real", "Imag.", "Conj."))
        ),
        listOf(
            KeyDef("sin", KeyStyle.SCIENTIFIC, secondaryLabel = "asin"),
            KeyDef("cos", KeyStyle.SCIENTIFIC, secondaryLabel = "acos"),
            KeyDef("tan", KeyStyle.SCIENTIFIC, secondaryLabel = "atan"),
            KeyDef("ln", KeyStyle.SCIENTIFIC, secondaryLabel = "log"),
            KeyDef("∞", KeyStyle.SCIENTIFIC, secondaryLabel = "!")
        ),
        listOf(
            KeyDef("%", KeyStyle.FUNCTION, secondaryLabel = "±"),
            KeyDef("π", KeyStyle.SCIENTIFIC, secondaryLabel = "e"),
            KeyDef("7", KeyStyle.NUMBER),
            KeyDef("8", KeyStyle.NUMBER),
            KeyDef("9", KeyStyle.NUMBER),
            KeyDef("⌫", KeyStyle.ACCENT),
            KeyDef("AC", KeyStyle.ACCENT)
        ),
        listOf(
            KeyDef("√", KeyStyle.SCIENTIFIC),
            KeyDef("xʸ", KeyStyle.SCIENTIFIC),
            KeyDef("4", KeyStyle.NUMBER),
            KeyDef("5", KeyStyle.NUMBER),
            KeyDef("6", KeyStyle.NUMBER),
            KeyDef("·", KeyStyle.FUNCTION),
            KeyDef("/", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef("(", KeyStyle.FUNCTION, secondaryLabel = "["),
            KeyDef(")", KeyStyle.FUNCTION, secondaryLabel = "]"),
            KeyDef("1", KeyStyle.NUMBER),
            KeyDef("2", KeyStyle.NUMBER),
            KeyDef("3", KeyStyle.NUMBER),
            KeyDef("+", KeyStyle.FUNCTION),
            KeyDef("-", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef("_", KeyStyle.FUNCTION, secondaryLabel = ";"),
            KeyDef("=", KeyStyle.FUNCTION, secondaryLabel = ","),
            KeyDef("0", KeyStyle.NUMBER),
            KeyDef(".", KeyStyle.NUMBER),
            KeyDef("E", KeyStyle.SCIENTIFIC),
            KeyDef("Enter", KeyStyle.ACCENT, span = 2)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { keyDef ->
                    val weight = if (keyDef.span > 1) 2.1f else 1f
                    if (keyDef.isDropdown) {
                        DropdownKeyButton(
                            currentLabel = keyDef.label,
                            items = keyDef.dropdownItems ?: emptyList(),
                            onItemSelected = { newLabel ->
                                when {
                                    keyDef.label == selectedPrefix -> onPrefixSelected(newLabel)
                                    keyDef.label == selectedUnit -> onUnitSelected(newLabel)
                                    keyDef.label == selectedComplex -> onComplexSelected(newLabel)
                                }
                            },
                            onTap = { onKeyPress(keyDef.label) },
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(weight)
                                .padding(2.dp)
                        )
                    } else {
                        DualFunctionButton(
                            keyDef = keyDef,
                            onTap = { onKeyPress(keyDef.label) },
                            onLongPress = { keyDef.secondaryLabel?.let { onKeyPress(it) } ?: onKeyPress(keyDef.label) },
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(weight)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ========== DUAL FUNCTION BUTTON ==========
@Composable
fun DualFunctionButton(
    keyDef: KeyDef,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val bg = when (keyDef.style) {
        KeyStyle.NUMBER -> colors.numberButton
        KeyStyle.FUNCTION -> colors.functionButton
        KeyStyle.ACCENT -> colors.accentButton
        KeyStyle.SCIENTIFIC -> colors.functionButton
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = keyDef.label,
            color = colors.textPrimary,
            fontSize = if (keyDef.label.length > 3) 16.sp else 20.sp,
            fontWeight = if (keyDef.style == KeyStyle.ACCENT) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
        )
        keyDef.secondaryLabel?.let {
            Text(
                text = it,
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = (-6).dp)
                    .padding(end = 4.dp, top = 2.dp)
            )
        }
    }
}

// ========== DROPDOWN UNIT BUTTON ==========
@Composable
fun DropdownKeyButton(
    currentLabel: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.functionButton)
            .combinedClickable(
                onClick = { onTap(currentLabel) },
                onLongClick = { expanded = true }
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentLabel,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(max = 200.dp)
                .heightIn(max = 300.dp)
                .background(
                    colors.background,
                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
            offset = DpOffset(0.dp, 4.dp),          // small gap
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            containerColor = colors.background,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = item == currentLabel
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
                        text = item,
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ========== PREVIEW ==========
@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    var selectedTheme by remember { mutableStateOf("System") }
    var isPureBlackEnabled by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()
    UniCalculatorTheme(
        darkTheme = darkTheme,
        pureBlack = isPureBlackEnabled
    ) {
        UniCalculatorApp(
            modifier = Modifier,
            selectedTheme = selectedTheme,
            onThemeChanged = {},
            isPureBlackEnabled = isPureBlackEnabled,
            onPureBlackChanged = {}
        )
    }
}