package com.unicalculator.app

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
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.jherkenhoff.libqalculate.AngleUnit
import com.unicalculator.app.calculator.HistoryItem
import com.unicalculator.app.calculator.HistoryManager
import com.unicalculator.app.calculator.QalculateEngine
import com.unicalculator.app.calculator.StandardCalculatorEngine
import com.unicalculator.app.ui.screens.LicensesScreen
import com.unicalculator.app.ui.theme.AboutScreen
import com.unicalculator.app.ui.theme.CalculatorColors
import com.unicalculator.app.ui.theme.ConverterDetailScreen
import com.unicalculator.app.ui.theme.LocalCalculatorColors
import com.unicalculator.app.ui.theme.SettingsScreen
import com.unicalculator.app.ui.theme.UniCalculatorTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun getModeIcon(mode: CalculatorMode, isDarkTheme: Boolean): Int = when (mode) {
    CalculatorMode.SCIENTIFIC -> if (isDarkTheme) R.drawable.ic_scientific_dark else R.drawable.ic_scientific_light
    CalculatorMode.STANDARD -> if (isDarkTheme) R.drawable.ic_standard_dark else R.drawable.ic_standard_light
    CalculatorMode.CONVERTER -> if (isDarkTheme) R.drawable.ic_converter_dark else R.drawable.ic_converter_light
}

data class ConverterCategory(val id: String, val name: String, val iconRes: Int, val units: List<String>)

val converterCategories = listOf(
    ConverterCategory("Length", "Length", R.drawable.ic_length, listOf(
        "Nanometer", "Micrometer", "Millimeter", "Centimeter", "Decimeter",
        "Meter", "Foot", "Yard", "Fathom", "Furlong",
        "Kilometer", "Mile", "Nautical Mile", "Astronomical Unit", "Light-year", "Parsec", "Inch"
    )),
    ConverterCategory("Area", "Area", R.drawable.ic_area, listOf(
        "Square Millimeter", "Square Centimeter", "Square Decimeter", "Are",
        "Square Meter", "Square Foot", "Square Yard", "Hectare", "Acre",
        "Square Kilometer", "Square Mile", "Square Inch"
    )),
    ConverterCategory("Volume", "Volume", R.drawable.ic_volume, listOf(
        "Cubic Millimeter", "Cubic Centimeter", "Milliliter", "Cubic Inch",
        "Cubic Decimeter", "Liter", "Teaspoon (US)", "Tablespoon (US)",
        "Fluid Ounce (US)", "Fluid Ounce (UK)", "Cup (US)", "Pint (US)", "Pint (UK)",
        "Quart (US)", "Quart (UK)", "Cubic Foot", "Gallon (US)", "Gallon (UK)",
        "Cubic Yard", "Cubic Meter", "Kiloliter"
    )),
    ConverterCategory("Fuel", "Fuel", R.drawable.ic_fuel, listOf(
        "Liter", "Gallon (US)", "Gallon (UK)", "Barrel (oil, US)", "Cubic meter"
    )),
    ConverterCategory("Mass", "Mass", R.drawable.ic_mass, listOf(
        "Milligram", "Gram", "Carat", "Grain", "Kilogram", "Slug",
        "Pound", "Ounce", "Stone", "Tonne (metric)", "Short Ton (US)", "Long Ton (UK)"
    )),
    ConverterCategory("Temperature", "Temperature", R.drawable.ic_temperature, listOf(
        "Celsius", "Fahrenheit", "Kelvin", "Rankine"
    )),
    ConverterCategory("Digital Storage", "Digital Storage", R.drawable.ic_storage, listOf(
        "Bit", "Byte", "Kilobit", "Kibibit", "Kilobyte", "Kibibyte",
        "Megabit", "Mebibit", "Megabyte", "Mebibyte", "Gigabit", "Gibibit",
        "Gigabyte", "Gibibyte", "Terabit", "Tebibit", "Terabyte", "Tebibyte",
        "Petabit", "Pebibit", "Petabyte", "Pebibyte", "Exabit", "Exbibit",
        "Exabyte", "Exbibyte"
    )),
    ConverterCategory("Data Transfer", "Data Rate", R.drawable.ic_data_transfer, listOf(
        "bps", "Kbps", "Byte per second", "KB/s (decimal)", "Kibit/s", "KiB/s",
        "Mbps", "MB/s (decimal)", "Mibit/s", "MiB/s", "Gbps", "GB/s (decimal)",
        "Gibit/s", "GiB/s", "Tbps", "TB/s (decimal)"
    )),
    ConverterCategory("Pressure", "Pressure", R.drawable.ic_pressure, listOf(
        "Pascal", "Millibar", "Kilopascal", "Kilogram-force per cm²",
        "mmHg", "Torr", "inHg", "Bar", "Atmosphere", "PSI", "Megapascal"
    )),
    ConverterCategory("Energy", "Energy", R.drawable.ic_energy, listOf(
        "Electronvolt", "Erg", "Joule", "Foot-pound (energy)",
        "Calorie (thermochemical)", "Kilojoule", "Kilocalorie",
        "Watt-hour", "BTU", "Kilowatt-hour", "Megajoule"
    )),
    ConverterCategory("Speed", "Speed", R.drawable.ic_speed, listOf(
        "Meter per second", "Foot per second", "Kilometer per hour",
        "Knot", "Mile per hour", "Kilometer per second", "Speed of light"
    )),
    ConverterCategory("Time", "Time", R.drawable.ic_time, listOf(
        "Millisecond", "Second", "Minute", "Hour", "Day"
    )),
    ConverterCategory("Duration", "Duration", R.drawable.ic_date, listOf(
        "Day", "Week", "Fortnight", "Month (average)", "Quarter (average)",
        "Year (Julian)", "Decade", "Century"
    )),
    ConverterCategory("Angle", "Angle", R.drawable.ic_angle, listOf(
        "Arcsecond", "Arcminute", "Mil (NATO)", "Degree", "Gradian", "Radian", "Turn"
    )),
    ConverterCategory("Power", "Power", R.drawable.ic_power, listOf(
        "Milliwatt", "Watt", "Kilowatt", "Megawatt", "Gigawatt",
        "Horsepower (mechanical)", "Horsepower (metric)",
        "Foot-pound per second", "Kilogram-force meter per second",
        "BTU per second", "Kilocalorie per second"
    )),
    ConverterCategory("Force", "Force", R.drawable.ic_force, listOf(
        "Dyne", "Poundal", "Ounce-force", "Newton", "Pound-force",
        "Kilogram-force", "Kilonewton"
    )),
    ConverterCategory("Density", "Density", R.drawable.ic_density, listOf(
        "Gram per liter", "Kilogram per cubic meter", "Gram per cubic centimeter",
        "Pound per cubic foot", "Pound per gallon (US)", "Pound per cubic inch"
    )),
    ConverterCategory("Frequency", "Frequency", R.drawable.ic_frequency, listOf(
        "RPM (revolutions/min)", "Hertz", "Kilohertz", "Megahertz", "Gigahertz", "Terahertz"
    )),
    ConverterCategory("Torque", "Torque", R.drawable.ic_torque, listOf(
        "Ounce-inch", "Pound-inch", "Newton meter", "Pound-foot",
        "Kilogram-force meter", "Kilonewton meter"
    )),
    ConverterCategory("Viscosity", "Viscosity", R.drawable.ic_viscosity, listOf(
        "Centipoise", "Poise", "Pascal-second", "Poiseuille", "Reyn"
    )),
    ConverterCategory("Fuel Economy", "Fuel Economy", R.drawable.ic_fuel_pump, listOf(
        "L/100km", "km/L", "MPG (US)", "MPG (UK)"
    )),
    ConverterCategory("Absorbed Dose", "Absorbed Dose", R.drawable.ic_radiation, listOf(
        "Milligray", "Centigray", "Rad", "Gray"
    )),
    ConverterCategory("Equivalent Dose", "Equivalent Dose", R.drawable.ic_radiation, listOf(
        "Millisievert", "Rem", "Sievert"
    )),
    ConverterCategory("Illuminance", "Illuminance", R.drawable.ic_illuminance, listOf(
        "Lux", "Foot-candle"
    )),
    ConverterCategory("Sound Level", "Sound Level", R.drawable.ic_sound, listOf(
        "dB SPL", "Pascal"
    )),
    ConverterCategory("Typography", "Typography", R.drawable.ic_typography, listOf(
        "Twip", "Point", "Pica", "Pixel (@96 DPI)", "Inch"
    )),
    ConverterCategory("Blood Glucose", "Blood Glucose", R.drawable.ic_blood_glucose, listOf(
        "mg/dL", "mmol/L"
    )),
    ConverterCategory("bmi", "BMI", R.drawable.ic_bmi, listOf("kg/m²")),
    ConverterCategory("shopping", "Shopping", R.drawable.ic_shopping, listOf("Percent", "Currency"))
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

private fun colorizeParsedExpression(text: String): AnnotatedString {
    val cyan = Color(0xFF84D2E6)
    val functionGray = Color(0xFFB6BDBF)
    val unitGray = Color(0xFFA7AEC7)
    val pattern = Regex("""\d+(?:\.\d+)?|[a-zA-Z]+\(|[()]|·|\S+""")
    val tokens = pattern.findAll(text).toList()
    return buildAnnotatedString {
        var lastIndex = 0
        for (token in tokens) {
            if (token.range.first > lastIndex) append(text.substring(lastIndex, token.range.first))
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
        if (lastIndex < text.length) append(text.substring(lastIndex))
    }
}

private val scientificInsertMap = mapOf(
    "sin" to "sin(", "cos" to "cos(", "tan" to "tan(",
    "ln" to "ln(", "log" to "log(",
    "asin" to "asin(", "acos" to "acos(", "atan" to "atan(",
    "√" to "sqrt(", "π" to "π", "e" to "e", "∞" to "∞",
    "x²" to "^2", "xʸ" to "^", "!" to "!", "%" to "%",
    "∫" to "integrate(", "dx" to "diff(",
    "Σ" to "sum(", "Π" to "product(",
    "i" to "i", "∠" to "∠",
    "(" to "(", ")" to ")", "[" to "[", "]" to "]",
    "_" to "_", ";" to ";", "=" to "=", "," to ",",
    "E" to "E",
    "0" to "0", "1" to "1", "2" to "2", "3" to "3", "4" to "4",
    "5" to "5", "6" to "6", "7" to "7", "8" to "8", "9" to "9",
    "." to ".", "+" to "+", "-" to "-", "·" to "·", "/" to "/",
    "x" to "x", "y" to "y", "z" to "z",
    "giga" to "G", "mega" to "M", "kilo" to "k",
    "milli" to "m", "micro" to "µ", "nano" to "n", "pico" to "p",
    "ampere" to "A", "gram" to "g", "joule" to "J", "kelvin" to "K",
    "liter" to "L", "meter" to "m", "newton" to "N", "ohm" to "Ω",
    "pascal" to "Pa", "second" to "s", "volt" to "V", "watt" to "W",
    "Abs." to "abs(", "Arg." to "arg(", "Real" to "re(",
    "Imag." to "im(", "Conj." to "conj("
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
        label == "AC" -> TextFieldValue("", TextRange(0))
        label == "⌫" -> {
            if (start == 0 && end == 0) return current
            val newText = if (start != end) text.substring(0, start) + text.substring(end)
            else text.substring(0, start - 1) + text.substring(start)
            val newCursor = if (start != end) start else (start - 1).coerceAtLeast(0)
            TextFieldValue(newText, TextRange(newCursor))
        }
        justEvaluated && label.length == 1 && label[0].isDigit() ->
            TextFieldValue(label, TextRange(label.length))
        text.isEmpty() -> {
            val insert = scientificInsertMap[label] ?: label
            TextFieldValue(insert, TextRange(insert.length))
        }
        else -> {
            val insert = scientificInsertMap[label] ?: label
            val newText = text.substring(0, start) + insert + text.substring(end)
            TextFieldValue(newText, TextRange(start + insert.length))
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        HistoryManager.init(this)
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val savedTheme = prefs.getString("selected_theme", "System") ?: "System"
        val savedPureBlack = prefs.getBoolean("pure_black_enabled", false)

        setContent {
            var selectedTheme by remember { mutableStateOf(savedTheme) }
            var isPureBlackEnabled by remember { mutableStateOf(savedPureBlack) }
            val darkTheme = when (selectedTheme) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }
            UniCalculatorTheme(darkTheme = darkTheme, pureBlack = isPureBlackEnabled) {
                // Scaffold provides NO insets — outer Box in UniCalculatorApp handles them once.
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    UniCalculatorApp(
                        modifier = Modifier.padding(innerPadding),
                        onThemeChanged = { theme ->
                            selectedTheme = theme
                            prefs.edit { putString("selected_theme", theme) }
                        },
                        onPureBlackChanged = { pureBlack ->
                            isPureBlackEnabled = pureBlack
                            prefs.edit { putBoolean("pure_black_enabled", pureBlack) }
                        }
                    )
                }
            }
        }
    }
}

