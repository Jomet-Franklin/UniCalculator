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

import kotlin.math.*

object StandardCalculatorEngine {

    // ====== PUBLIC API ======

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
            if (formatted == expr || !result.isFinite() || result.isNaN()) {
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
            "π" -> if (current == "0") "π" else current + "π"
            "e" -> if (current == "0") "e" else current + "e"
            "∞" -> if (current == "0") "∞" else current + "∞"
            "√" -> if (current == "0") "√(" else current + "√("
            "sin", "cos", "tan", "ln", "log", "exp" -> {
                if (current == "0") "$label(" else current + "$label("
            }
            "sin⁻¹" -> if (current == "0") "asin(" else current + "asin("
            "cos⁻¹" -> if (current == "0") "acos(" else current + "acos("
            "tan⁻¹" -> if (current == "0") "atan(" else current + "atan("

            "xⁿ" -> current + "^"
            "!" -> current + "!"
            "%" -> current + "%"
            "±" -> if (current.startsWith('-')) current.drop(1) else "-$current"

            "(" -> current + "("
            ")" -> current + ")"
            "[" -> current + "["
            "]" -> current + "]"
            "_" -> current + "_"
            ";" -> current + ";"
            "=" -> current + "="
            "," -> current + ","
            "E" -> current + "E"

            "·" -> current + "*"
            "/" -> current + "/"
            "+" -> current + "+"
            "-" -> current + "-"
            "." -> if (current == "0") "0." else current + "."

            else -> {
                if (current == "0" && label.firstOrNull()?.isDigit() == true) label
                else current + label
            }
        }
    }

    // ====== PRIVATE PARSER ======

    private fun sanitize(expr: String): String {
        var sanitized = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("·", "*")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("π", "pi")
            .replace("∞", "inf")
            .replace("√", "sqrt")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
        // "log" is handled by tokenizer – do NOT replace it here
        // "e" is handled by tokenizer – do NOT replace it here
        // "^" is kept as operator – do NOT replace it here

        sanitized = sanitized.replace(Regex("(\\d)([a-zA-Z])"), "$1*$2")
        sanitized = sanitized.replace(Regex("(\\d)(\\()"), "$1*$(")
        sanitized = sanitized.replace(Regex("""\)([a-zA-Z0-9(])"""), ")*$1")
        return sanitized
    }

    private fun evaluate(expr: String): Double {
        val tokens = tokenize(expr)
        return Parser(tokens).parse()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < expr.length) {
            val ch = expr[i]
            when {
                ch.isDigit() || ch == '.' -> {
                    var num = ""
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        num += expr[i]
                        i++
                    }
                    tokens.add(Token.Number(num.toDouble()))
                    continue
                }
                ch == 'p' && expr.startsWith("pi", i) -> {
                    tokens.add(Token.Number(PI))
                    i += 2
                    continue
                }
                ch == 'e' && (i + 1 == expr.length || !expr[i + 1].isLetterOrDigit()) -> {
                    tokens.add(Token.Number(E))
                    i++
                    continue
                }
                ch == 'i' && expr.startsWith("inf", i) -> {
                    tokens.add(Token.Number(Double.POSITIVE_INFINITY))
                    i += 3
                    continue
                }
                ch.isLetter() -> {
                    var name = ""
                    while (i < expr.length && expr[i].isLetter()) {
                        name += expr[i]
                        i++
                    }
                    val func = when (name) {
                        "sin", "cos", "tan", "asin", "acos", "atan",
                        "ln", "log", "log10", "sqrt", "exp" -> {
                            Token.Function(name)
                        }
                        else -> null
                    }
                    if (func != null) {
                        tokens.add(func)
                    }
                    continue
                }
                ch == '(' -> { tokens.add(Token.LParen); i++ }
                ch == ')' -> { tokens.add(Token.RParen); i++ }
                ch in "+-*/%" -> { tokens.add(Token.Op(ch)); i++ }
                ch == '^' -> { tokens.add(Token.Op('^')); i++ }
                ch == '!' -> { tokens.add(Token.PostfixOp('!')); i++ }
                ch == ' ' -> i++
                else -> i++
            }
        }
        return tokens
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Op(val char: Char) : Token()
        data class Function(val name: String) : Token()
        object LParen : Token()
        object RParen : Token()
        data class PostfixOp(val char: Char) : Token()
    }

    private class Parser(private val tokens: List<Token>) {
        private var pos = 0

        fun parse(): Double = parseExpression()

        private fun parseExpression(): Double = parseAddSub()

        private fun parseAddSub(): Double {
            var left = parseMulDiv()
            while (pos < tokens.size) {
                when (val token = tokens[pos]) {
                    is Token.Op -> {
                        when (token.char) {
                            '+' -> { pos++; left += parseMulDiv() }
                            '-' -> { pos++; left -= parseMulDiv() }
                            else -> break
                        }
                    }
                    else -> break
                }
            }
            return left
        }

        private fun parseMulDiv(): Double {
            var left = parseUnary()
            while (pos < tokens.size) {
                when (val token = tokens[pos]) {
                    is Token.Op -> {
                        when (token.char) {
                            '*' -> { pos++; left *= parseUnary() }
                            '/' -> { pos++; left /= parseUnary() }
                            '%' -> { pos++; left %= parseUnary() }
                            else -> break
                        }
                    }
                    else -> break
                }
            }
            return left
        }

        private fun parseUnary(): Double {
            if (pos < tokens.size) {
                val token = tokens[pos]
                if (token is Token.Op && token.char == '-') {
                    pos++
                    return -parseUnary()
                }
            }
            return parsePow()
        }

        private fun parsePow(): Double {
            var left = parsePrimary()
            if (pos < tokens.size) {
                val token = tokens[pos]
                if (token is Token.Op && token.char == '^') {
                    pos++
                    val right = parseUnary()
                    return left.pow(right)
                }
            }
            return left
        }

        private fun parsePrimary(): Double {
            if (pos >= tokens.size) return 0.0
            val token = tokens[pos]
            return when (token) {
                is Token.Number -> { pos++; applyPostfix(token.value) }
                is Token.LParen -> {
                    pos++
                    val result = parseExpression()
                    if (pos < tokens.size && tokens[pos] is Token.RParen) pos++
                    applyPostfix(result)
                }
                is Token.Function -> {
                    pos++
                    if (pos < tokens.size && tokens[pos] is Token.LParen) {
                        pos++
                        val arg = parseExpression()
                        if (pos < tokens.size && tokens[pos] is Token.RParen) pos++
                        val result = evaluateFunction(token.name, arg)
                        applyPostfix(result)
                    } else {
                        0.0
                    }
                }
                else -> {
                    pos++
                    0.0
                }
            }
        }

        private fun applyPostfix(value: Double): Double {
            if (pos < tokens.size && tokens[pos] is Token.PostfixOp) {
                val op = tokens[pos] as Token.PostfixOp
                if (op.char == '!') {
                    pos++
                    return factorial(value.toInt()).toDouble()
                }
            }
            return value
        }

        private fun evaluateFunction(name: String, arg: Double): Double {
            return when (name) {
                "sin" -> sin(Math.toRadians(arg))
                "cos" -> cos(Math.toRadians(arg))
                "tan" -> tan(Math.toRadians(arg))
                "asin" -> Math.toDegrees(asin(arg))
                "acos" -> Math.toDegrees(acos(arg))
                "atan" -> Math.toDegrees(atan(arg))
                "ln" -> ln(arg)
                "log" -> log10(arg)
                "log10" -> log10(arg)
                "sqrt" -> sqrt(arg)
                "exp" -> exp(arg)
                else -> 0.0
            }
        }
    }

    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            val str = value.toString()
            if (str.length > 15) String.format("%.10f", value).trimEnd('0').trimEnd('.')
            else str
        }
    }

    private fun factorial(n: Int): Long {
        if (n < 0) return 0
        var result = 1L
        for (i in 2..n) result *= i
        return result
    }
}