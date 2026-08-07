package com.unicalculator.app.ui.screens

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

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.unicalculator.app.ui.theme.LocalCalculatorColors

data class LicenseEntry(
    val name: String,
    val copyright: String,
    val licenseText: String,
    val url: String
)

object LicenseTexts {
    val APACHE_2_0 = """
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
    """.trimIndent()

    val GPL_2_0 = """
        This program is free software; you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation; either version 2 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.
    """.trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    val colors = LocalCalculatorColors.current

    val licenses = listOf(
        LicenseEntry(
            name = "Qalculate Android",
            copyright = "Copyright 2022-2025 jherkenhoff",
            licenseText = LicenseTexts.GPL_2_0,
            url = "https://github.com/jherkenhoff/qalculate-android"
        ),
        LicenseEntry(
            name = "Gson",
            copyright = "Copyright 2008-2018 Google Inc.",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://github.com/google/gson"
        ),
        LicenseEntry(
            name = "Kotlin Coroutines",
            copyright = "Copyright 2016-2024 JetBrains s.r.o.",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://github.com/Kotlin/kotlinx.coroutines"
        ),
        LicenseEntry(
            name = "Material Design Icons",
            copyright = "Copyright Google",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://github.com/google/material-design-icons"
        ),
        LicenseEntry(
            name = "AndroidX DataStore",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/datastore"
        ),
        LicenseEntry(
            name = "AndroidX Activity Compose",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/activity"
        ),
        LicenseEntry(
            name = "AndroidX Lifecycle Runtime KTX",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
        ),
        LicenseEntry(
            name = "AndroidX Core KTX",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/core"
        ),
        LicenseEntry(
            name = "Jetpack Compose UI",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/compose-ui"
        ),
        LicenseEntry(
            name = "Jetpack Compose UI Graphics",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/compose-ui"
        ),
        LicenseEntry(
            name = "Jetpack Compose UI Tooling Preview",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/compose-ui"
        ),
        LicenseEntry(
            name = "Jetpack Compose Material3",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/compose-material3"
        ),
        LicenseEntry(
            name = "Material Icons Extended",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/compose-material"
        ),
        LicenseEntry(
            name = "Material3 Window Size Class",
            copyright = "Copyright The Android Open Source Project",
            licenseText = LicenseTexts.APACHE_2_0,
            url = "https://developer.android.com/jetpack/androidx/releases/compose-material3"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Licenses", color = colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(licenses) { entry ->
                LicenseCard(entry, colors)
            }
        }
    }
}

@Composable
private fun LicenseCard(entry: LicenseEntry, colors: com.unicalculator.app.ui.theme.CalculatorColors) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.numberButton),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = entry.name,
                color = colors.someLink,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, entry.url.toUri())
                        context.startActivity(intent)
                    }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.copyright,
                color = colors.textSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.licenseText,
                color = colors.textSecondary.copy(alpha = 0.7f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}