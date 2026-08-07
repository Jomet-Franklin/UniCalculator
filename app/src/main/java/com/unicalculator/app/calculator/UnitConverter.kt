package com.unicalculator.app.calculator

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

import kotlin.math.log10
import kotlin.math.pow

object UnitConverter {

    private val categories =
        mutableMapOf<String, List<Pair<String, Double>>>()

    private val categoryDefaults =
        mutableMapOf<String, Pair<String, String>>()

    init {
        categories["Length"] = listOf(
            "Nanometer" to 1e-9,
            "Micrometer" to 1e-6,
            "Millimeter" to 1e-3,
            "Centimeter" to 1e-2,
            "Decimeter" to 1e-1,
            "Meter" to 1.0,
            "Foot" to 0.3048,
            "Yard" to 0.9144,
            "Fathom" to 1.8288,
            "Furlong" to 201.168,
            "Kilometer" to 1e3,
            "Mile" to 1609.344,
            "Nautical Mile" to 1852.0,
            "Astronomical Unit" to 1.495978707e11,
            "Light-year" to 9.4607304725808e15,
            "Parsec" to 3.0856775814913673e16,
            "Inch" to 0.0254
        )

        categoryDefaults["Length"] =
            "Meter" to "Foot"

        categories["Area"] = listOf(
            "Square Millimeter" to 1e-6,
            "Square Centimeter" to 1e-4,
            "Square Decimeter" to 1e-2,
            "Are" to 100.0,
            "Square Meter" to 1.0,
            "Square Foot" to 0.09290304,
            "Square Yard" to 0.83612736,
            "Hectare" to 10_000.0,
            "Acre" to 4046.8564224,
            "Square Kilometer" to 1e6,
            "Square Mile" to 2589988.110336,
            "Square Inch" to 0.00064516
        )

        categoryDefaults["Area"] =
            "Square Meter" to "Square Foot"

        categories["Volume"] = listOf(
            "Cubic Millimeter" to 1e-6,
            "Cubic Centimeter" to 0.001,
            "Milliliter" to 0.001,
            "Cubic Inch" to 0.016387064,
            "Cubic Decimeter" to 1.0,
            "Liter" to 1.0,
            "Teaspoon (US)" to 0.00492892159375,
            "Tablespoon (US)" to 0.01478676478125,
            "Fluid Ounce (US)" to 0.0295735295625,
            "Fluid Ounce (UK)" to 0.0284130625,
            "Cup (US)" to 0.2365882365,
            "Pint (US)" to 0.473176473,
            "Pint (UK)" to 0.56826125,
            "Quart (US)" to 0.946352946,
            "Quart (UK)" to 1.1365225,
            "Cubic Foot" to 28.316846592,
            "Gallon (US)" to 3.785411784,
            "Gallon (UK)" to 4.54609,
            "Cubic Yard" to 764.554857984,
            "Cubic Meter" to 1000.0,
            "Kiloliter" to 1000.0
        )

        categoryDefaults["Volume"] =
            "Liter" to "Gallon (US)"

        categories["Fuel"] = listOf(
            "Liter" to 1.0,
            "Gallon (US)" to 3.785411784,
            "Gallon (UK)" to 4.54609,
            "Barrel (oil, US)" to 158.987294928,
            "Cubic Meter" to 1000.0
        )

        categoryDefaults["Fuel"] =
            "Gallon (US)" to "Liter"

        categories["Mass"] = listOf(
            "Milligram" to 1e-6,
            "Gram" to 1e-3,
            "Carat" to 0.0002,
            "Grain" to 0.00006479891,
            "Kilogram" to 1.0,
            "Slug" to 14.59390294,
            "Pound" to 0.45359237,
            "Ounce" to 0.028349523125,
            "Stone" to 6.35029318,
            "Tonne (metric)" to 1000.0,
            "Short Ton (US)" to 907.18474,
            "Long Ton (UK)" to 1016.0469088
        )

        categoryDefaults["Mass"] =
            "Kilogram" to "Pound"

        categories["Temperature"] = listOf(
            "Celsius" to 1.0,
            "Fahrenheit" to 1.0,
            "Kelvin" to 1.0,
            "Rankine" to 1.0
        )

        categoryDefaults["Temperature"] =
            "Celsius" to "Fahrenheit"

        categories["Digital Storage"] = listOf(
            "Bit" to 1.0,
            "Byte" to 8.0,
            "Kilobit" to 1e3,
            "Kibibit" to 1024.0,
            "Kilobyte" to 8e3,
            "Kibibyte" to 8192.0,
            "Megabit" to 1e6,
            "Mebibit" to 1_048_576.0,
            "Megabyte" to 8e6,
            "Mebibyte" to 8_388_608.0,
            "Gigabit" to 1e9,
            "Gibibit" to 1_073_741_824.0,
            "Gigabyte" to 8e9,
            "Gibibyte" to 8_589_934_592.0,
            "Terabit" to 1e12,
            "Tebibit" to 1_099_511_627_776.0,
            "Terabyte" to 8e12,
            "Tebibyte" to 8_796_093_022_208.0,
            "Petabit" to 1e15,
            "Pebibit" to 1_125_899_906_842_624.0,
            "Petabyte" to 8e15,
            "Pebibyte" to 9_007_199_254_740_992.0,
            "Exabit" to 1e18,
            "Exbibit" to 1_152_921_504_606_846_976.0,
            "Exabyte" to 8e18,
            "Exbibyte" to 9_223_372_036_854_775_808.0
        )

        categoryDefaults["Digital Storage"] =
            "Gigabyte" to "Megabyte"

        categories["Data Transfer"] = listOf(
            "bps" to 1.0,
            "Kbps" to 1e3,
            "Kibit/s" to 1024.0,
            "Byte per second" to 8.0,
            "KB/s (decimal)" to 8e3,
            "KiB/s" to 8192.0,
            "Mbps" to 1e6,
            "Mibit/s" to 1_048_576.0,
            "MB/s (decimal)" to 8e6,
            "MiB/s" to 8_388_608.0,
            "Gbps" to 1e9,
            "Gibit/s" to 1_073_741_824.0,
            "GB/s (decimal)" to 8e9,
            "GiB/s" to 8_589_934_592.0,
            "Tbps" to 1e12,
            "TB/s (decimal)" to 8e12
        )

        categoryDefaults["Data Transfer"] =
            "Mbps" to "MB/s (decimal)"

        categories["Pressure"] = listOf(
            "Pascal" to 1.0,
            "Millibar" to 100.0,
            "Kilopascal" to 1000.0,
            "Kilogram-force per cm²" to 98066.5,
            "mmHg" to 133.322387415,
            "Torr" to 133.3223684211,
            "inHg" to 3386.389,
            "Bar" to 100000.0,
            "Atmosphere" to 101325.0,
            "PSI" to 6894.757293168,
            "Megapascal" to 1e6
        )

        categoryDefaults["Pressure"] =
            "PSI" to "Bar"

        categories["Energy"] = listOf(
            "Electronvolt" to 1.602176634e-19,
            "Erg" to 1e-7,
            "Joule" to 1.0,
            "Foot-pound (energy)" to 1.3558179483314004,
            "Calorie (thermochemical)" to 4.184,
            "Kilojoule" to 1000.0,
            "Kilocalorie" to 4184.0,
            "Watt-hour" to 3600.0,
            "BTU" to 1055.05585262,
            "Kilowatt-hour" to 3_600_000.0,
            "Megajoule" to 1e6
        )

        categoryDefaults["Energy"] =
            "Kilocalorie" to "Kilojoule"

        categories["Speed"] = listOf(
            "Meter per second" to 1.0,
            "Foot per second" to 0.3048,
            "Kilometer per hour" to 1.0 / 3.6,
            "Knot" to 0.5144444444444445,
            "Mile per hour" to 0.44704,
            "Kilometer per second" to 1000.0,
            "Speed of light" to 299792458.0
        )

        categoryDefaults["Speed"] =
            "Kilometer per hour" to "Mile per hour"

        categories["Time"] = listOf(
            "Millisecond" to 0.001,
            "Second" to 1.0,
            "Minute" to 60.0,
            "Hour" to 3600.0,
            "Day" to 86400.0
        )

        categoryDefaults["Time"] =
            "Hour" to "Minute"

        categories["Duration"] = listOf(
            "Day" to 1.0,
            "Week" to 7.0,
            "Fortnight" to 14.0,
            "Month (average)" to 30.436875,
            "Quarter (average)" to 91.310625,
            "Year (Julian)" to 365.25,
            "Decade" to 3652.5,
            "Century" to 36525.0
        )

        categoryDefaults["Duration"] =
            "Day" to "Week"

        categories["Angle"] = listOf(
            "Arcsecond" to 1.0 / 3600.0,
            "Arcminute" to 1.0 / 60.0,
            "Mil (NATO)" to 0.05625,
            "Degree" to 1.0,
            "Gradian" to 0.9,
            "Radian" to 57.29577951308232,
            "Turn" to 360.0
        )

        categoryDefaults["Angle"] =
            "Degree" to "Radian"

        categories["Power"] = listOf(
            "Milliwatt" to 0.001,
            "Watt" to 1.0,
            "Kilowatt" to 1000.0,
            "Megawatt" to 1e6,
            "Gigawatt" to 1e9,
            "Horsepower (mechanical)" to 745.699871582,
            "Horsepower (metric)" to 735.49875,
            "Foot-pound per second" to 1.35581794833,
            "Kilogram-force meter per second" to 9.80665,
            "BTU per second" to 1055.05585262,
            "Kilocalorie per second" to 4184.0
        )

        categoryDefaults["Power"] =
            "Kilowatt" to "Horsepower (mechanical)"

        categories["Force"] = listOf(
            "Dyne" to 1e-5,
            "Poundal" to 0.138254954376,
            "Ounce-force" to 0.278013850953,
            "Newton" to 1.0,
            "Pound-force" to 4.44822161526,
            "Kilogram-force" to 9.80665,
            "Kilonewton" to 1000.0
        )

        categoryDefaults["Force"] =
            "Newton" to "Pound-force"

        categories["Density"] = listOf(
            "Gram per liter" to 1.0,
            "Kilogram per cubic meter" to 1.0,
            "Gram per cubic centimeter" to 1000.0,
            "Pound per cubic foot" to 16.01846337396,
            "Pound per gallon (US)" to 119.82642731689663,
            "Pound per cubic inch" to 27679.90471
        )

        categoryDefaults["Density"] =
            "Kilogram per cubic meter" to
                    "Gram per cubic centimeter"

        categories["Frequency"] = listOf(
            "RPM (revolutions/min)" to (1.0 / 60.0),
            "Hertz" to 1.0,
            "Kilohertz" to 1000.0,
            "Megahertz" to 1e6,
            "Gigahertz" to 1e9,
            "Terahertz" to 1e12
        )

        categoryDefaults["Frequency"] =
            "Hertz" to "Kilohertz"

        categories["Torque"] = listOf(
            "Ounce-inch" to 0.007061551814226044,
            "Pound-inch" to 0.1129848290276167,
            "Newton meter" to 1.0,
            "Pound-foot" to 1.35581794833,
            "Kilogram-force meter" to 9.80665,
            "Kilonewton meter" to 1000.0
        )

        categoryDefaults["Torque"] =
            "Newton meter" to "Pound-foot"

        categories["Viscosity"] = listOf(
            "Centipoise" to 0.001,
            "Poise" to 0.1,
            "Pascal-second" to 1.0,
            "Poiseuille" to 1.0,
            "Reyn" to 6894.757293168
        )

        categoryDefaults["Viscosity"] =
            "Centipoise" to "Pascal-second"

        categories["Fuel Economy"] = listOf(
            "L/100km" to 1.0,
            "km/L" to 1.0,
            "MPG (US)" to 1.0,
            "MPG (UK)" to 1.0
        )

        categoryDefaults["Fuel Economy"] =
            "MPG (US)" to "L/100km"

        categories["Absorbed Dose"] = listOf(
            "Milligray" to 0.001,
            "Centigray" to 0.01,
            "Rad" to 0.01,
            "Gray" to 1.0
        )

        categoryDefaults["Absorbed Dose"] =
            "Gray" to "Rad"

        categories["Equivalent Dose"] = listOf(
            "Millisievert" to 0.001,
            "Rem" to 0.01,
            "Sievert" to 1.0
        )

        categoryDefaults["Equivalent Dose"] =
            "Millisievert" to "Rem"

        categories["Illuminance"] = listOf(
            "Lux" to 1.0,
            "Foot-candle" to 10.763910417
        )

        categoryDefaults["Illuminance"] =
            "Lux" to "Foot-candle"

        categories["Sound Level"] = listOf(
            "dB SPL" to 1.0,
            "Pascal" to 1.0
        )

        categoryDefaults["Sound Level"] =
            "dB SPL" to "Pascal"

        categories["Typography"] = listOf(
            "Twip" to 0.05,
            "Point" to 1.0,
            "Pica" to 12.0,
            "Pixel (@96 DPI)" to 0.75,
            "Inch" to 72.0
        )

        categoryDefaults["Typography"] =
            "Point" to "Pixel (@96 DPI)"

        categories["Blood Glucose"] = listOf(
            "mg/dL" to 1.0,
            "mmol/L" to 18.0182
        )

        categoryDefaults["Blood Glucose"] =
            "mg/dL" to "mmol/L"
    }

