package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.CheckResult
import com.example.network.StatusVerdict
import com.example.ui.theme.StatusDegraded
import com.example.ui.theme.StatusDown
import com.example.ui.theme.StatusUp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstantCheckScreen(
    result: CheckResult?,
    isLoading: Boolean,
    onRunCheck: (String) -> Unit,
    onAddToWatchlist: (name: String, url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputUrl by remember { mutableStateOf("") }
    var showHeaders by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val popularPresets = listOf(
        "google.com",
        "github.com",
        "cloudflare.com",
        "openai.com",
        "reddit.com",
        "wikipedia.org",
        "aws.amazon.com",
        "discord.com"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Description
        item {
            Column {
                Text(
                    text = "Instant Health Checker",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Perform an immediate live diagnostic test on any URL or IP.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Input card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Website URL or domain") },
                        placeholder = { Text("e.g. example.com or api.service.io") },
                        leadingIcon = {
                            Icon(Icons.Default.Language, contentDescription = null)
                        },
                        trailingIcon = {
                            if (inputUrl.isNotEmpty()) {
                                IconButton(onClick = { inputUrl = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("instant_check_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onRunCheck(inputUrl) },
                        enabled = inputUrl.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("instant_check_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pinging Target Server...")
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check Website Health Now")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Quick Test Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        popularPresets.forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    inputUrl = preset
                                    onRunCheck(preset)
                                },
                                label = { Text(preset, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Live Diagnostic Result
        if (result != null) {
            item {
                DiagnosticResultCard(
                    result = result,
                    showHeaders = showHeaders,
                    onToggleHeaders = { showHeaders = !showHeaders },
                    onCopyHeaders = {
                        val headerText = result.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                        clipboardManager.setText(AnnotatedString(headerText))
                    },
                    onAddToWatchlist = {
                        val domain = result.url.removePrefix("https://").removePrefix("http://").substringBefore("/")
                        onAddToWatchlist(domain, result.url)
                    }
                )
            }
        }
    }
}

@Composable
private fun DiagnosticResultCard(
    result: CheckResult,
    showHeaders: Boolean,
    onToggleHeaders: () -> Unit,
    onCopyHeaders: () -> Unit,
    onAddToWatchlist: () -> Unit
) {
    val visual = when (result.verdict) {
        StatusVerdict.UP -> DiagnosticVisual(
            StatusUp,
            StatusUp.copy(alpha = 0.12f),
            "WEBSITE IS ONLINE & HEALTHY",
            Icons.Default.CheckCircle
        )
        StatusVerdict.DEGRADED -> DiagnosticVisual(
            StatusDegraded,
            StatusDegraded.copy(alpha = 0.12f),
            "SERVICE DEGRADED / RESTRICTED",
            Icons.Default.Warning
        )
        StatusVerdict.DOWN -> DiagnosticVisual(
            StatusDown,
            StatusDown.copy(alpha = 0.12f),
            "WEBSITE IS DOWN / UNREACHABLE",
            Icons.Default.Error
        )
        StatusVerdict.UNKNOWN -> DiagnosticVisual(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant,
            "UNKNOWN STATUS",
            Icons.Default.Error
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, visual.color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Status banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(visual.bgColor)
                    .border(1.dp, visual.color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.color,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = visual.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = visual.color
                        )
                        Text(
                            text = result.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // HTTP Status Description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(10.dp)
            ) {
                Text(
                    text = result.getStatusCodeDescription(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Telemetry Grid
            Text(
                text = "Live Telemetry & Diagnostics",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryBox(
                    label = "Total Latency",
                    value = "${result.latencyMs} ms",
                    color = if (result.latencyMs < 250) StatusUp else if (result.latencyMs < 800) StatusDegraded else StatusDown,
                    modifier = Modifier.weight(1f)
                )
                TelemetryBox(
                    label = "HTTP Status",
                    value = if (result.statusCode > 0) "${result.statusCode}" else "ERR",
                    color = visual.color,
                    modifier = Modifier.weight(1f)
                )
                TelemetryBox(
                    label = "Protocol",
                    value = result.protocol,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryBox(
                    label = "DNS Lookup",
                    value = if (result.dnsLookupMs > 0) "${result.dnsLookupMs} ms" else "< 1 ms",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TelemetryBox(
                    label = "Connect / TLS",
                    value = if (result.connectMs > 0) "${result.connectMs} ms" else "--",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TelemetryBox(
                    label = "Resolved IP",
                    value = result.resolvedIp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            if (result.tlsVersion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = StatusUp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SSL/TLS Security",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${result.tlsVersion} (${result.cipherSuite?.substringAfterLast("_") ?: ""})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable HTTP Headers
            if (result.headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleHeaders() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Response Headers (${result.headers.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showHeaders) {
                            IconButton(onClick = onCopyHeaders, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy headers", modifier = Modifier.size(16.dp))
                            }
                        }
                        Icon(
                            imageVector = if (showHeaders) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showHeaders,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        result.headers.forEach { (key, value) ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "$key: ",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = value,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add to watchlist button
            OutlinedButton(
                onClick = onAddToWatchlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_to_watchlist_from_instant_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Monitored Watchlist")
            }
        }
    }
}

@Composable
private fun TelemetryBox(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private data class DiagnosticVisual(
    val color: androidx.compose.ui.graphics.Color,
    val bgColor: androidx.compose.ui.graphics.Color,
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