enum class CalculatorMode { SCIENTIFIC, STANDARD, CONVERTER }

@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniCalculatorApp(
    modifier: Modifier = Modifier,
    onThemeChanged: (String) -> Unit,
    onPureBlackChanged: (Boolean) -> Unit
) {
    val colors = LocalCalculatorColors.current
    var currentIndex by rememberSaveable { mutableIntStateOf(1) }
    val modes = listOf(CalculatorMode.SCIENTIFIC, CalculatorMode.STANDARD, CalculatorMode.CONVERTER)

    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showLicenses by rememberSaveable { mutableStateOf(false) }
    var isFunctionExpanded by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showConverterDetail by rememberSaveable { mutableStateOf(false) }
    var selectedConverterCategory by rememberSaveable { mutableStateOf("") }

    var standardTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var scientificTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val threshold = 150f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (!showConverterDetail && !showHistory) {
                            when {
                                dragOffset < -threshold && currentIndex < 2 -> currentIndex++
                                dragOffset > threshold && currentIndex > 0 -> currentIndex--
                            }
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (!showConverterDetail && !showHistory) dragOffset += dragAmount
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
        ) {
            val currentMode = modes[currentIndex]
            when {
                showLicenses -> LicensesScreen(onBack = { showLicenses = false })
                showAbout -> AboutScreen(
                    onBack = { showAbout = false },
                    onLicensesClick = { showLicenses = true }
                )
                showSettings -> SettingsScreen(
                    onBack = { showSettings = false },
                    onThemeChanged = onThemeChanged,
                    onPureBlackChanged = onPureBlackChanged
                )
                showConverterDetail -> when (selectedConverterCategory) {
                    "bmi" -> BMIScreen(onBack = { showConverterDetail = false })
                    "shopping" -> ShoppingScreen(onBack = { showConverterDetail = false })
                    else -> ConverterDetailScreen(
                        category = selectedConverterCategory,
                        onBack = { showConverterDetail = false }
                    )
                }
                currentMode == CalculatorMode.STANDARD -> StandardCalculatorScreen(
                    textFieldValue = standardTextFieldValue,
                    onTextFieldValueChange = { standardTextFieldValue = it },
                    isFunctionExpanded = isFunctionExpanded,
                    onToggleFunction = { isFunctionExpanded = !isFunctionExpanded },
                    onModeChange = { currentIndex = modes.indexOf(it) },
                    onHistoryClick = { showHistory = true },
                    onMenuItemClick = { item ->
                        when (item) {
                            "About" -> showAbout = true
                            "Settings" -> showSettings = true
                        }
                    }
                )
                currentMode == CalculatorMode.CONVERTER -> ConverterScreen(
                    onModeChange = { currentIndex = modes.indexOf(it) },
                    onCategoryClick = { selectedConverterCategory = it; showConverterDetail = true },
                    onMenuItemClick = { item ->
                        when (item) {
                            "About" -> showAbout = true
                            "Settings" -> showSettings = true
                        }
                    }
                )
                currentMode == CalculatorMode.SCIENTIFIC -> ScientificCalculatorScreen(
                    textFieldValue = scientificTextFieldValue,
                    onTextFieldValueChange = { scientificTextFieldValue = it },
                    onModeChange = { currentIndex = modes.indexOf(it) },
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
                containerColor = colors.displayBackground,
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                dragHandle = { BottomSheetDefaults.DragHandle(color = colors.textTertiary) }
            ) {
                HistoryContent(
                    currentMode = modes[currentIndex].name,
                    onExpressionSelected = { expr, _ ->
                        if (modes[currentIndex].name == "STANDARD") {
                            val v = standardTextFieldValue
                            val cursor = v.selection.start
                            val newText = if (v.text == "0") expr
                            else v.text.substring(0, cursor) + expr + v.text.substring(cursor)
                            val newCursor = if (v.text == "0") expr.length else cursor + expr.length
                            standardTextFieldValue = TextFieldValue(newText, TextRange(newCursor))
                        } else {
                            val v = scientificTextFieldValue
                            val cursor = v.selection.start
                            val newText = if (v.text == "0") expr
                            else v.text.substring(0, cursor) + expr + v.text.substring(cursor)
                            val newCursor = if (v.text == "0") expr.length else cursor + expr.length
                            scientificTextFieldValue = TextFieldValue(newText, TextRange(newCursor))
                        }
                        showHistory = false
                    }
                )
            }
        }
    }
}

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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val barHeight = if (isLandscape) 48.dp else 56.dp
    val iconSize = if (isLandscape) 40.dp else 44.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(colors.background)
            .padding(horizontal = 8.dp)
    ) {
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colors.functionButton.copy(alpha = 0.5f))
                    .clickable { menuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colors.textPrimary, modifier = Modifier.size(24.dp))
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
                    onClick = { menuExpanded = false; onMenuItemClick("Settings") },
                    modifier = Modifier.background(Color.Transparent).padding(4.dp)
                )
                DropdownMenuItem(
                    text = { Text("About", color = colors.textPrimary) },
                    onClick = { menuExpanded = false; onMenuItemClick("About") },
                    modifier = Modifier.background(Color.Transparent).padding(4.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center)
        ) {
            listOf(
                CalculatorMode.SCIENTIFIC to "Scientific",
                CalculatorMode.STANDARD to "Standard",
                CalculatorMode.CONVERTER to "Converter"
            ).forEach { (m, name) ->
                val selected = m == mode
                IconButton(
                    onClick = { onModeChange(m) },
                    modifier = Modifier
                        .size(iconSize)
                        .background(
                            if (selected) colors.toolbarActive.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        painter = painterResource(getModeIcon(m, isDarkTheme)),
                        contentDescription = name,
                        modifier = Modifier.size(24.dp),
                        tint = if (selected) colors.toolbarActive else colors.textSecondary
                    )
                }
            }
        }
        if (onHistoryClick != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = onHistoryClick, modifier = Modifier.size(iconSize)) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = colors.textPrimary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val expression = textFieldValue.text
    val preview = StandardCalculatorEngine.evaluateExpressionOrBlank(expression)

    fun handleKey(label: String) {
        if (label == "=") {
            val result = solveEquation(expression)
            onTextFieldValueChange(TextFieldValue(result, TextRange(result.length)))
            HistoryManager.addEntry(expression, result, "STANDARD")
            justEvaluated = true
            keyboardEnabled = false
            keyboardController?.hide()
            return
        }
        onTextFieldValueChange(standardHandleButtonPress(textFieldValue, label, justEvaluated))
        justEvaluated = false
        keyboardEnabled = false
        keyboardController?.hide()
        val sciFns = listOf("sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹", "log", "ln", "exp", "%", "|x|")
        if (sciFns.contains(label) && isFunctionExpanded) onToggleFunction()
    }

    val bottomInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom)

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Column(
                modifier = Modifier.weight(0.42f).fillMaxHeight()
            ) {
                CalculatorTopBar(
                    mode = CalculatorMode.STANDARD,
                    onModeChange = onModeChange,
                    onHistoryClick = onHistoryClick,
                    onMenuItemClick = onMenuItemClick
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(colors.displayBackground)
                ) {
                    EditableDisplay(
                        textFieldValue = textFieldValue,
                        preview = preview,
                        onTextFieldValueChange = onTextFieldValueChange,
                        onPaste = { pasted ->
                            val newText = textFieldValue.text.substring(0, textFieldValue.selection.start) +
                                    pasted + textFieldValue.text.substring(textFieldValue.selection.end)
                            onTextFieldValueChange(TextFieldValue(newText, TextRange(textFieldValue.selection.start + pasted.length)))
                        },
                        modifier = Modifier.fillMaxSize(),
                        isScientific = false
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
                    .windowInsetsPadding(bottomInsets)
            ) {
                StandardKeypadWithScientific(
                    isExpanded = isFunctionExpanded,
                    isLandscape = true,
                    onKeyPress = ::handleKey
                )
            }
        }
    } else {
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
                    .weight(1f)
                    .background(colors.displayBackground)
            ) {
                EditableDisplay(
                    textFieldValue = textFieldValue,
                    preview = preview,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onPaste = { pasted ->
                        val newText = textFieldValue.text.substring(0, textFieldValue.selection.start) +
                                pasted + textFieldValue.text.substring(textFieldValue.selection.end)
                        onTextFieldValueChange(TextFieldValue(newText, TextRange(textFieldValue.selection.start + pasted.length)))
                    },
                    modifier = Modifier.fillMaxSize(),
                    isScientific = false
                )
            }
            CompactFunctionRow(
                isExpanded = isFunctionExpanded,
                onToggle = onToggleFunction,
                onFunctionClick = { func ->
                    onTextFieldValueChange(standardHandleButtonPress(textFieldValue, func, justEvaluated))
                    justEvaluated = false
                    keyboardEnabled = false
                    keyboardController?.hide()
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(bottomInsets)
            ) {
                StandardKeypadWithScientific(
                    isExpanded = isFunctionExpanded,
                    isLandscape = false,
                    onKeyPress = ::handleKey
                )
            }
        }
    }
}

