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

    data class CalcResult(
        val display: String,
        val plain: String,
        val expression: String,
        val isError: Boolean
    )

    fun evaluate(expr: String): CalcResult {
        if (expr.isBlank()) {
            return CalcResult(display = "", plain = "", expression = expr, isError = false)
        }

        return try {
            val calc = calculator

            val parseOptions = ParseOptions().apply {
                preserve_format = false
                angle_unit = angleUnit
                limit_implicit_multiplication = false
            }

            val evalOptions = EvaluationOptions().apply {
                sync_units = true
                approximation = when (approxMode) {
                    "EXACT" -> ApproximationMode.APPROXIMATION_EXACT
                    "TRY EXACT" -> ApproximationMode.APPROXIMATION_TRY_EXACT
                    "APPROX" -> ApproximationMode.APPROXIMATION_APPROXIMATE
                    else -> ApproximationMode.APPROXIMATION_TRY_EXACT
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
                    "SCI" -> 3
                    "ENG" -> -3
                    else -> -3
                }

                number_fraction_format = when (fractionMode) {
                    "DEC" -> NumberFractionFormat.FRACTION_DECIMAL
                    "DEC EXACT" -> NumberFractionFormat.FRACTION_DECIMAL_EXACT
                    "FRACTIONAL" -> NumberFractionFormat.FRACTION_FRACTIONAL
                    "COMBINED" -> NumberFractionFormat.FRACTION_COMBINED
                    "PERCENT" -> NumberFractionFormat.FRACTION_PERCENT
                    "PERMILLE" -> NumberFractionFormat.FRACTION_PERMILLE
                    "PERMYRIAD" -> NumberFractionFormat.FRACTION_PERMYRIAD
                    else -> NumberFractionFormat.FRACTION_DECIMAL
                }

                interval_display = IntervalDisplay.INTERVAL_DISPLAY_CONCISE
                multiplication_sign = MultiplicationSign.MULTIPLICATION_SIGN_DOT
                division_sign = DivisionSign.DIVISION_SIGN_DIVISION
            }

            val unlocalized = calc.unlocalizeExpression(expr, parseOptions)
            val plainResult = calc.calculateAndPrint(
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

            val displayResult = toDisplayFormat(plainResult)

            CalcResult(
                display = displayResult,
                plain = stripHtml(plainResult),
                expression = expr,
                isError = false
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Error evaluating '$expr'", e)
            CalcResult(display = "Error", plain = "", expression = expr, isError = true)
        }
    }

    fun evaluateExpression(expr: String): String = evaluate(expr).display

    fun evaluateExpressionOrBlank(expr: String): String {
        val result = evaluate(expr)
        return if (result.isError) "" else result.display
    }

    fun parseExpression(expr: String): String {
        return try {
            toSuperscriptExponents(expr)
        } catch (_: Throwable) {
            ""
        }
    }

    private fun stripHtml(text: String): String = text.replace(Regex("<[^>]*>"), "")

    private fun toDisplayFormat(text: String): String {
        return toSuperscriptExponents(stripHtml(text))
    }

    private val superscriptMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '-' to '⁻', '−' to '⁻'
    )

    private fun digitsToSuperscript(digits: String, negative: Boolean): String {
        val body = digits.map { superscriptMap[it] ?: it }.joinToString("")
        return if (negative) "⁻$body" else body
    }

    private fun toSuperscriptExponents(input: String): String {
        val grouped = Regex("""\^\(([−-]?)(\d+)\)""")
        var result = grouped.replace(input) { m ->
            digitsToSuperscript(m.groupValues[2], m.groupValues[1].isNotEmpty())
        }

        val bare = Regex("""\^([−-]?)(\d+)""")
        result = bare.replace(result) { m ->
            digitsToSuperscript(m.groupValues[2], m.groupValues[1].isNotEmpty())
        }

        return result
    }
}