    fun getDefaultUnits(
        category: String
    ): Pair<String, String>? {
        return categoryDefaults[category]
    }

    fun getUnitsForCategory(
        category: String
    ): List<String> {
        return categories[category]
            ?.map { it.first }
            ?: emptyList()
    }

    fun convert(
        value: Double,
        fromUnit: String,
        toUnit: String,
        category: String? = null
    ): Double? {
        if (!value.isFinite()) return null

        val cat =
            category
                ?: findUniqueCategoryForUnit(fromUnit)
                ?: return null

        val units =
            categories[cat]
                ?: return null

        if (
            units.none {
                it.first == fromUnit
            }
        ) {
            return null
        }

        if (
            units.none {
                it.first == toUnit
            }
        ) {
            return null
        }

        return when (cat) {
            "Temperature" ->
                convertTemperature(
                    value,
                    fromUnit,
                    toUnit
                )

            "Fuel Economy" ->
                convertFuelEconomy(
                    value,
                    fromUnit,
                    toUnit
                )

            "Sound Level" ->
                convertSoundLevel(
                    value,
                    fromUnit,
                    toUnit
                )

            else ->
                convertLinear(
                    value,
                    fromUnit,
                    toUnit,
                    cat
                )
        }
    }

    private fun convertLinear(
        value: Double,
        fromUnit: String,
        toUnit: String,
        category: String
    ): Double? {
        val units =
            categories[category]
                ?: return null

        val fromFactor =
            units
                .find {
                    it.first == fromUnit
                }
                ?.second
                ?: return null

        val toFactor =
            units
                .find {
                    it.first == toUnit
                }
                ?.second
                ?: return null

        if (
            !fromFactor.isFinite() ||
            !toFactor.isFinite() ||
            toFactor == 0.0
        ) {
            return null
        }

        val result =
            (value * fromFactor) / toFactor

        return if (result.isFinite()) {
            result
        } else {
            null
        }
    }