private fun standardHandleButtonPress(current: TextFieldValue, label: String, justEvaluated: Boolean): TextFieldValue {
    val text = current.text
    val start = current.selection.start.coerceIn(0, text.length)
    val end = current.selection.end.coerceIn(0, text.length)
    return when {
        label == "AC" -> TextFieldValue("", TextRange(0))
        label == "=" -> {
            val result = StandardCalculatorEngine.evaluateExpression(text)
            TextFieldValue(result, TextRange(result.length))
        }
        label == "⌫" -> {
            if (start == 0 && end == 0) return current
            val newText = if (start != end) text.substring(0, start) + text.substring(end)
            else text.substring(0, start - 1) + text.substring(start)
            val newCursor = if (start != end) start else (start - 1).coerceAtLeast(0)
            TextFieldValue(newText, TextRange(newCursor))
        }
        justEvaluated && label.length == 1 && label[0].isDigit() -> TextFieldValue(label, TextRange(label.length))
        else -> {
            val transformed = StandardCalculatorEngine.handleKeyPress("", label)
            val newText = if (text.isEmpty()) transformed else text.substring(0, start) + transformed + text.substring(end)
            val newCursor = if (text.isEmpty()) transformed.length else start + transformed.length
            TextFieldValue(newText, TextRange(newCursor))
        }
    }
}

