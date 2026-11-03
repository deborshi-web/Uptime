package com.example.data

import com.example.network.CheckResult
import com.example.network.StatusVerdict
import com.example.network.UptimeChecker
import kotlinx.coroutines.flow.Flow

class SiteRepository(
    private val siteDao: SiteDao,
    private val uptimeChecker: UptimeChecker = UptimeChecker()
) {
    val allSites: Flow<List<MonitoredSite>> = siteDao.getAllSites()
    val allRecentLogs: Flow<List<CheckLog>> = siteDao.getAllRecentLogs(50)
    val incidentLogs: Flow<List<CheckLog>> = siteDao.getIncidentLogs(50)

    fun getLogsForSite(siteId: Long): Flow<List<CheckLog>> = siteDao.getLogsForSite(siteId, 30)

    suspend fun ensureDefaultSitesSeeded() {
        if (siteDao.getSiteCount() == 0) {
            val defaults = listOf(
                MonitoredSite(name = "Google Search", url = "https://www.google.com"),
                MonitoredSite(name = "GitHub", url = "https://github.com"),
                MonitoredSite(name = "Cloudflare CDN", url = "https://www.cloudflare.com"),
                MonitoredSite(name = "Wikipedia", url = "https://www.wikipedia.org"),
                MonitoredSite(name = "Reddit", url = "https://www.reddit.com")
            )
            for (site in defaults) {
                siteDao.insertSite(site)
            }
        }
    }

    suspend fun addSite(name: String, url: String): Long {
        val site = MonitoredSite(
            name = name.ifBlank { url.removePrefix("https://").removePrefix("http://") },
            url = url
        )
        return siteDao.insertSite(site)
    }

    suspend fun removeSite(site: MonitoredSite) {
        siteDao.deleteLogsForSite(site.id)
        siteDao.deleteSite(site)
    }

    suspend fun checkSite(site: MonitoredSite): CheckResult {
        val result = uptimeChecker.checkUrl(site.url)
        val isSuccessful = result.verdict == StatusVerdict.UP || result.verdict == StatusVerdict.DEGRADED

        val updatedSite = site.copy(
            lastChecked = result.checkedAt,
            lastStatusCode = result.statusCode,
            lastLatencyMs = result.latencyMs,
            lastStatus = result.verdict.name,
            lastErrorMessage = result.errorMessage,
            totalChecks = site.totalChecks + 1,
            successfulChecks = site.successfulChecks + if (isSuccessful) 1 else 0
        )
        siteDao.updateSite(updatedSite)

        val log = CheckLog(
            siteId = site.id,
            siteName = site.name,
            url = site.url,
            timestamp = result.checkedAt,
            statusCode = result.statusCode,
            latencyMs = result.latencyMs,
            dnsTimeMs = result.dnsLookupMs,
            connectTimeMs = result.connectMs,
            status = result.verdict.name,
            protocol = result.protocol,
            ipAddress = result.resolvedIp,
            tlsVersion = result.tlsVersion,
            errorMessage = result.errorMessage
        )
        siteDao.insertLog(log)

        return result
    }

    suspend fun runInstantCheck(url: String): CheckResult {
        return uptimeChecker.checkUrl(url)
    }
}
