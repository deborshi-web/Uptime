package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.AddSiteDialog
import com.example.ui.components.SiteDetailSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.IncidentHistoryScreen
import com.example.ui.screens.InstantCheckScreen
import com.example.ui.theme.SitePulseTheme
import com.example.ui.theme.StatusDown
import com.example.ui.theme.StatusUp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SitePulseTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val rawSites by viewModel.rawSites.collectAsStateWithLifecycle()
    val filteredSites by viewModel.filteredSites.collectAsStateWithLifecycle()
    val checkingSiteIds by viewModel.checkingSiteIds.collectAsStateWithLifecycle()
    val isCheckingAll by viewModel.isCheckingAll.collectAsStateWithLifecycle()
    val autoRefreshInterval by viewModel.autoRefreshInterval.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val incidentLogs by viewModel.incidentLogs.collectAsStateWithLifecycle()

    val instantCheckResult by viewModel.instantCheckResult.collectAsStateWithLifecycle()
    val isInstantChecking by viewModel.isInstantChecking.collectAsStateWithLifecycle()

    val selectedSite by viewModel.selectedSite.collectAsStateWithLifecycle()
    val selectedSiteLogs by viewModel.selectedSiteLogs.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Handle back button when sheet is open or tab is not dashboard
    BackHandler(enabled = selectedSite != null || selectedTab != 0) {
        if (selectedSite != null) {
            coroutineScope.launch { sheetState.hide() }
            viewModel.selectSite(null)
        } else if (selectedTab != 0) {
            selectedTab = 0
        }
    }

    // Auto trigger initial check on first launch once sites are loaded
    var initialCheckRun by remember { mutableStateOf(false) }
    LaunchedEffect(rawSites.isNotEmpty()) {
        if (rawSites.isNotEmpty() && !initialCheckRun) {
            initialCheckRun = true
            val needsCheck = rawSites.any { it.lastChecked == null }
            if (needsCheck) {
                viewModel.checkAllSites()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(StatusUp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SitePulse",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Watchlist"
                        )
                    },
                    label = { Text("Watchlist") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.NetworkCheck else Icons.Outlined.NetworkCheck,
                            contentDescription = "Instant Check"
                        )
                    },
                    label = { Text("Instant Check") },
                    modifier = Modifier.testTag("nav_tab_instant")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        if (incidentLogs.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = StatusDown) {
                                        Text("${incidentLogs.size.coerceAtMost(99)}")
                                    }
                                }
                            ) {
                                Icon(
                                    if (selectedTab == 2) Icons.Filled.Warning else Icons.Outlined.Warning,
                                    contentDescription = "Incidents"
                                )
                            }
                        } else {
                            Icon(
                                if (selectedTab == 2) Icons.Filled.Warning else Icons.Outlined.Warning,
                                contentDescription = "Incidents"
                            )
                        }
                    },
                    label = { Text("Incidents") },
                    modifier = Modifier.testTag("nav_tab_incidents")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    sites = filteredSites,
                    allSites = rawSites,
                    checkingSiteIds = checkingSiteIds,
                    isCheckingAll = isCheckingAll,
                    autoRefreshInterval = autoRefreshInterval,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onFilterChange = viewModel::setStatusFilter,
                    onAutoRefreshChange = viewModel::setAutoRefresh,
                    onCheckAll = viewModel::checkAllSites,
                    onCheckSite = viewModel::checkSingleSite,
                    onSelectSite = viewModel::selectSite,
                    onAddSiteClick = { showAddDialog = true }
                )
                1 -> InstantCheckScreen(
                    result = instantCheckResult,
                    isLoading = isInstantChecking,
                    onRunCheck = viewModel::runInstantCheck,
                    onAddToWatchlist = { name, url ->
                        viewModel.addSite(name, url)
                        selectedTab = 0
                    }
                )
                2 -> IncidentHistoryScreen(
                    incidentLogs = incidentLogs
                )
            }
        }
    }

    // Detail Bottom Sheet
    selectedSite?.let { site ->
        val isChecking = checkingSiteIds.contains(site.id)
        SiteDetailSheet(
            site = site,
            logs = selectedSiteLogs,
            isChecking = isChecking,
            sheetState = sheetState,
            onDismiss = {
                viewModel.selectSite(null)
            },
            onRecheck = {
                viewModel.checkSingleSite(site)
            },
            onDelete = {
                viewModel.deleteSite(site)
            }
        )
    }

    // Add Site Dialog
    if (showAddDialog) {
        AddSiteDialog(
            onDismiss = { showAddDialog = false },
            onAddSite = { name, url ->
                viewModel.addSite(name, url)
            }
        )
    }
}