@Composable
fun CompactFunctionRow(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onFunctionClick: (String) -> Unit,
) {
    val colors = LocalCalculatorColors.current
    val isDarkTheme = isSystemInDarkTheme()
    val toggleIconRes = when {
        isExpanded && isDarkTheme -> R.drawable.ic_flask_up_dark
        isExpanded && !isDarkTheme -> R.drawable.ic_flask_up_light
        !isExpanded && isDarkTheme -> R.drawable.ic_flask_down_dark
        else -> R.drawable.ic_flask_down_light
    }
    Column(modifier = Modifier.fillMaxWidth().background(colors.background).padding(horizontal = 8.dp, vertical = 4.dp)) {
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
                    IconButton(onClick = { onFunctionClick(label) }, modifier = Modifier.size(40.dp)) {
                        Text(label, color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Icon(
                painter = painterResource(toggleIconRes),
                contentDescription = if (isExpanded) "Hide functions" else "Show functions",
                tint = colors.textPrimary,
                modifier = Modifier.size(32.dp).clickable { onToggle() }
            )
        }
    }
}

@Composable
fun StandardKeypadWithScientific(
    isExpanded: Boolean,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val row1 = listOf(
        KeyDef("AC", KeyStyle.FUNCTION), KeyDef("(", KeyStyle.FUNCTION),
        KeyDef(")", KeyStyle.FUNCTION), KeyDef("÷", KeyStyle.FUNCTION)
    )
    val row2 = if (isExpanded) listOf(
        KeyDef("sin", KeyStyle.SCIENTIFIC), KeyDef("cos", KeyStyle.SCIENTIFIC),
        KeyDef("tan", KeyStyle.SCIENTIFIC), KeyDef("×", KeyStyle.FUNCTION)
    ) else listOf(
        KeyDef("7", KeyStyle.NUMBER), KeyDef("8", KeyStyle.NUMBER),
        KeyDef("9", KeyStyle.NUMBER), KeyDef("×", KeyStyle.FUNCTION)
    )
    val row3 = if (isExpanded) listOf(
        KeyDef("sin⁻¹", KeyStyle.SCIENTIFIC), KeyDef("cos⁻¹", KeyStyle.SCIENTIFIC),
        KeyDef("tan⁻¹", KeyStyle.SCIENTIFIC), KeyDef("−", KeyStyle.FUNCTION)
    ) else listOf(
        KeyDef("4", KeyStyle.NUMBER), KeyDef("5", KeyStyle.NUMBER),
        KeyDef("6", KeyStyle.NUMBER), KeyDef("−", KeyStyle.FUNCTION)
    )
    val row4 = if (isExpanded) listOf(
        KeyDef("log", KeyStyle.SCIENTIFIC), KeyDef("ln", KeyStyle.SCIENTIFIC),
        KeyDef("exp", KeyStyle.SCIENTIFIC), KeyDef("+", KeyStyle.FUNCTION)
    ) else listOf(
        KeyDef("1", KeyStyle.NUMBER), KeyDef("2", KeyStyle.NUMBER),
        KeyDef("3", KeyStyle.NUMBER), KeyDef("+", KeyStyle.FUNCTION)
    )
    val row5 = if (isExpanded) listOf(
        KeyDef("%", KeyStyle.SCIENTIFIC), KeyDef("|x|", KeyStyle.SCIENTIFIC),
        KeyDef("⌫", KeyStyle.ACCENT), KeyDef("=", KeyStyle.ACCENT)
    ) else listOf(
        KeyDef("0", KeyStyle.NUMBER), KeyDef(".", KeyStyle.NUMBER),
        KeyDef("⌫", KeyStyle.ACCENT), KeyDef("=", KeyStyle.ACCENT)
    )
    val rows = listOf(row1, row2, row3, row4, row5)

    val columnModifier = if (isLandscape) {
        modifier.fillMaxSize().background(colors.background).padding(4.dp)
    } else {
        modifier.fillMaxWidth().background(colors.background).padding(8.dp)
    }

    Column(modifier = columnModifier) {
        rows.forEach { row ->
            Row(
                modifier = if (isLandscape) Modifier.fillMaxWidth().weight(1f) else Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 8.dp)
            ) {
                row.forEach { key ->
                    val bg = when (key.style) {
                        KeyStyle.NUMBER -> colors.numberButton
                        KeyStyle.SCIENTIFIC -> colors.numberButton
                        KeyStyle.FUNCTION -> colors.functionButton
                        KeyStyle.ACCENT -> colors.accentButton
                    }
                    val fontSize = when {
                        isLandscape && key.label.length > 3 -> 12.sp
                        isLandscape -> 15.sp
                        key.label.length > 3 && key.style == KeyStyle.SCIENTIFIC -> 13.sp
                        key.style == KeyStyle.SCIENTIFIC -> 15.sp
                        key.label == "AC" -> 18.sp
                        key.label == "=" -> 28.sp
                        else -> 24.sp
                    }
                    val btnModifier = if (isLandscape) {
                        Modifier.weight(1f).fillMaxHeight().padding(2.dp)
                    } else {
                        Modifier.weight(1f).aspectRatio(1f).padding(4.dp)
                    }
                    Box(
                        modifier = btnModifier
                            .clip(RoundedCornerShape(if (isLandscape) 12.dp else 20.dp))
                            .background(bg)
                            .clickable { onKeyPress(key.label) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            key.label,
                            color = colors.textPrimary,
                            fontSize = fontSize,
                            fontWeight = if (key.style == KeyStyle.ACCENT) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditableDisplay(
    textFieldValue: TextFieldValue,
    preview: String,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onPaste: (String) -> Unit,
    modifier: Modifier = Modifier,
    isScientific: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(textFieldValue.text.length) {
        coroutineScope.launch { delay(50.milliseconds); scrollState.animateScrollTo(scrollState.maxValue) }
    }

    val charCount = textFieldValue.text.length
    val expressionFontSize: Float
    val previewFontSize: Float
    val maxHeight: Dp = 10000.dp
    if (isScientific) {
        val longestToken = textFieldValue.text.split(Regex("[^0-9A-Za-z.]+")).maxOfOrNull { it.length } ?: 0
        expressionFontSize = when {
            longestToken <= 20 -> 26f
            longestToken <= 30 -> 23f
            longestToken <= 45 -> 20f
            else -> 18f
        }
        previewFontSize = 20f
    } else {
        expressionFontSize = when {
            charCount <= 6 -> 64f
            charCount <= 9 -> 56f
            charCount <= 12 -> 48f
            charCount <= 16 -> 42f
            charCount <= 20 -> 36f
            charCount <= 26 -> 32f
            else -> 28f
        }
        previewFontSize = 26f
    }

    Box(
        modifier = modifier.fillMaxSize().background(colors.displayBackground)
            .padding(horizontal = if (isScientific) 14.dp else 20.dp, vertical = 12.dp)
    ) {
        Column(
            horizontalAlignment = if (isScientific) Alignment.Start else Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            if (preview.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .height(if (isScientific) 50.dp else 60.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = preview,
                        color = colors.accentText,
                        fontSize = previewFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().clickable {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("result", preview))
                            Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                        },
                        textAlign = if (isScientific) TextAlign.Start else TextAlign.End,
                        overflow = TextOverflow.Visible
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            InterceptPlatformTextInput(interceptor = { _, _ -> awaitCancellation() }) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = onTextFieldValueChange,
                    textStyle = TextStyle(
                        fontSize = expressionFontSize.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = if (isScientific) TextAlign.Start else TextAlign.End
                    ),
                    cursorBrush = SolidColor(colors.accentButton),
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 40.dp, max = maxHeight)
                        .verticalScroll(scrollState)
                        .focusRequester(focusRequester)
                        .onFocusChanged { hasFocus = it.isFocused }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    val items = mutableListOf<Pair<String, () -> Unit>>()
                                    items.add("Copy Expression" to {
                                        clipboardManager.setPrimaryClip(ClipData.newPlainText("expression", textFieldValue.text))
                                        Toast.makeText(context, "Expression copied", Toast.LENGTH_SHORT).show()
                                    })
                                    if (preview.isNotEmpty()) {
                                        items.add("Copy Result" to {
                                            clipboardManager.setPrimaryClip(ClipData.newPlainText("result", preview))
                                            Toast.makeText(context, "Result copied", Toast.LENGTH_SHORT).show()
                                        })
                                    }
                                    val clip = clipboardManager.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        clip.getItemAt(0).text?.toString()?.let { text ->
                                            if (text.isNotBlank()) {
                                                items.add("Paste" to { onPaste(smartFormatPastedText(text)) })
                                            }
                                        }
                                    }
                                    if (items.isNotEmpty()) {
                                        AlertDialog.Builder(context)
                                            .setTitle("Options")
                                            .setItems(items.map { it.first }.toTypedArray()) { _, which -> items[which].second.invoke() }
                                            .show()
                                    }
                                }
                            )
                        }
                )
            }
        }
    }
}

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
                    if (rightValue != null) return "$variable = $rightValue"
                }
            }
        }
        return StandardCalculatorEngine.evaluateExpression(expression)
    } catch (_: Exception) {
        return StandardCalculatorEngine.evaluateExpression(expression)
    }
}

fun smartFormatPastedText(raw: String): String {
    var cleaned = raw
        .replace(Regex("[₹$€£¥]"), "")
        .replace(Regex(","), "")
        .replace(Regex("\\s+"), " ")
        .trim()
    cleaned = cleaned.replace("*", "×").replace("/", "÷").replace("x", "×").replace("X", "×")
    return cleaned.replace(Regex("\\s+"), " ")
}

// ==================== HISTORY (unchanged visuals) ====================

