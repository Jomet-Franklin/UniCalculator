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
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

data class HistoryItem(
    val expression: String,
    val result: String,
    val timestamp: Long,
    val mode: String,
    val pinned: Boolean = false
)

object HistoryManager {
    private const val PREF_NAME = "history_prefs"
    private const val KEY_HISTORY = "history_list"
    private const val KEY_KEEP_PINNED = "history_keep_pinned"
    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        getHistory()
    }

    fun addEntry(expression: String, result: String, mode: String) {
        try {
            val list = getHistory().toMutableList()
            val last = list.firstOrNull()
            val isDuplicate = last != null &&
                    last.expression == expression &&
                    last.result == result &&
                    last.mode == mode
            val wasPinned = isDuplicate && last?.pinned == true
            if (isDuplicate) list.removeAt(0)
            list.add(0, HistoryItem(expression, result, System.currentTimeMillis(), mode, wasPinned))
            saveHistory(list)
        } catch (_: Exception) {}
    }

    fun getHistory(): List<HistoryItem> {
        val p = prefs ?: return emptyList()
        val json = p.getString(KEY_HISTORY, null)
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<JsonObject>>() {}.type
            val raw: List<JsonObject>? = gson.fromJson(json, type)
            raw?.mapNotNull { obj ->
                try {
                    val expression = obj.get("expression")?.asString ?: return@mapNotNull null
                    val result = obj.get("result")?.asString ?: return@mapNotNull null
                    val timestamp = obj.get("timestamp")?.asLong ?: return@mapNotNull null
                    val mode = obj.get("mode")?.asString ?: return@mapNotNull null
                    val pinned = try { obj.get("pinned")?.asBoolean ?: false } catch (_: Exception) { false }
                    HistoryItem(expression, result, timestamp, mode, pinned)
                } catch (_: Exception) { null }
            } ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun deleteItems(timestamps: List<Long>): List<HistoryItem> {
        val all = getHistory()
        val ids = timestamps.toSet()
        val removed = all.filter { it.timestamp in ids }
        saveHistory(all.filterNot { it.timestamp in ids })
        return removed
    }

    fun restoreItems(items: List<HistoryItem>) {
        if (items.isEmpty()) return
        val current = getHistory()
        saveHistory((current + items).distinctBy { it.timestamp }.sortedByDescending { it.timestamp })
    }

    fun togglePin(timestamps: List<Long>) {
        val ids = timestamps.toSet()
        saveHistory(getHistory().map { if (it.timestamp in ids) it.copy(pinned = !it.pinned) else it })
    }

    fun setPinned(timestamps: List<Long>, pinned: Boolean) {
        val ids = timestamps.toSet()
        saveHistory(getHistory().map { if (it.timestamp in ids) it.copy(pinned = pinned) else it })
    }

    fun count(mode: String?): Int {
        val list = getHistory()
        return if (mode == null) list.size else list.count { it.mode == mode }
    }

    fun clearHistory(mode: String? = null, keepPinned: Boolean = false): List<HistoryItem> {
        val all = getHistory()
        val toRemove = when {
            mode == null && !keepPinned -> all
            mode == null -> all.filter { !it.pinned }
            !keepPinned -> all.filter { it.mode == mode }
            else -> all.filter { it.mode == mode && !it.pinned }
        }
        val removeIds = toRemove.map { it.timestamp }.toSet()
        saveHistory(all.filterNot { it.timestamp in removeIds })
        return toRemove
    }

    fun getKeepPinned(): Boolean = prefs?.getBoolean(KEY_KEEP_PINNED, true) ?: true
    fun setKeepPinned(value: Boolean) { prefs?.edit { putBoolean(KEY_KEEP_PINNED, value) } }

    private fun saveHistory(list: List<HistoryItem>) {
        try {
            val p = prefs ?: return
            p.edit { putString(KEY_HISTORY, gson.toJson(list)) }
        } catch (_: Exception) {}
    }
}