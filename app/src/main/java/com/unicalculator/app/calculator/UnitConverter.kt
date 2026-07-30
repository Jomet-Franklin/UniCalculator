package com.unicalculator.app.calculator

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

import kotlin.math.PI

object UnitConverter {

    private val categories = mutableMapOf<String, List<Pair<String, Double>>>()

    init {
        // ===== LENGTH ====
        categories["length"] = listOf(
            "Nanometer" to 1e-9,
            "Micrometer" to 1e-6,
            "Millimeter" to 0.001,
            "Centimeter" to 0.01,
            "Decimeter" to 0.1,
            "Meter" to 1.0,
            "Kilometer" to 1000.0,
            "Mile" to 1609.344,
            "Yard" to 0.9144,
            "Foot" to 0.3048,
            "Inch" to 0.0254,
            "Nautical Mile" to 1852.0
        )

        // ==== AREA ====
        categories["area"] = listOf(
            "Square Millimeter" to 1e-6,
            "Square Centimeter" to 1e-4,
            "Square Decimeter" to 0.01,
            "Square Meter" to 1.0,
            "Square Kilometer" to 1e6,
            "Hectare" to 10000.0,
            "Acre" to 4046.8564224,
            "Square Yard" to 0.83612736,
            "Square Foot" to 0.09290304,
            "Square Inch" to 0.00064516
        )

        // ==== VOLUME ====
        categories["volume"] = listOf(
            "Cubic Millimeter" to 1e-6,
            "Cubic Centimeter" to 0.001,
            "Cubic Decimeter" to 1.0,
            "Cubic Meter" to 1000.0,
            "Liter" to 1.0,
            "Milliliter" to 0.001,
            "Kiloliter" to 1000.0,
            "Gallon" to 3.785411784,
            "Quart" to 0.946352946,
            "Pint" to 0.473176473,
            "Cup" to 0.2365882365,
            "Fluid Ounce" to 0.02957352957,
            "Tablespoon" to 0.01478676479,
            "Teaspoon" to 0.00492892159
        )

        // ==== MASS ====
        categories["mass"] = listOf(
            "Milligram" to 1e-6,
            "Gram" to 0.001,
            "Kilogram" to 1.0,
            "Tonne" to 1000.0,
            "Pound" to 0.45359237,
            "Ounce" to 0.028349523125,
            "Stone" to 6.35029318
        )

        // ==== TEMPERATURE ====
        categories["temperature"] = listOf(
            "Celsius" to 1.0,
            "Fahrenheit" to 1.0,
            "Kelvin" to 1.0
        )

        // ==== STORAGE ====
        categories["storage"] = listOf(
            "Bit" to 1.0,
            "Byte" to 8.0,
            "Kilobit" to 1000.0,
            "Kilobyte" to 8000.0,
            "Kibibit" to 1024.0,
            "Kibibyte" to 8192.0,
            "Megabit" to 1_000_000.0,
            "Megabyte" to 8_000_000.0,
            "Mebibit" to 1_048_576.0,
            "Mebibyte" to 8_388_608.0,
            "Gigabit" to 1_000_000_000.0,
            "Gigabyte" to 8_000_000_000.0,
            "Gibibit" to 1_073_741_824.0,
            "Gibibyte" to 8_589_934_592.0,
            "Terabit" to 1_000_000_000_000.0,
            "Terabyte" to 8_000_000_000_000.0,
            "Tebibit" to 1_099_511_627_776.0,
            "Tebibyte" to 8_796_093_022_208.0,
            "Petabit" to 1_000_000_000_000_000.0,
            "Petabyte" to 8_000_000_000_000_000.0,
            "Pebibit" to 1_125_899_906_842_624.0,
            "Pebibyte" to 9_007_199_254_740_992.0
        )

        // ==== PRESSURE ====
        categories["pressure"] = listOf(
            "Pascal" to 1.0,
            "Kilopascal" to 1000.0,
            "Megapascal" to 1e6,
            "Bar" to 100_000.0,
            "Millibar" to 100.0,
            "Atmosphere" to 101325.0,
            "PSI" to 6894.757293168,
            "mmHg" to 133.322368421
        )

        // ==== HEAT / ENERGY ====
        categories["heat"] = listOf(
            "Joule" to 1.0,
            "Kilojoule" to 1000.0,
            "Megajoule" to 1e6,
            "Calorie" to 4.184,
            "Kilocalorie" to 4184.0,
            "Watt-hour" to 3600.0,
            "Kilowatt-hour" to 3_600_000.0
        )

        // ==== SPEED ====
        categories["speed"] = listOf(
            "Meter per second" to 1.0,
            "Kilometer per hour" to 0.2777777778,
            "Kilometer per second" to 1000.0,
            "Mile per hour" to 0.44704,
            "Knot" to 0.5144444444,
            "Mach" to 340.29,
            "Speed of light" to 299792458.0
        )

        // ==== TIME ====
        categories["time"] = listOf(
            "Millisecond" to 0.001,
            "Second" to 1.0,
            "Minute" to 60.0,
            "Hour" to 3600.0,
            "Day" to 86400.0,
            "Week" to 604800.0,
            "Month" to 2629800.0,
            "Year" to 31557600.0
        )

        // ==== ANGLE ====
        categories["angle"] = listOf(
            "Degree" to 1.0,
            "Arcminute" to 1.0 / 60.0,
            "Arcsecond" to 1.0 / 3600.0,
            "Radian" to 180.0 / PI,
            "Gradian" to 0.9,
            "Turn" to 360.0
        )

        // ==== POWER ====
        categories["power"] = listOf(
            "Watt" to 1.0,
            "Kilowatt" to 1000.0,
            "Megawatt" to 1e6,
            "Horsepower" to 745.699871582,
            "Metric horsepower" to 735.49875,
            "Kilocalorie per second" to 4184.0,
            "Newton-meter per second" to 1.0,
            "Kilogram-meter per second" to 9.80665,
            "BTU per second" to 1055.05585262,
            "Foot-pound per second" to 1.35581794833
        )

        // ==== FORCE ====
        categories["force"] = listOf(
            "Newton" to 1.0,
            "Kilonewton" to 1000.0,
            "Dyne" to 1e-5,
            "Pound-force" to 4.44822161526,
            "Ounce-force" to 0.278013850953
        )

        // ==== DENSITY ====
        categories["density"] = listOf(
            "Kilogram per cubic meter" to 1.0,
            "Gram per cubic centimeter" to 1000.0,
            "Pound per cubic foot" to 16.01846337396,
            "Pound per gallon" to 119.8264268105
        )

        // ==== FREQUENCY ====
        categories["frequency"] = listOf(
            "Hertz" to 1.0,
            "Kilohertz" to 1000.0,
            "Megahertz" to 1_000_000.0,
            "Gigahertz" to 1_000_000_000.0
        )

        // ==== TORQUE ====
        categories["torque"] = listOf(
            "Newton meter" to 1.0,
            "Kilonewton meter" to 1000.0,
            "Pound-foot" to 1.35581794833,
            "Ounce-inch" to 0.00706155183333
        )

        // ==== VISCOSITY ====
        categories["viscosity"] = listOf(
            "Pascal-second" to 1.0,
            "Centipoise" to 0.001,
            "Poise" to 0.1,
            "Poiseuille" to 1.0
        )

        // ==== FUEL ====
        categories["fuel"] = listOf(
            "Liter" to 1.0,
            "Gallon (US)" to 3.785411784,
            "Gallon (UK)" to 4.54609,
            "Barrel" to 158.987294928,
            "Cubic meter" to 1000.0
        )

        // ==== DATE ====
        categories["date"] = listOf(
            "Days" to 1.0,
            "Weeks" to 7.0,
            "Months" to 30.436875,
            "Years" to 365.2425
        )

    }