private fun formatMetaTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 24L * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 7L * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun groupByDate(items: List<HistoryItem>): List<Pair<String, List<HistoryItem>>> {
    if (items.isEmpty()) return emptyList()
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val todayStart = cal.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L
    val weekStart = todayStart - 6 * 86_400_000L
    val today = mutableListOf<HistoryItem>()
    val yesterday = mutableListOf<HistoryItem>()
    val thisWeek = mutableListOf<HistoryItem>()
    val earlier = mutableListOf<HistoryItem>()
    items.forEach {
        when {
            it.timestamp >= todayStart -> today.add(it)
            it.timestamp >= yesterdayStart -> yesterday.add(it)
            it.timestamp >= weekStart -> thisWeek.add(it)
            else -> earlier.add(it)
        }
    }
    return buildList {
        if (today.isNotEmpty()) add("TODAY" to today)
        if (yesterday.isNotEmpty()) add("YESTERDAY" to yesterday)
        if (thisWeek.isNotEmpty()) add("THIS WEEK" to thisWeek)
        if (earlier.isNotEmpty()) add("EARLIER" to earlier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    currentMode: String,
    onExpressionSelected: (String, String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var historyItems by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(if (currentMode == "CONVERTER") "ALL" else currentMode) }
    var selectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var expandedId by remember { mutableStateOf<Long?>(null) }
    var clearMenuOpen by remember { mutableStateOf(false) }
    var keepPinned by remember { mutableStateOf(HistoryManager.getKeepPinned()) }

    fun refresh() { historyItems = HistoryManager.getHistory() }
    LaunchedEffect(Unit) { refresh() }

    LaunchedEffect(selectedIds) {
        if (selectMode && selectedIds.isEmpty()) selectMode = false
    }

    BackHandler(enabled = selectMode) {
        selectMode = false
        selectedIds = emptySet()
    }
    BackHandler(enabled = !selectMode && expandedId != null) {
        expandedId = null
    }

    fun snack(message: String, action: String? = null, onAction: (() -> Unit)? = null) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = action,
                duration = if (action != null) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && onAction != null) onAction()
        }
    }

    fun copyToClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("history", text))
        snack("Copied")
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    val filtered = remember(historyItems, query, filter) {
        historyItems.filter { item ->
            (filter == "ALL" || item.mode == filter) &&
                    (query.isEmpty() ||
                            item.expression.contains(query, ignoreCase = true) ||
                            item.result.contains(query, ignoreCase = true))
        }
    }
    val pinnedItems = remember(filtered) { filtered.filter { it.pinned } }
    val unpinnedItems = remember(filtered) { filtered.filter { !it.pinned } }
    val grouped = remember(unpinnedItems) { groupByDate(unpinnedItems) }
    val selectedItems = remember(historyItems, selectedIds) {
        historyItems.filter { it.timestamp in selectedIds }
    }
    val allSelectedPinned = remember(selectedItems) {
        selectedItems.isNotEmpty() && selectedItems.all { it.pinned }
    }

    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 4.dp,
                bottom = 4.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                Column {
                    if (selectMode) {
                        SelectBar(
                            count = selectedIds.size,
                            allPinned = allSelectedPinned,
                            onCancel = { selectMode = false; selectedIds = emptySet() },
                            onPinToggle = {
                                if (allSelectedPinned) {
                                    HistoryManager.setPinned(selectedIds.toList(), false)
                                    snack("Unpinned ${selectedIds.size}")
                                } else {
                                    HistoryManager.setPinned(selectedIds.toList(), true)
                                    snack("Pinned ${selectedIds.size}")
                                }
                                refresh()
                                selectMode = false
                                selectedIds = emptySet()
                            },
                            onCopy = {
                                copyToClipboard(selectedItems.joinToString("\n") { "${it.expression} = ${it.result}" })
                            },
                            onShare = {
                                shareText(selectedItems.joinToString("\n") { "${it.expression} = ${it.result}" })
                            },
                            onDelete = {
                                val removed = HistoryManager.deleteItems(selectedIds.toList())
                                refresh()
                                selectMode = false
                                selectedIds = emptySet()
                                snack("${removed.size} entries deleted", "UNDO") {
                                    HistoryManager.restoreItems(removed)
                                    refresh()
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        FilterChips(
                            filter = filter,
                            onFilterChange = { filter = it },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                    } else {
                        HistoryHeader(onOverflowClick = { clearMenuOpen = true })
                        SearchBarAndChips(
                            query = query,
                            onQueryChange = { query = it },
                            filter = filter,
                            onFilterChange = { filter = it }
                        )
                    }
                }
            }

            if (pinnedItems.isNotEmpty()) {
                item(key = "pinned") {
                    PinnedStrip(
                        items = pinnedItems,
                        selectedIds = selectedIds,
                        onItemTap = { item ->
                            if (selectMode) {
                                selectedIds = if (item.timestamp in selectedIds)
                                    selectedIds - item.timestamp else selectedIds + item.timestamp
                            } else {
                                onExpressionSelected(item.result, item.mode)
                            }
                        },
                        onItemLongPress = { item ->
                            selectMode = true
                            selectedIds = selectedIds + item.timestamp
                        },
                        onUnpin = { item ->
                            HistoryManager.setPinned(listOf(item.timestamp), false)
                            refresh()
                            snack("Unpinned", "UNDO") {
                                HistoryManager.setPinned(listOf(item.timestamp), true)
                                refresh()
                            }
                        }
                    )
                }
            }

            if (filtered.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        isSearching = query.isNotEmpty(),
                        modifier = Modifier.fillParentMaxHeight(0.6f)
                    )
                }
            } else {
                grouped.forEach { (label, entries) ->
                    item(key = "hdr_$label") {
                        Text(
                            text = label,
                            color = colors.textTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    items(entries, key = { it.timestamp }) { item ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            HistoryCard(
                                item = item,
                                colors = colors,
                                showMode = filter == "ALL",
                                isExpanded = expandedId == item.timestamp,
                                selectMode = selectMode,
                                isSelected = item.timestamp in selectedIds,
                                onTap = {
                                    if (selectMode) {
                                        selectedIds = if (item.timestamp in selectedIds)
                                            selectedIds - item.timestamp else selectedIds + item.timestamp
                                    } else {
                                        expandedId = if (expandedId == item.timestamp) null else item.timestamp
                                    }
                                },
                                onLongPress = {
                                    if (!selectMode) {
                                        selectMode = true
                                        selectedIds = setOf(item.timestamp)
                                    }
                                },
                                onTogglePin = {
                                    HistoryManager.togglePin(listOf(item.timestamp))
                                    refresh()
                                },
                                onDelete = {
                                    val removed = HistoryManager.deleteItems(listOf(item.timestamp))
                                    refresh()
                                    snack("1 entry deleted", "UNDO") {
                                        HistoryManager.restoreItems(removed)
                                        refresh()
                                    }
                                },
                                onUseExpression = { onExpressionSelected(item.expression, item.mode) },
                                onUseResult = { onExpressionSelected(item.result, item.mode) },
                                onCopy = { copyToClipboard("${item.expression} = ${item.result}") },
                                onShare = { shareText("${item.expression} = ${item.result}") }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = colors.functionButton,
                contentColor = colors.textPrimary,
                actionColor = colors.accentText
            )
        }

        if (clearMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { clearMenuOpen = false }
            )
            HistoryOverflowMenu(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 60.dp, end = 20.dp),
                standardCount = HistoryManager.count("STANDARD"),
                scientificCount = HistoryManager.count("SCIENTIFIC"),
                allCount = HistoryManager.count(null),
                keepPinned = keepPinned,
                onKeepPinnedChange = {
                    keepPinned = it
                    HistoryManager.setKeepPinned(it)
                },
                onClearStandard = {
                    val removed = HistoryManager.clearHistory("STANDARD", keepPinned)
                    refresh(); clearMenuOpen = false
                    if (removed.isNotEmpty()) snack("History cleared", "UNDO") {
                        HistoryManager.restoreItems(removed); refresh()
                    }
                },
                onClearScientific = {
                    val removed = HistoryManager.clearHistory("SCIENTIFIC", keepPinned)
                    refresh(); clearMenuOpen = false
                    if (removed.isNotEmpty()) snack("History cleared", "UNDO") {
                        HistoryManager.restoreItems(removed); refresh()
                    }
                },
                onClearAll = {
                    val removed = HistoryManager.clearHistory(null, keepPinned)
                    refresh(); clearMenuOpen = false
                    if (removed.isNotEmpty()) snack("History cleared", "UNDO") {
                        HistoryManager.restoreItems(removed); refresh()
                    }
                }
            )
        }
    }
}

@Composable
private fun HistoryHeader(onOverflowClick: () -> Unit) {
    val colors = LocalCalculatorColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "History",
            color = colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onOverflowClick, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "History options", tint = colors.textPrimary)
        }
    }
}

@Composable
private fun HistoryOverflowMenu(
    modifier: Modifier = Modifier,
    standardCount: Int,
    scientificCount: Int,
    allCount: Int,
    keepPinned: Boolean,
    onKeepPinnedChange: (Boolean) -> Unit,
    onClearStandard: () -> Unit,
    onClearScientific: () -> Unit,
    onClearAll: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    Surface(
        modifier = modifier.width(292.dp),
        shape = RoundedCornerShape(22.dp),
        color = colors.menuSurface,
        border = BorderStroke(1.dp, colors.menuBorder),
        shadowElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            MenuRow(Icons.Default.Calculate, "Clear Standard history", standardCount, colors, false, onClearStandard)
            MenuRow(Icons.Default.Science, "Clear Scientific history", scientificCount, colors, false, onClearScientific)
            HorizontalDivider(color = colors.menuBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            MenuRow(Icons.Default.Delete, "Clear all history", allCount, colors, true, onClearAll)
            HorizontalDivider(color = colors.menuBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keep pinned entries",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = keepPinned,
                    onCheckedChange = onKeepPinnedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onAccent,
                        checkedTrackColor = colors.accentButton,
                        uncheckedThumbColor = colors.textTertiary,
                        uncheckedTrackColor = colors.displayDivider
                    )
                )
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    count: Int,
    colors: CalculatorColors,
    danger: Boolean,
    onClick: () -> Unit
) {
    val color = if (danger) colors.dangerText else colors.textPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(
            text = count.toString(),
            color = if (danger) colors.dangerText.copy(alpha = 0.75f) else colors.textTertiary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SelectBar(
    count: Int,
    allPinned: Boolean,
    onCancel: () -> Unit,
    onPinToggle: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalCalculatorColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.numberButton)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel selection", tint = colors.textPrimary)
        }
        Text(
            text = "$count selected",
            color = colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        IconButton(onClick = onPinToggle, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = if (allPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                contentDescription = if (allPinned) "Unpin" else "Pin",
                tint = colors.textPrimary
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = colors.textPrimary)
        }
        IconButton(onClick = onShare, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = colors.textPrimary)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.textPrimary)
        }
    }
}

