package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CheckLog
import com.example.data.MonitoredSite
import com.example.data.SiteRepository
import com.example.network.CheckResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StatusFilter {
    ALL,
    UP,
    DOWN
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SiteRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SiteRepository(db.siteDao())
        viewModelScope.launch {
            repository.ensureDefaultSitesSeeded()
        }
    }

    val rawSites: StateFlow<List<MonitoredSite>> = repository.allSites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

    val filteredSites: StateFlow<List<MonitoredSite>> = combine(
        rawSites,
        _searchQuery,
        _statusFilter
    ) { sites, query, filter ->
        sites.filter { site ->
            val matchesQuery = query.isBlank() ||
                    site.name.contains(query, ignoreCase = true) ||
                    site.url.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                StatusFilter.ALL -> true
                StatusFilter.UP -> site.lastStatus == "UP"
                StatusFilter.DOWN -> site.lastStatus == "DOWN" || site.lastStatus == "DEGRADED"
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidentLogs: StateFlow<List<CheckLog>> = repository.incidentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isCheckingAll = MutableStateFlow(false)
    val isCheckingAll: StateFlow<Boolean> = _isCheckingAll.asStateFlow()

    private val _checkingSiteIds = MutableStateFlow<Set<Long>>(emptySet())
    val checkingSiteIds: StateFlow<Set<Long>> = _checkingSiteIds.asStateFlow()

    // Instant Check State
    private val _instantCheckResult = MutableStateFlow<CheckResult?>(null)
    val instantCheckResult: StateFlow<CheckResult?> = _instantCheckResult.asStateFlow()

    private val _isInstantChecking = MutableStateFlow(false)
    val isInstantChecking: StateFlow<Boolean> = _isInstantChecking.asStateFlow()

    // Selected Site for Detail Sheet
    private val _selectedSite = MutableStateFlow<MonitoredSite?>(null)
    val selectedSite: StateFlow<MonitoredSite?> = _selectedSite.asStateFlow()

    val selectedSiteLogs: StateFlow<List<CheckLog>> = _selectedSite.flatMapLatest { site ->
        if (site != null) {
            repository.getLogsForSite(site.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auto-refresh interval (0 = off, 15, 30, 60 seconds)
    private val _autoRefreshInterval = MutableStateFlow(0)
    val autoRefreshInterval: StateFlow<Int> = _autoRefreshInterval.asStateFlow()

    private var autoRefreshJob: Job? = null

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: StatusFilter) {
        _statusFilter.value = filter
    }

    fun selectSite(site: MonitoredSite?) {
        _selectedSite.value = site
    }

    fun addSite(name: String, url: String) {
        viewModelScope.launch {
            val normalizedUrl = if (!url.startsWith("http://", ignoreCase = true) &&
                !url.startsWith("https://", ignoreCase = true)
            ) {
                "https://$url"
            } else {
                url
            }
            val id = repository.addSite(name, normalizedUrl)
            // Immediately run a check for the new site
            val created = MonitoredSite(id = id, name = name, url = normalizedUrl)
            checkSingleSite(created)
        }
    }

    fun deleteSite(site: MonitoredSite) {
        viewModelScope.launch {
            if (_selectedSite.value?.id == site.id) {
                _selectedSite.value = null
            }
            repository.removeSite(site)
        }
    }

    fun checkSingleSite(site: MonitoredSite) {
        if (_checkingSiteIds.value.contains(site.id)) return
        viewModelScope.launch {
            _checkingSiteIds.value = _checkingSiteIds.value + site.id
            try {
                val result = repository.checkSite(site)
                // If this site is currently viewed in detail, update selectedSite reference
                if (_selectedSite.value?.id == site.id) {
                    _selectedSite.value = _selectedSite.value?.copy(
                        lastChecked = result.checkedAt,
                        lastStatusCode = result.statusCode,
                        lastLatencyMs = result.latencyMs,
                        lastStatus = result.verdict.name,
                        lastErrorMessage = result.errorMessage
                    )
                }
            } finally {
                _checkingSiteIds.value = _checkingSiteIds.value - site.id
            }
        }
    }

    fun checkAllSites() {
        if (_isCheckingAll.value) return
        viewModelScope.launch {
            _isCheckingAll.value = true
            val currentList = rawSites.value
            for (site in currentList) {
                _checkingSiteIds.value = _checkingSiteIds.value + site.id
                try {
                    repository.checkSite(site)
                } catch (e: Exception) {
                    // Handled within repository
                } finally {
                    _checkingSiteIds.value = _checkingSiteIds.value - site.id
                }
            }
            _isCheckingAll.value = false
        }
    }

    fun runInstantCheck(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _isInstantChecking.value = true
            _instantCheckResult.value = null
            try {
                val result = repository.runInstantCheck(url)
                _instantCheckResult.value = result
            } finally {
                _isInstantChecking.value = false
            }
        }
    }

    fun setAutoRefresh(seconds: Int) {
        _autoRefreshInterval.value = seconds
        autoRefreshJob?.cancel()
        if (seconds > 0) {
            autoRefreshJob = viewModelScope.launch {
                while (true) {
                    delay(seconds * 1000L)
                    checkAllSites()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
