package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonitoredSite
import com.example.ui.StatusFilter
import com.example.ui.components.LatencyPill
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusDegraded
import com.example.ui.theme.StatusDown
import com.example.ui.theme.StatusUp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    sites: List<MonitoredSite>,
    allSites: List<MonitoredSite>,
    checkingSiteIds: Set<Long>,
    isCheckingAll: Boolean,
    autoRefreshInterval: Int,
    searchQuery: String,
    statusFilter: StatusFilter,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (StatusFilter) -> Unit,
    onAutoRefreshChange: (Int) -> Unit,
    onCheckAll: () -> Unit,
    onCheckSite: (MonitoredSite) -> Unit,
    onSelectSite: (MonitoredSite) -> Unit,
    onAddSiteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRefreshMenu by remember { mutableStateOf(false) }

    val upCount = allSites.count { it.lastStatus == "UP" }
    val degradedCount = allSites.count { it.lastStatus == "DEGRADED" }
    val downCount = allSites.count { it.lastStatus == "DOWN" }
    val isAllOperational = downCount == 0 && degradedCount == 0 && allSites.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overall System Status Card
            item {
                SystemStatusHeader(
                    isOperational = isAllOperational,
                    totalCount = allSites.size,
                    downCount = downCount,
                    degradedCount = degradedCount,
                    isCheckingAll = isCheckingAll,
                    autoRefreshInterval = autoRefreshInterval,
                    onOpenRefreshMenu = { showRefreshMenu = true },
                    onCheckAll = onCheckAll
                )

                // Refresh menu dropdown
                DropdownMenu(
                    expanded = showRefreshMenu,
                    onDismissRequest = { showRefreshMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Auto-refresh: Off") },
                        onClick = {
                            onAutoRefreshChange(0)
                            showRefreshMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Every 15 seconds") },
                        onClick = {
                            onAutoRefreshChange(15)
                            showRefreshMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Every 30 seconds") },
                        onClick = {
                            onAutoRefreshChange(30)
                            showRefreshMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Every 60 seconds") },
                        onClick = {
                            onAutoRefreshChange(60)
                            showRefreshMenu = false
                        }
                    )
                }
            }

            // Summary Metric Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCounter(
                        label = "Monitored",
                        count = allSites.size,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCounter(
                        label = "Online",
                        count = upCount,
                        color = StatusUp,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCounter(
                        label = "Degraded",
                        count = degradedCount,
                        color = StatusDegraded,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCounter(
                        label = "Down",
                        count = downCount,
                        color = StatusDown,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search and Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search websites...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_search_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = statusFilter == StatusFilter.ALL,
                            onClick = { onFilterChange(StatusFilter.ALL) },
                            label = { Text("All (${allSites.size})") },
                            modifier = Modifier.testTag("filter_all_chip")
                        )
                        FilterChip(
                            selected = statusFilter == StatusFilter.UP,
                            onClick = { onFilterChange(StatusFilter.UP) },
                            label = { Text("Online ($upCount)") },
                            modifier = Modifier.testTag("filter_up_chip")
                        )
                        FilterChip(
                            selected = statusFilter == StatusFilter.DOWN,
                            onClick = { onFilterChange(StatusFilter.DOWN) },
                            label = { Text("Issues (${downCount + degradedCount})") },
                            modifier = Modifier.testTag("filter_down_chip")
                        )
                    }
                }
            }

            // Monitored Sites List
            if (sites.isEmpty()) {
                item {
                    EmptySitesView(
                        isFiltered = searchQuery.isNotEmpty() || statusFilter != StatusFilter.ALL,
                        onAddClick = onAddSiteClick
                    )
                }
            } else {
                items(sites, key = { it.id }) { site ->
                    val isChecking = checkingSiteIds.contains(site.id)
                    SiteCard(
                        site = site,
                        isChecking = isChecking,
                        onPingClick = { onCheckSite(site) },
                        onCardClick = { onSelectSite(site) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddSiteClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_site_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Website")
        }
    }
}

@Composable
private fun SystemStatusHeader(
    isOperational: Boolean,
    totalCount: Int,
    downCount: Int,
    degradedCount: Int,
    isCheckingAll: Boolean,
    autoRefreshInterval: Int,
    onOpenRefreshMenu: () -> Unit,
    onCheckAll: () -> Unit
) {
    val visual = when {
        totalCount == 0 -> HeaderStatusVisual(
            "No Websites Monitored",
            "Add a website to start real-time tracking",
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
        downCount > 0 -> HeaderStatusVisual(
            "$downCount Website${if (downCount > 1) "s" else ""} DOWN",
            "Disruption detected. Immediate action required.",
            StatusDown,
            StatusDown.copy(alpha = 0.14f)
        )
        degradedCount > 0 -> HeaderStatusVisual(
            "$degradedCount Service Degraded",
            "High latency or client errors observed.",
            StatusDegraded,
            StatusDegraded.copy(alpha = 0.14f)
        )
        else -> HeaderStatusVisual(
            "All Systems Operational",
            "All $totalCount monitored websites are responding normally.",
            StatusUp,
            StatusUp.copy(alpha = 0.14f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = visual.bgColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, visual.color.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(visual.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = visual.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = visual.color
                    )
                }

                // Auto-refresh badge button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .clickable { onOpenRefreshMenu() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (autoRefreshInterval > 0) "${autoRefreshInterval}s" else "Manual",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = visual.subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Check All Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onCheckAll,
                    enabled = !isCheckingAll,
                    modifier = Modifier.testTag("check_all_button")
                ) {
                    if (isCheckingAll) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Checking all...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check All Now")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCounter(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SiteCard(
    site: MonitoredSite,
    isChecking: Boolean,
    onPingClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val lastCheckedText = site.lastChecked?.let { "Checked ${timeFormat.format(Date(it))}" } ?: "Never checked"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("site_card_${site.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = site.name.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = site.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = site.url.removePrefix("https://").removePrefix("http://"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                StatusBadge(
                    status = site.lastStatus,
                    isChecking = isChecking
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row: Latency, Status code, Last checked, and Ping button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LatencyPill(
                        latencyMs = site.lastLatencyMs,
                        isDown = site.lastStatus == "DOWN"
                    )

                    if (site.lastStatusCode != null && site.lastStatusCode > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "HTTP ${site.lastStatusCode}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = lastCheckedText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = onPingClick,
                    enabled = !isChecking,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("ping_button_${site.id}")
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ping site",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySitesView(
    isFiltered: Boolean,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = if (isFiltered) "No websites matched your filter" else "No websites monitored yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isFiltered) "Try adjusting your search query or filter" else "Add your websites or API endpoints to monitor in real-time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isFiltered) {
                TextButton(onClick = onAddClick) {
                    Text("Add First Website")
                }
            }
        }
    }
}

private data class HeaderStatusVisual(
    val text: String,
    val subtext: String,
    val color: Color,
    val bgColor: Color
)