@Composable
private fun SearchBarAndChips(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: String,
    onFilterChange: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search calculations", color = colors.textTertiary, fontSize = 15.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.textTertiary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = colors.textTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 15.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.numberButton,
                unfocusedContainerColor = colors.numberButton,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = colors.accentText
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        FilterChips(filter = filter, onFilterChange = onFilterChange)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun FilterChips(
    filter: String,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("ALL" to "All", "STANDARD" to "Standard", "SCIENTIFIC" to "Scientific").forEach { (value, label) ->
            val selected = filter == value
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) colors.accentButton else colors.numberButton)
                    .clickable { onFilterChange(value) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) colors.onAccent else colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PinnedStrip(
    items: List<HistoryItem>,
    selectedIds: Set<Long>,
    onItemTap: (HistoryItem) -> Unit,
    onItemLongPress: (HistoryItem) -> Unit,
    onUnpin: (HistoryItem) -> Unit
) {
    val colors = LocalCalculatorColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PINNED",
            color = colors.textTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(items, key = { it.timestamp }) { item ->
                val isSelected = item.timestamp in selectedIds
                val selectedBg = colors.accentText.copy(alpha = 0.14f).compositeOver(colors.numberButton)
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) selectedBg else colors.numberButton)
                        .border(
                            width = if (isSelected) 1.dp else 1.5.dp,
                            color = colors.accentText,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .combinedClickable(
                            onClick = { onItemTap(item) },
                            onLongClick = { onItemLongPress(item) }
                        )
                        .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.accentText, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = item.expression,
                                color = colors.textTertiary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier.size(48.dp).clickable { onUnpin(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Unpin",
                                    tint = colors.accentText.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.result,
                            color = colors.accentText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colors.accentText),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryCard(
    item: HistoryItem,
    colors: CalculatorColors,
    showMode: Boolean,
    isExpanded: Boolean,
    selectMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onUseExpression: () -> Unit,
    onUseResult: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onTogglePin(); false }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        }
    )

    val selectedBg = colors.accentText.copy(alpha = 0.14f).compositeOver(colors.numberButton)

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !selectMode,
        enableDismissFromEndToStart = !selectMode,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            if (!selectMode && direction != SwipeToDismissBoxValue.Settled) {
                val isStart = direction == SwipeToDismissBoxValue.StartToEnd
                val progress = dismissState.progress
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    contentAlignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = if (isStart) Icons.Default.PushPin else Icons.Default.Delete,
                        contentDescription = null,
                        tint = (if (isStart) colors.accentText else colors.dangerText).copy(alpha = progress)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) selectedBg else colors.numberButton)
                .border(
                    width = 1.dp,
                    color = if (isSelected) colors.accentText else colors.displayDivider,
                    shape = RoundedCornerShape(20.dp)
                )
                .combinedClickable(onClick = onTap, onLongClick = onLongPress)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectMode) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.accentText else Color.Transparent)
                            .border(
                                width = if (isSelected) 0.dp else 2.dp,
                                color = if (isSelected) Color.Transparent else colors.textTertiary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    val meta = buildString {
                        append(formatMetaTime(item.timestamp))
                        if (showMode) {
                            append(" · ")
                            append(if (item.mode == "STANDARD") "Standard" else "Scientific")
                        }
                    }
                    Text(text = meta, color = colors.textTertiary, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.expression,
                        color = colors.textTertiary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.result,
                        color = colors.accentText,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (item.pinned && !selectMode) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = colors.accentText,
                        modifier = Modifier.size(16.dp).padding(start = 8.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded && !selectMode) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionChip("Use expression", onUseExpression, colors)
                        ActionChip("Use result", onUseResult, colors)
                        ActionChip("Copy", onCopy, colors)
                        ActionChip("Share", onShare, colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit, colors: CalculatorColors) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.functionButton)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyState(isSearching: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalCalculatorColors.current
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Default.Search else Icons.Default.History,
            contentDescription = null,
            tint = colors.textTertiary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isSearching) "No matching calculations" else "No history yet",
            color = colors.textTertiary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isSearching) "Try a different search or filter" else "Your calculations will appear here",
            color = colors.textTertiary.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun BMIScreen(onBack: () -> Unit) {
    val colors = LocalCalculatorColors.current
    BackHandler(onBack = onBack)
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<Double?>(null) }
    fun calculateBMI() {
        val h = height.toDoubleOrNull()?.div(100)
        val w = weight.toDoubleOrNull()
        bmiResult = if (h != null && w != null && h > 0) w / (h * h) else null
    }
    LaunchedEffect(height, weight) { calculateBMI() }
    Column(modifier = Modifier.fillMaxSize().background(colors.background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }
            Text("BMI Calculator", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = height,
            onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } },
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
            onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
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
                    Text(String.format("%.2f BMI", bmi), color = colors.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(classification, color = colors.accentButton, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    if (weightToLose > 0) {
                        Text("For healthy life you have to decrease: ${String.format("%.1f", weightToLose)} kg", color = colors.textSecondary, fontSize = 16.sp)
                    } else if (bmi < 18.5) {
                        val weightToGain = idealWeightLow - (weight.toDoubleOrNull() ?: 0.0)
                        Text("For healthy life you have to increase: ${String.format("%.1f", weightToGain)} kg", color = colors.textSecondary, fontSize = 16.sp)
                    } else {
                        Text("You are in a healthy range. Keep it up!", color = colors.textSecondary, fontSize = 16.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth().background(colors.functionButton, RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                listOf(
                    "< 16.0" to "Very severely underweight", "16.0 ~ 16.9" to "Severely underweight",
                    "17.0 ~ 18.4" to "Underweight", "18.5 ~ 24.9" to "Healthy",
                    "25.0 ~ 29.9" to "Overweight", "30.0 ~ 34.9" to "Obese Class I",
                    "35.0 ~ 39.9" to "Obese Class II", "≥ 40.0" to "Obese Class III"
                ).forEach { (range, label) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

@Composable
fun ShoppingScreen(onBack: () -> Unit) {
    val colors = LocalCalculatorColors.current
    BackHandler(onBack = onBack)
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().background(colors.background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }
            Text("Shopping Calculator", color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Discount", "Unit Price").forEachIndexed { index, label ->
                Button(
                    onClick = { selectedTab = index },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) colors.accentButton else colors.functionButton,
                        contentColor = if (selectedTab == index) colors.onAccent else colors.textSecondary
                    ),
                    modifier = Modifier.weight(1f)
                ) { Text(label, fontSize = 16.sp) }
            }
        }
        Spacer(Modifier.height(16.dp))
        when (selectedTab) { 0 -> DiscountTab(); 1 -> UnitPriceTab() }
    }
}

@Composable
fun DiscountTab() {
    val colors = LocalCalculatorColors.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var originalPrice by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var discountedPrice by remember { mutableStateOf("") }
    var savedAmount by remember { mutableStateOf("") }
    @SuppressLint("DefaultLocale")
    fun calculate() {
        val orig = originalPrice.toDoubleOrNull()
        val disc = discountPercent.toDoubleOrNull()
        val discPrice = discountedPrice.toDoubleOrNull()
        val saved = savedAmount.toDoubleOrNull()
        when {
            orig != null && disc != null -> {
                val np = orig * (1 - disc / 100)
                discountedPrice = String.format("%.2f", np)
                savedAmount = String.format("%.2f", orig - np)
            }
            orig != null && discPrice != null -> {
                discountPercent = String.format("%.2f", (1 - discPrice / orig) * 100)
                savedAmount = String.format("%.2f", orig - discPrice)
            }
            orig != null && saved != null -> {
                val np = orig - saved
                discountedPrice = String.format("%.2f", np)
                discountPercent = String.format("%.2f", (saved / orig) * 100)
            }
            disc != null && discPrice != null -> {
                val no = discPrice / (1 - disc / 100)
                originalPrice = String.format("%.2f", no)
                savedAmount = String.format("%.2f", no - discPrice)
            }
            disc != null && saved != null -> {
                val no = saved / (disc / 100)
                originalPrice = String.format("%.2f", no)
                discountedPrice = String.format("%.2f", no - saved)
            }
            discPrice != null && saved != null -> {
                val no = discPrice + saved
                originalPrice = String.format("%.2f", no)
                discountPercent = String.format("%.2f", (saved / no) * 100)
            }
        }
    }
    LaunchedEffect(Unit) { delay(100.milliseconds); focusRequester.requestFocus(); keyboardController?.show() }
    LaunchedEffect(originalPrice, discountPercent, discountedPrice, savedAmount) { calculate() }
    Column {
        OutlinedTextField(
            value = originalPrice,
            onValueChange = { originalPrice = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Original Price", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton, unfocusedBorderColor = colors.textSecondary, focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                .clickable { focusRequester.requestFocus(); keyboardController?.show() },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = discountPercent,
            onValueChange = { discountPercent = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Discount (%)", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton, unfocusedBorderColor = colors.textSecondary, focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = discountedPrice,
            onValueChange = { discountedPrice = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Discounted Price", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton, unfocusedBorderColor = colors.textSecondary, focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = savedAmount,
            onValueChange = { savedAmount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Saved Amount", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton, unfocusedBorderColor = colors.textSecondary, focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun UnitPriceTab() {
    val colors = LocalCalculatorColors.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var totalAmount by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    @SuppressLint("DefaultLocale")
    fun calculate() {
        val total = totalAmount.toDoubleOrNull()
        val qty = quantity.toDoubleOrNull()
        val price = unitPrice.toDoubleOrNull()
        when {
            total != null && qty != null && qty != 0.0 -> unitPrice = String.format("%.2f", total / qty)
            total != null && price != null -> if (price != 0.0) quantity = String.format("%.2f", total / price)
            qty != null && price != null && qty != 0.0 && price != 0.0 -> totalAmount = String.format("%.2f", qty * price)
        }
    }
    LaunchedEffect(Unit) { delay(100.milliseconds); focusRequester.requestFocus(); keyboardController?.show() }
    LaunchedEffect(totalAmount, quantity, unitPrice) { calculate() }
    Column {
        OutlinedTextField(
            value = totalAmount,
            onValueChange = { totalAmount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Total Amount", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton, unfocusedBorderColor = colors.textSecondary, focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                .clickable { focusRequester.requestFocus(); keyboardController?.show() },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Quantity", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton, unfocusedBorderColor = colors.textSecondary, focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = unitPrice,
            onValueChange = { unitPrice = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Unit Price", color = colors.textSecondary) },
            textStyle = TextStyle(color = colors.textPrimary, fontSize = 18.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentButton, unfocusedBorderColor = colors.textSecondary, focusedLabelColor = colors.accentButton
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun ConverterScreen(
    onModeChange: (CalculatorMode) -> Unit,
    onCategoryClick: (String) -> Unit,
    onMenuItemClick: (String) -> Unit
) {
    val colors = LocalCalculatorColors.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val columnCount = when {
        screenWidthDp >= 900 -> 6
        screenWidthDp >= 600 -> 5
        isLandscape -> 4
        else -> 3
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
    ) {
        CalculatorTopBar(
            mode = CalculatorMode.CONVERTER,
            onModeChange = onModeChange,
            onHistoryClick = null,
            onMenuItemClick = onMenuItemClick
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(converterCategories) { category ->
                ConverterCard(category = category, onClick = { onCategoryClick(category.id) })
            }
        }
    }
}

@Composable
fun ConverterCard(category: ConverterCategory, onClick: () -> Unit) {
    val colors = LocalCalculatorColors.current
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).padding(4.dp).clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.converterButton)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(painterResource(category.iconRes), contentDescription = category.name, modifier = Modifier.size(36.dp), tint = colors.converterIconTint)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary, modifier = Modifier.size(20.dp))
            }
            SettingsButton(angleMode, listOf("RAD" to "Radians", "DEG" to "Degrees", "GRA" to "Gradians"), onAngleModeChange)
            SettingsButton(displayMode, listOf("NORM" to "Normal", "SCI" to "Scientific", "ENG" to "Engineering"), onDisplayModeChange)
            SettingsButton(
                fractionMode,
                listOf(
                    "DEC" to "Decimal", "DEC EXACT" to "Decimal Exact", "FRACTIONAL" to "Fractional",
                    "COMBINED" to "Combined", "PERCENT" to "Percent", "PERMILLE" to "Permille", "PERMYRIAD" to "Permyriad"
                ),
                onFractionModeChange
            )
            SettingsButton(approxMode, listOf("EXACT" to "Always Exact", "TRY EXACT" to "Try Exact", "APPROX" to "Approximate"), onApproxModeChange)
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
                onClick = { onOptionSelected(short); expanded = false },
                modifier = Modifier.background(colors.functionButton)
            )
        }
    }
}

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (keyboardEnabled) colors.accentButton else Color.Transparent)
                .clickable { onKeyboardEnabledChange(!keyboardEnabled) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Keyboard,
                contentDescription = if (keyboardEnabled) "Hide Keyboard" else "Show Keyboard",
                tint = if (keyboardEnabled) colors.onAccent else colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo",
                tint = if (canUndo) colors.textSecondary else colors.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp))
        }
        IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo",
                tint = if (canRedo) colors.textSecondary else colors.textSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp))
        }
    }
}

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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
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
    var undoRedoState by remember { mutableStateOf(UndoRedoState(emptyList(), emptyList(), textFieldValue)) }
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
            val nu = undoRedoState.undoStack.dropLast(1)
            val p = undoRedoState.undoStack.last()
            val nr = undoRedoState.redoStack + undoRedoState.current
            undoRedoState = UndoRedoState(nu, nr, p)
            onTextFieldValueChange(p)
        }
    }
    fun redo() {
        if (undoRedoState.canRedo) {
            val nr = undoRedoState.redoStack.dropLast(1)
            val n = undoRedoState.redoStack.last()
            val nu = undoRedoState.undoStack + undoRedoState.current
            undoRedoState = UndoRedoState(nu, nr, n)
            onTextFieldValueChange(n)
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
    val parsedExpr = remember(expression, angleMode) { QalculateEngine.parseExpression(expression) }
    val answerScrollState = rememberScrollState()
    val inputScrollState = rememberScrollState()
    val parsedScrollState = rememberScrollState()
    LaunchedEffect(answer) { if (answer.isNotEmpty()) answerScrollState.animateScrollTo(answerScrollState.maxValue) }
    LaunchedEffect(expression) { if (expression.isNotEmpty()) inputScrollState.animateScrollTo(inputScrollState.maxValue) }
    LaunchedEffect(parsedExpr) { if (parsedExpr.isNotEmpty()) parsedScrollState.animateScrollTo(parsedScrollState.maxValue) }

    val displayBg = colors.displayBackground
    val answerColor = colors.accentText
    val inputColor = colors.textPrimary
    val dividerColor = colors.displayDivider

    fun handleKeyPress(label: String) {
        if (label == "Enter") {
            val result = QalculateEngine.evaluateExpression(expression)
            if (result != "Error") HistoryManager.addEntry(expression, result, "SCIENTIFIC")
            justEvaluated = true
        } else {
            onTextFieldValueChange(scientificHandleKeyPress(textFieldValue, label, justEvaluated))
            justEvaluated = false
        }
        keyboardEnabled = false
        keyboardController?.hide()
    }

    @Composable
    fun DisplayPane(modifier: Modifier) {
        Column(modifier = modifier.background(displayBg).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp).horizontalScroll(answerScrollState)) {
                if (answer.isNotEmpty()) {
                    Text(
                        text = answer, color = answerColor, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().clickable {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("answer", answer))
                            Toast.makeText(context, "Answer copied", Toast.LENGTH_SHORT).show()
                        },
                        textAlign = TextAlign.End, maxLines = 1, softWrap = false, overflow = TextOverflow.Visible
                    )
                } else {
                    Text("Waiting for input...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 3.dp), thickness = 1.dp, color = dividerColor)
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(inputScrollState),
                contentAlignment = Alignment.BottomStart
            ) {
                key(keyboardEnabled) {
                    InterceptPlatformTextInput(
                        interceptor = { request, nextHandler ->
                            if (keyboardEnabled) nextHandler.startInputMethod(request) else awaitCancellation()
                        }
                    ) {
                        val focusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChange,
                            textStyle = TextStyle(
                                fontSize = when {
                                    expression.length <= 10 -> 22.sp
                                    expression.length <= 20 -> 18.sp
                                    else -> 15.sp
                                },
                                color = inputColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start
                            ),
                            cursorBrush = SolidColor(answerColor),
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 3.dp), thickness = 1.dp, color = dividerColor)
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 20.dp, max = 50.dp).verticalScroll(parsedScrollState)) {
                if (parsedExpr.isNotEmpty()) {
                    Text(
                        text = colorizeParsedExpression(parsedExpr), fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().clickable {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("parsed", parsedExpr))
                            Toast.makeText(context, "Expression copied", Toast.LENGTH_SHORT).show()
                        },
                        softWrap = true, maxLines = Int.MAX_VALUE
                    )
                }
            }
        }
    }

    val bottomInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom)

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Column(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
                ScientificTopBar(
                    onBack = onBack, onHistoryClick = onHistoryClick, onMenuItemClick = onMenuItemClick,
                    angleMode = angleMode, onAngleModeChange = { angleMode = it },
                    displayMode = displayMode, onDisplayModeChange = { displayMode = it },
                    fractionMode = fractionMode, onFractionModeChange = { fractionMode = it },
                    approxMode = approxMode, onApproxModeChange = { approxMode = it },
                    onModeChange = onModeChange
                )
                DisplayPane(modifier = Modifier.fillMaxWidth().weight(1f))
            }
            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
                    .windowInsetsPadding(bottomInsets)
            ) {
                ExternalKeyboardRow(
                    keyboardEnabled = keyboardEnabled,
                    onKeyboardEnabledChange = { keyboardEnabled = it },
                    onUndo = { undo() }, onRedo = { redo() },
                    canUndo = undoRedoState.canUndo, canRedo = undoRedoState.canRedo
                )
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    ScientificKeypadFull(
                        onKeyPress = ::handleKeyPress,
                        selectedPrefix = selectedPrefix, onPrefixSelected = { selectedPrefix = it },
                        selectedUnit = selectedUnit, onUnitSelected = { selectedUnit = it },
                        selectedComplex = selectedComplex, onComplexSelected = { selectedComplex = it },
                        isLandscape = true
                    )
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
            ScientificTopBar(
                onBack = onBack, onHistoryClick = onHistoryClick, onMenuItemClick = onMenuItemClick,
                angleMode = angleMode, onAngleModeChange = { angleMode = it },
                displayMode = displayMode, onDisplayModeChange = { displayMode = it },
                fractionMode = fractionMode, onFractionModeChange = { fractionMode = it },
                approxMode = approxMode, onApproxModeChange = { approxMode = it },
                onModeChange = onModeChange
            )
            DisplayPane(modifier = Modifier.fillMaxWidth().weight(1f))
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(dividerColor))
            ExternalKeyboardRow(
                keyboardEnabled = keyboardEnabled,
                onKeyboardEnabledChange = { keyboardEnabled = it },
                onUndo = { undo() }, onRedo = { redo() },
                canUndo = undoRedoState.canUndo, canRedo = undoRedoState.canRedo
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(bottomInsets)
            ) {
                ScientificKeypadFull(
                    onKeyPress = ::handleKeyPress,
                    selectedPrefix = selectedPrefix, onPrefixSelected = { selectedPrefix = it },
                    selectedUnit = selectedUnit, onUnitSelected = { selectedUnit = it },
                    selectedComplex = selectedComplex, onComplexSelected = { selectedComplex = it },
                    isLandscape = false
                )
            }
        }
    }
}