    private fun convertTemperature(
        value: Double,
        from: String,
        to: String
    ): Double? {
        if (!value.isFinite()) return null

        val celsius =
            when (from) {
                "Celsius" ->
                    value

                "Fahrenheit" ->
                    (value - 32.0) *
                            5.0 / 9.0

                "Kelvin" ->
                    value - 273.15

                "Rankine" ->
                    (value - 491.67) *
                            5.0 / 9.0

                else ->
                    return null
            }

        if (celsius < -273.15) {
            return null
        }

        return when (to) {
            "Celsius" ->
                celsius

            "Fahrenheit" ->
                celsius *
                        9.0 / 5.0 +
                        32.0

            "Kelvin" ->
                celsius + 273.15

            "Rankine" ->
                (celsius + 273.15) *
                        9.0 / 5.0

            else ->
                null
        }
    }

    private fun convertFuelEconomy(
        value: Double,
        from: String,
        to: String
    ): Double? {
        if (
            !value.isFinite() ||
            value <= 0.0
        ) {
            return null
        }

        val lPer100Km =
            when (from) {
                "L/100km" ->
                    value

                "km/L" ->
                    100.0 / value

                "MPG (US)" ->
                    235.214583 / value

                "MPG (UK)" ->
                    282.480936 / value

                else ->
                    return null
            }

        if (
            !lPer100Km.isFinite() ||
            lPer100Km <= 0.0
        ) {
            return null
        }

        return when (to) {
            "L/100km" ->
                lPer100Km

            "km/L" ->
                100.0 / lPer100Km

            "MPG (US)" ->
                235.214583 / lPer100Km

            "MPG (UK)" ->
                282.480936 / lPer100Km

            else ->
                null
        }
    }

    private fun convertSoundLevel(
        value: Double,
        from: String,
        to: String
    ): Double? {
        if (!value.isFinite()) {
            return null
        }

        val p0 = 20e-6

        val pascals =
            when (from) {
                "Pascal" -> {
                    if (value <= 0.0) {
                        return null
                    }

                    value
                }

                "dB SPL" ->
                    p0 *
                            10.0.pow(
                                value / 20.0
                            )

                else ->
                    return null
            }

        if (
            !pascals.isFinite() ||
            pascals <= 0.0
        ) {
            return null
        }

        return when (to) {
            "Pascal" ->
                pascals

            "dB SPL" ->
                20.0 *
                        log10(
                            pascals / p0
                        )

            else ->
                null
        }
    }

    private fun findUniqueCategoryForUnit(
        unit: String
    ): String? {
        val matches =
            categories
                .filter { (_, units) ->
                    units.any {
                        it.first == unit
                    }
                }
                .keys

        return if (matches.size == 1) {
            matches.first()
        } else {
            null
        }
    }
}