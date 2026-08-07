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

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class HistoryItem(
    val expression: String,
    val result: String,
    val timestamp: Long,
    val mode: String
)

object HistoryManager {
    private const val PREF_NAME = "history_prefs"
    private const val KEY_HISTORY = "history_list"
    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        getHistory()
    }

    fun addEntry(expression: String, result: String, mode: String) {
        try {
            val list = getHistory().toMutableList()
            val item = HistoryItem(expression, result, System.currentTimeMillis(), mode)
            val last = list.firstOrNull()
            if (last != null && last.expression == expression && last.result == result && last.mode == mode) {
                list.removeAt(0)
            }
            list.add(0, item)
            saveHistory(list)
        } catch (_: Exception) {

        }
    }

    fun getHistory(): List<HistoryItem> {
        return try {
            val prefs = prefs ?: return emptyList()
            val json = prefs.getString(KEY_HISTORY, null)
            if (json.isNullOrEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<HistoryItem>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            }
        } catch (_: Exception) {
            saveHistory(emptyList())
            emptyList()
        }
    }

    fun deleteItem(timestamp: Long) {
        val list = getHistory().toMutableList()
        list.removeAll { it.timestamp == timestamp }
        saveHistory(list)
    }

    fun clearHistory(mode: String? = null) {
        if (mode == null) {
            saveHistory(emptyList())
        } else {
            val list = getHistory().filter { it.mode != mode }
            saveHistory(list)
        }
    }

    private fun saveHistory(list: List<HistoryItem>) {
        try {
            val prefs = prefs ?: return
            val json = gson.toJson(list)
            prefs.edit { putString(KEY_HISTORY, json) }
        } catch (_: Exception) {
        }
    }
}