@Composable
fun ScientificKeypadFull(
    onKeyPress: (String) -> Unit,
    selectedPrefix: String,
    onPrefixSelected: (String) -> Unit,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    selectedComplex: String,
    onComplexSelected: (String) -> Unit,
    isLandscape: Boolean = false
) {
    val colors = LocalCalculatorColors.current
    val buttonHeight = if (isLandscape) 42.dp else 54.dp
    val rows = listOf(
        listOf(
            KeyDef("x", KeyStyle.SCIENTIFIC, secondaryLabel = "→x"),
            KeyDef("y", KeyStyle.SCIENTIFIC, secondaryLabel = "→y"),
            KeyDef("z", KeyStyle.SCIENTIFIC, secondaryLabel = "→z"),
            KeyDef(selectedPrefix, KeyStyle.SCIENTIFIC, isDropdown = true, dropdownItems = listOf("giga", "mega", "kilo", "milli", "micro", "nano", "pico")),
            KeyDef(selectedUnit, KeyStyle.SCIENTIFIC, isDropdown = true, dropdownItems = listOf("ampere", "gram", "joule", "kelvin", "liter", "meter", "newton", "ohm", "pascal", "second", "volt", "watt"))
        ),
        listOf(
            KeyDef("∫", KeyStyle.SCIENTIFIC), KeyDef("dx", KeyStyle.SCIENTIFIC),
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
            KeyDef("7", KeyStyle.NUMBER), KeyDef("8", KeyStyle.NUMBER), KeyDef("9", KeyStyle.NUMBER),
            KeyDef("⌫", KeyStyle.ACCENT), KeyDef("AC", KeyStyle.ACCENT)
        ),
        listOf(
            KeyDef("√", KeyStyle.SCIENTIFIC), KeyDef("xʸ", KeyStyle.SCIENTIFIC),
            KeyDef("4", KeyStyle.NUMBER), KeyDef("5", KeyStyle.NUMBER), KeyDef("6", KeyStyle.NUMBER),
            KeyDef("·", KeyStyle.FUNCTION), KeyDef("/", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef("(", KeyStyle.FUNCTION, secondaryLabel = "["),
            KeyDef(")", KeyStyle.FUNCTION, secondaryLabel = "]"),
            KeyDef("1", KeyStyle.NUMBER), KeyDef("2", KeyStyle.NUMBER), KeyDef("3", KeyStyle.NUMBER),
            KeyDef("+", KeyStyle.FUNCTION), KeyDef("-", KeyStyle.FUNCTION)
        ),
        listOf(
            KeyDef(";", KeyStyle.FUNCTION, secondaryLabel = "_"),
            KeyDef("=", KeyStyle.FUNCTION, secondaryLabel = ","),
            KeyDef("0", KeyStyle.NUMBER), KeyDef(".", KeyStyle.NUMBER),
            KeyDef("E", KeyStyle.SCIENTIFIC), KeyDef("Enter", KeyStyle.ACCENT, span = 2)
        )
    )
    val columnModifier = if (isLandscape) {
        Modifier.fillMaxSize().background(colors.background).padding(4.dp)
    } else {
        Modifier.fillMaxWidth().background(colors.background).padding(4.dp).verticalScroll(rememberScrollState())
    }
    Column(modifier = columnModifier) {
        rows.forEach { row ->
            val rowModifier = if (isLandscape) {
                Modifier.fillMaxWidth().weight(1f).padding(vertical = 2.dp)
            } else {
                Modifier.fillMaxWidth().height(buttonHeight)
            }
            Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { keyDef ->
                    val buttonWeight = if (keyDef.span > 1) 2.1f else 1f
                    if (keyDef.isDropdown) {
                        DropdownKeyButton(
                            currentLabel = keyDef.label,
                            items = keyDef.dropdownItems ?: emptyList(),
                            onItemSelected = { newLabel ->
                                when (keyDef.label) {
                                    selectedPrefix -> onPrefixSelected(newLabel)
                                    selectedUnit -> onUnitSelected(newLabel)
                                    selectedComplex -> onComplexSelected(newLabel)
                                }
                            },
                            onTap = { onKeyPress(keyDef.label) },
                            modifier = Modifier.fillMaxHeight().weight(buttonWeight).padding(1.dp)
                        )
                    } else {
                        DualFunctionButton(
                            keyDef = keyDef,
                            onTap = { onKeyPress(keyDef.label) },
                            onLongPress = { keyDef.secondaryLabel?.let { onKeyPress(it) } ?: onKeyPress(keyDef.label) },
                            modifier = Modifier.fillMaxHeight().weight(buttonWeight).padding(1.dp)
                        )
                    }
                }
            }
        }
    }
}

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
            .combinedClickable(onClick = onTap, onLongClick = onLongPress),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = keyDef.label,
            color = colors.textPrimary,
            fontSize = if (keyDef.label.length > 3) 14.sp else 18.sp,
            fontWeight = if (keyDef.style == KeyStyle.ACCENT) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        keyDef.secondaryLabel?.let {
            Text(
                text = it,
                color = colors.textTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = (-4).dp)
                    .padding(end = 4.dp, top = 2.dp)
            )
        }
    }
}

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
            .combinedClickable(onClick = { onTap(currentLabel) }, onLongClick = { expanded = true }),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(currentLabel, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(max = 200.dp)
                .heightIn(max = 300.dp)
                .background(colors.background, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
            offset = DpOffset(0.dp, 4.dp),
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            containerColor = colors.background,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = item == currentLabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) colors.functionButton else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { onItemSelected(item); expanded = false }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=411")
@Preview(showBackground = true, device = "spec:width=800dp,height=360dp,dpi=411", name = "Landscape")
@Preview(showBackground = true, device = "spec:width=600dp,height=960dp,dpi=240", name = "Tablet Portrait")
@Composable
fun CalculatorPreview() {
    val darkTheme = isSystemInDarkTheme()
    UniCalculatorTheme(darkTheme = darkTheme, pureBlack = false) {
        UniCalculatorApp(modifier = Modifier, onThemeChanged = {}, onPureBlackChanged = {})
    }
}