package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

class UptimeChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {

    suspend fun checkUrl(rawUrl: String): CheckResult = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeUrl(rawUrl)
        val dnsTime = AtomicLong(0L)
        val connectTime = AtomicLong(0L)
        var dnsStartTime = 0L
        var connectStartTime = 0L

        val eventListener = object : EventListener() {
            override fun dnsStart(call: Call, domainName: String) {
                dnsStartTime = System.currentTimeMillis()
            }

            override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
                if (dnsStartTime > 0) {
                    dnsTime.set(System.currentTimeMillis() - dnsStartTime)
                }
            }

            override fun connectStart(call: Call, inetSocketAddress: java.net.InetSocketAddress, proxy: java.net.Proxy) {
                connectStartTime = System.currentTimeMillis()
            }

            override fun connectEnd(
                call: Call,
                inetSocketAddress: java.net.InetSocketAddress,
                proxy: java.net.Proxy,
                protocol: okhttp3.Protocol?
            ) {
                if (connectStartTime > 0) {
                    connectTime.set(System.currentTimeMillis() - connectStartTime)
                }
            }
        }

        val request = try {
            Request.Builder()
                .url(normalizedUrl)
                .header("User-Agent", "SitePulse-Monitor/1.0 (Android; Mobile Real-Time Uptime Checker)")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build()
        } catch (e: IllegalArgumentException) {
            return@withContext CheckResult(
                url = normalizedUrl,
                verdict = StatusVerdict.DOWN,
                errorMessage = "Invalid URL format: ${e.message}"
            )
        }

        val specializedClient = client.newBuilder()
            .eventListener(eventListener)
            .build()

        val startTime = System.currentTimeMillis()
        try {
            specializedClient.newCall(request).execute().use { response ->
                val totalLatency = System.currentTimeMillis() - startTime
                val code = response.code
                val message = response.message
                val protocolStr = response.protocol.toString().uppercase()

                val handshake = response.handshake
                val tlsVersion = handshake?.tlsVersion?.javaName
                val cipherSuite = handshake?.cipherSuite?.javaName

                val headerMap = mutableMapOf<String, String>()
                for (name in response.headers.names()) {
                    headerMap[name] = response.header(name) ?: ""
                }

                // Resolve host IP if possible
                val host = request.url.host
                val resolvedIp = try {
                    InetAddress.getByName(host).hostAddress ?: "Unknown"
                } catch (e: Exception) {
                    "Resolved"
                }

                val verdict = when {
                    code in 200..299 -> {
                        if (totalLatency > 2500) StatusVerdict.DEGRADED else StatusVerdict.UP
                    }
                    code in 300..399 -> StatusVerdict.UP
                    code in 400..499 -> {
                        // 401 Unauthorized / 403 Forbidden means server is operational but restricts access
                        if (code == 401 || code == 403 || code == 429) {
                            StatusVerdict.DEGRADED
                        } else {
                            StatusVerdict.DEGRADED
                        }
                    }
                    code in 500..599 -> StatusVerdict.DOWN
                    else -> StatusVerdict.DEGRADED
                }

                CheckResult(
                    url = normalizedUrl,
                    verdict = verdict,
                    statusCode = code,
                    statusMessage = message,
                    latencyMs = totalLatency,
                    dnsLookupMs = dnsTime.get(),
                    connectMs = connectTime.get(),
                    resolvedIp = resolvedIp,
                    protocol = protocolStr,
                    tlsVersion = tlsVersion,
                    cipherSuite = cipherSuite,
                    headers = headerMap,
                    redirectUrl = response.priorResponse?.request?.url?.toString(),
                    errorMessage = if (code >= 500) "HTTP $code Server Error: $message" else null,
                    checkedAt = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            val totalLatency = System.currentTimeMillis() - startTime
            val friendlyError = mapExceptionToMessage(e)
            CheckResult(
                url = normalizedUrl,
                verdict = StatusVerdict.DOWN,
                statusCode = 0,
                latencyMs = totalLatency,
                dnsLookupMs = dnsTime.get(),
                connectMs = connectTime.get(),
                errorMessage = friendlyError,
                checkedAt = System.currentTimeMillis()
            )
        }
    }

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return if (!trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
        ) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    private fun mapExceptionToMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "DNS Lookup Failed: Domain name could not be resolved. Verify URL or check internet connection."
            is SocketTimeoutException -> "Connection Timeout (>10s): The server took too long to respond. It may be offline or heavily congested."
            is ConnectException -> "Connection Refused: Target server actively refused connection on port 80/443. Web server may be stopped."
            is SSLHandshakeException, is SSLPeerUnverifiedException, is CertificateException ->
                "SSL/TLS Certificate Error: Failed secure handshake (${e.localizedMessage ?: "Invalid or untrusted certificate"})."
            is ProtocolException -> "Protocol Error: Server sent an unparseable or illegal HTTP response."
            is IOException -> "Network Error: ${e.localizedMessage ?: "I/O communication failure"}"
            else -> "Error: ${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }
}
