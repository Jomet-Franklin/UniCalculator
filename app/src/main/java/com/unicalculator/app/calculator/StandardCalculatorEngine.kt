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

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.util.Locale
import kotlin.math.*

object StandardCalculatorEngine {

    fun evaluateExpression(expr: String): String {
        return try {
            val sanitized = sanitize(expr)
            val result = evaluate(sanitized)
            formatResult(result)
        } catch (_: Exception) {
            "Error"
        }
    }

    fun evaluateExpressionOrBlank(expr: String): String {
        return try {
            val sanitized = sanitize(expr)
            val result = evaluate(sanitized)
            val formatted = formatResult(result)
            if (formatted == expr || !result.isFinite()) {
                ""
            } else {
                formatted
            }
        } catch (_: Exception) {
            ""
        }
    }
    fun handleKeyPress(current: String, label: String): String {
        return when (label) {
            "AC" -> "0"
            "⌫" -> if (current.length > 1) current.dropLast(1) else "0"
            "π" -> if (current == "0") "π" else "${current}π"
            "e" -> if (current == "0") "e" else "${current}e"
            "√" -> if (current == "0") "√(" else "${current}√("
            "sin", "cos", "tan", "ln", "log", "exp" -> {
                if (current == "0") "$label(" else "$current$label("
            }
            "sin⁻¹" -> if (current == "0") "asin(" else "${current}asin("
            "cos⁻¹" -> if (current == "0") "acos(" else "${current}acos("
            "tan⁻¹" -> if (current == "0") "atan(" else "${current}atan("

            "xⁿ" -> "${current}^"
            "!" -> "${current}!"
            "%" -> "${current}%"
            "±" -> if (current.startsWith('-')) current.drop(1) else "-$current"
            "|x|" -> "abs("

            "(" -> "$current("
            ")" -> "$current)"
            "×" -> "$current×"
            "÷" -> "$current÷"
            "·" -> "$current*"
            "/" -> "$current/"
            "+" -> "$current+"
            "-" -> "$current-"
            "." -> if (current == "0") "0." else "$current."

            else -> {
                if (current == "0" && label.firstOrNull()?.isDigit() == true) label
                else "$current$label"
            }
        }
    }

    private fun sanitize(expr: String): String {
        var sanitized = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("·", "*")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("π", "pi")
            .replace("∞", "Infinity")
            .replace("√", "sqrt")
            .replace(Regex("""(\d+)!"""), "factorial($1)")
            .replace(Regex("""(\d+)%"""), "($1/100)")

        sanitized = sanitized.replace(Regex("(\\d)([a-zA-Z(])"), "$1*$2")
        sanitized = sanitized.replace(Regex("""\)([a-zA-Z(])"""), ")*$1")
        sanitized = autoCloseParentheses(sanitized)

        return sanitized
    }
    private fun autoCloseParentheses(expr: String): String {
        var open = 0
        for (ch in expr) {
            if (ch == '(') open++
            else if (ch == ')') open--
        }
        return if (open > 0) expr + ")".repeat(open) else expr
    }

    private fun evaluate(expr: String): Double {

        val sinDeg = object : Function("sin", 1) {
            override fun apply(args: DoubleArray): Double = sin(Math.toRadians(args[0]))
        }
        val cosDeg = object : Function("cos", 1) {
            override fun apply(args: DoubleArray): Double = cos(Math.toRadians(args[0]))
        }
        val tanDeg = object : Function("tan", 1) {
            override fun apply(args: DoubleArray): Double = tan(Math.toRadians(args[0]))
        }
        val asinDeg = object : Function("asin", 1) {
            override fun apply(args: DoubleArray): Double = Math.toDegrees(asin(args[0]))
        }
        val acosDeg = object : Function("acos", 1) {
            override fun apply(args: DoubleArray): Double = Math.toDegrees(acos(args[0]))
        }
        val atanDeg = object : Function("atan", 1) {
            override fun apply(args: DoubleArray): Double = Math.toDegrees(atan(args[0]))
        }
        val factorial = object : Function("factorial", 1) {
            override fun apply(args: DoubleArray): Double {
                val n = args[0].toInt()
                if (n < 0) throw IllegalArgumentException("Factorial of negative number")
                var result = 1.0
                for (i in 2..n) result *= i
                return result
            }
        }
        val lnFunc = object : Function("ln", 1) {
            override fun apply(args: DoubleArray): Double = ln(args[0])
        }
        val expFunc = object : Function("exp", 1) {
            override fun apply(args: DoubleArray): Double = exp(args[0])
        }
        val log10Func = object : Function("log", 1) {
            override fun apply(args: DoubleArray): Double = log10(args[0])
        }

        val expression = ExpressionBuilder(expr)
            .function(sinDeg)
            .function(cosDeg)
            .function(tanDeg)
            .function(asinDeg)
            .function(acosDeg)
            .function(atanDeg)
            .function(factorial)
            .function(lnFunc)
            .function(expFunc)
            .function(log10Func)
            .build()

        return expression.evaluate()
    }

    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            val str = value.toString()
            if (str.length > 15) {
                String.format(Locale.US, "%.10f", value).trimEnd('0').trimEnd('.')
            } else {
                str
            }
        }
    }
}