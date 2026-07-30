@file:Suppress("DEPRECATION")

package com.unicalculator.app.ui.theme

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

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

private data class LinkRange(val start: Int, val end: Int, val url: String)

data class RowItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: (() -> Unit)? = null,
    val isClickable: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onLicensesClick: () -> Unit
) {
    BackHandler { onBack() }

    val colors = LocalCalculatorColors.current
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    val privacyText = """
        UniCalculator does not collect, store, or transmit any personal data.
        No analytics, no tracking, no advertisements.

        All calculations are performed locally on your device.
        No information is ever sent to any server.

        This policy is subject to change; any changes will be posted here.
        Last updated: July 2026
    """.trimIndent()

    val creditsPlainText = """
        This app is built with the help of many amazing open‑source projects:

        • Qalculate! – The powerful calculation engine
        https://qalculate.github.io/

        • qalculate-android – Android port
        https://github.com/jherkenhoff/qalculate-android

        • Gson – JSON serialization (Apache 2.0)
        • Kotlin Coroutines – Asynchronous programming (Apache 2.0)
        • Android Jetpack Compose – Modern UI toolkit (Apache 2.0)

        Special thanks to all contributors and testers who made this app better.
    """.trimIndent()

    val linkRanges = remember {
        val ranges = mutableListOf<LinkRange>()
        val url1 = "https://qalculate.github.io/"
        val start1 = creditsPlainText.indexOf(url1)
        if (start1 != -1) {
            ranges.add(LinkRange(start1, start1 + url1.length, url1))
        }
        val url2 = "https://github.com/jherkenhoff/qalculate-android"
        val start2 = creditsPlainText.indexOf(url2)
        if (start2 != -1) {
            ranges.add(LinkRange(start2, start2 + url2.length, url2))
        }
        ranges
    }

    val creditsAnnotated = buildAnnotatedString {
        append(creditsPlainText)
        linkRanges.forEach { range ->
            addStyle(
                style = SpanStyle(
                    color = Color(0xFF4FC3F7),
                    textDecoration = TextDecoration.Underline
                ),
                start = range.start,
                end = range.end
            )
        }
    }

    val helpSection = listOf(
        RowItem(
            icon = Icons.Outlined.Star,
            title = "Star on GitHub",
            subtitle = "Show your support on GitHub",
            onClick = {
                val url = "https://github.com/Jomet-Franklin/UniCalculator"
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
        ),
        RowItem(
            icon = Icons.Outlined.Payments,
            title = "Donate",
            subtitle = "Support this app with a PayPal donation",
            onClick = {
                val url = "https://www.paypal.me/JometFranklin"
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
        )
    )

    val supportSection = listOf(
        RowItem(
            icon = Icons.Outlined.Email,
            title = "Email",
            subtitle = "jometfranklin143@gmail.com",
            onClick = {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:jometfranklin143@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "UniCalculator Support")
                }
                context.startActivity(emailIntent)
            }
        )
    )

    val creditsSection = listOf(
        RowItem(
            icon = Icons.Outlined.People,
            title = "Credits",
            subtitle = "See the contributors and libraries",
            onClick = {
                dialogTitle = "Credits"
                dialogMessage = ""
                showDialog = true
            }
        )
    )

    val infoSection = listOf(
        RowItem(
            icon = Icons.Outlined.Code,
            title = "Open source code",
            subtitle = "View open source on GitHub",
            onClick = {
                val url = "https://github.com/Jomet-Franklin/UniCalculator"
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            }
        ),
        RowItem(
            icon = Icons.Outlined.Description,
            title = "Licenses",
            subtitle = "View licenses and attributions",
            onClick = onLicensesClick
        ),
        RowItem(
            icon = Icons.Outlined.Shield,
            title = "Privacy policy",
            subtitle = "View privacy policy",
            onClick = {
                dialogTitle = "Privacy Policy"
                dialogMessage = privacyText
                showDialog = true
            }
        ),

        // =========== Version ===========

        RowItem(
            icon = Icons.Outlined.Info,
            title = "Version",
            subtitle = "1.1.0",
            isClickable = false
        )
    )

    val sectionHeaders = listOf(
        "Help" to helpSection,
        "Support" to supportSection,
        "Credits" to creditsSection,
        "App info" to infoSection
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", color = colors.textPrimary) },
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sectionHeaders.forEach { (header, items) ->
                    item {
                        Text(
                            text = header,
                            color = colors.accentButton,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(top = 24.dp, bottom = 12.dp)
                        )
                    }

                    items(items) { item ->
                        RowItemView(item = item, colors = colors)
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = {
                        Text(
                            dialogTitle,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        if (dialogTitle == "Credits") {
                            ClickableText(
                                text = creditsAnnotated,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) { offset ->
                                linkRanges.forEach { range ->
                                    if (offset in range.start..range.end) {
                                        val intent = Intent(Intent.ACTION_VIEW, range.url.toUri())
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = dialogMessage,
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colors.accentButton
                            )
                        ) {
                            Text("Close")
                        }
                    },
                    containerColor = colors.functionButton,
                    tonalElevation = 8.dp
                )
            }
        }
    }
}

@Composable
private fun RowItemView(item: RowItem, colors: CalculatorColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.isClickable) {
                    Modifier.clickable { item.onClick?.invoke() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(24.dp))

        Column {
            Text(
                text = item.title,
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = item.subtitle,
                color = colors.textSecondary.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}