    fun getUnitsForCategory(category: String): List<String> {
        return categories[category]?.map { it.first } ?: emptyList()
    }

    fun convert(value: Double, fromUnit: String, toUnit: String, category: String? = null): Double? {
        if (category == "temperature") {
            return convertTemperature(value, fromUnit, toUnit)
        }

        val cat = category ?: findCategoryForUnit(fromUnit) ?: return null
        val units = categories[cat] ?: return null

        val fromFactor = units.find { it.first == fromUnit }?.second ?: return null
        val toFactor = units.find { it.first == toUnit }?.second ?: return null

        return (value * fromFactor) / toFactor
    }

    // ==== Temperature Conversion ====
    private fun convertTemperature(value: Double, from: String, to: String): Double? {
        val celsius = when (from) {
            "Celsius" -> value
            "Fahrenheit" -> (value - 32) * 5 / 9
            "Kelvin" -> value - 273.15
            else -> return null
        }
        return when (to) {
            "Celsius" -> celsius
            "Fahrenheit" -> celsius * 9 / 5 + 32
            "Kelvin" -> celsius + 273.15
            else -> null
        }
    }

    // ==== Helper ====
    private fun findCategoryForUnit(unit: String): String? {
        for ((category, units) in categories) {
            if (units.any { it.first == unit }) {
                return category
            }
        }
        return null
    }
}