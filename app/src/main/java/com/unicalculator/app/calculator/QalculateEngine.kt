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

import android.util.Log
import com.jherkenhoff.libqalculate.*

object QalculateEngine {
    private const val TAG = "QalculateEngine"

    private val calculator: Calculator by lazy {
        try {
            System.loadLibrary("qalculate_swig")
            val calc = Calculator()
            calc.loadGlobalDefinitions()
            Log.i(TAG, "Qalculate! engine ready")
            calc
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize Qalculate!", e)
            throw e
        }
    }

    var angleUnit: AngleUnit = AngleUnit.ANGLE_UNIT_DEGREES
    var displayMode: String = "ENG"
    var fractionMode: String = "DEC"
    var approxMode: String = "TRY EXACT"

    fun evaluateExpression(expr: String): String {
        return try {
            val calc = calculator

            val parseOptions = ParseOptions().apply {
                preserve_format = false
                angle_unit = angleUnit
            }

            val evalOptions = EvaluationOptions().apply {
                sync_units = true
                approximation = when (approxMode) {
                    "EXACT"      -> ApproximationMode.APPROXIMATION_EXACT
                    "TRY EXACT"  -> ApproximationMode.APPROXIMATION_TRY_EXACT
                    "APPROX"     -> ApproximationMode.APPROXIMATION_APPROXIMATE
                    else         -> ApproximationMode.APPROXIMATION_TRY_EXACT
                }
                parse_options = parseOptions
                allow_complex = true
            }

            val printOptions = PrintOptions().apply {
                use_unicode_signs = 1
                decimalpoint_sign = "."
                digit_grouping = DigitGrouping.DIGIT_GROUPING_NONE
                negative_exponents = true
                exp_display = ExpDisplay.EXP_POWER_OF_10

                min_exp = when (displayMode) {
                    "NORM" -> -1
                    "SCI"  -> 3
                    "ENG"  -> -3
                    else   -> -3
                }

                number_fraction_format = when (fractionMode) {
                    "DEC"          -> NumberFractionFormat.FRACTION_DECIMAL
                    "DEC EXACT"    -> NumberFractionFormat.FRACTION_DECIMAL_EXACT
                    "FRACTIONAL"   -> NumberFractionFormat.FRACTION_FRACTIONAL
                    "COMBINED"     -> NumberFractionFormat.FRACTION_COMBINED
                    "PERCENT"      -> NumberFractionFormat.FRACTION_PERCENT
                    "PERMILLE"     -> NumberFractionFormat.FRACTION_PERMILLE
                    "PERMYRIAD"    -> NumberFractionFormat.FRACTION_PERMYRIAD
                    else           -> NumberFractionFormat.FRACTION_DECIMAL
                }

                interval_display = IntervalDisplay.INTERVAL_DISPLAY_CONCISE
                multiplication_sign = MultiplicationSign.MULTIPLICATION_SIGN_DOT
                division_sign = DivisionSign.DIVISION_SIGN_DIVISION
            }

            val unlocalized = calc.unlocalizeExpression(expr, parseOptions)
            val raw = calc.calculateAndPrint(
                unlocalized,
                2000,
                evalOptions,
                printOptions,
                AutomaticFractionFormat.AUTOMATIC_FRACTION_OFF,
                AutomaticApproximation.AUTOMATIC_APPROXIMATION_OFF,
                null,
                -1,
                null,
                false,
                0,
                0
            )
            formatOutput(raw)
        } catch (e: Throwable) {
            Log.e(TAG, "Error evaluating '$expr'", e)
            "Error"
        }
    }

    fun evaluateExpressionOrBlank(expr: String): String {
        return try {
            val result = evaluateExpression(expr)
            if (result == expr) "" else result
        } catch (_: Exception) {
            ""
        }
    }

    fun parseExpression(expr: String): String {
        return try {
            val calc = calculator
            val parseOptions = ParseOptions().apply {
                preserve_format = true
                angle_unit = angleUnit
            }
            val printOptions = PrintOptions().apply {
                use_unicode_signs = 1
                decimalpoint_sign = "."
                digit_grouping = DigitGrouping.DIGIT_GROUPING_NONE
                abbreviate_names = false
                negative_exponents = true
                exp_display = ExpDisplay.EXP_POWER_OF_10
                spacious = true
                place_units_separately = true
            }
            val unlocalized = calc.unlocalizeExpression(expr, parseOptions)
            val parsed = calc.parse(unlocalized, parseOptions)
            parsed ?: return ""
            val printed = calc.print(parsed, 2000, printOptions, true, 0, 0)
            formatOutput(printed)
        } catch (_: Throwable) {
            ""
        }
    }
    private fun formatOutput(text: String): String {
        var cleaned = text.replace(Regex("<[^>]*>"), "")

        val superscriptMap = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
            '-' to '⁻', '−' to '⁻'  // ASCII hyphen and Unicode minus
        )

        val regex = Regex("""\^\s*([−-])?\s*(\d+)\s*\)?""")
        cleaned = regex.replace(cleaned) { match ->
            val sign = match.groupValues[1]
            val digits = match.groupValues[2]
            val exponent = if (sign.isNotEmpty()) "⁻" + digits.map { superscriptMap[it] ?: it }.joinToString("")
            else digits.map { superscriptMap[it] ?: it }.joinToString("")
            exponent
        }

        cleaned = cleaned.replace(Regex("""\^(\d+)""")) { match ->
            val digits = match.groupValues[1]
            digits.map { superscriptMap[it] ?: it }.joinToString("")
        }

        cleaned = cleaned.replace(Regex("""\^\s*-"""), "⁻")

        return cleaned
    }
}