package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MonitoredSite
import com.example.network.CheckResult
import com.example.network.StatusVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SitePulse", appName)
    }

    @Test
    fun `test uptime percentage calculation`() {
        val site = MonitoredSite(
            name = "Test Site",
            url = "https://example.com",
            totalChecks = 10,
            successfulChecks = 9
        )
        assertEquals(90.0f, site.uptimePercentage, 0.01f)
    }

    @Test
    fun `test CheckResult verdict status helpers`() {
        val result = CheckResult(
            url = "https://example.com",
            verdict = StatusVerdict.UP,
            statusCode = 200,
            latencyMs = 120
        )
        assertTrue(result.isUp)
        assertEquals("2xx Success", result.getStatusCodeCategory())
    }
}
