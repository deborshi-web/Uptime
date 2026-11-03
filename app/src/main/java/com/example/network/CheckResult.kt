package com.example.network

enum class StatusVerdict {
    UP,
    DEGRADED,
    DOWN,
    UNKNOWN
}

data class CheckResult(
    val url: String,
    val verdict: StatusVerdict,
    val statusCode: Int = 0,
    val statusMessage: String = "",
    val latencyMs: Long = 0L,
    val dnsLookupMs: Long = 0L,
    val connectMs: Long = 0L,
    val resolvedIp: String = "Unknown",
    val protocol: String = "Unknown",
    val tlsVersion: String? = null,
    val cipherSuite: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val redirectUrl: String? = null,
    val errorMessage: String? = null,
    val checkedAt: Long = System.currentTimeMillis()
) {
    val isUp: Boolean get() = verdict == StatusVerdict.UP
    val isDown: Boolean get() = verdict == StatusVerdict.DOWN
    val isDegraded: Boolean get() = verdict == StatusVerdict.DEGRADED

    fun getStatusCodeCategory(): String = when (statusCode) {
        in 200..299 -> "2xx Success"
        in 300..399 -> "3xx Redirection"
        in 400..499 -> "4xx Client Error"
        in 500..599 -> "5xx Server Error"
        else -> if (errorMessage != null) "Connection Failure" else "Unknown"
    }

    fun getStatusCodeDescription(): String = when (statusCode) {
        200 -> "200 OK — The request has succeeded."
        201 -> "201 Created — Request succeeded and resource created."
        204 -> "204 No Content — Server successfully processed request with no content."
        301 -> "301 Moved Permanently — Target resource has been assigned a new permanent URI."
        302 -> "302 Found — Target resource resides temporarily under different URI."
        307 -> "307 Temporary Redirect — Target resource resides temporarily under different URI."
        308 -> "308 Permanent Redirect — Target resource has a permanent redirect."
        400 -> "400 Bad Request — Server cannot process request due to client error."
        401 -> "401 Unauthorized — Authentication is required and has failed or not been provided."
        403 -> "403 Forbidden — Server understood request but refuses to authorize it."
        404 -> "404 Not Found — Server has not found anything matching the Request-URI."
        408 -> "408 Request Timeout — Server did not produce response within time limit."
        429 -> "429 Too Many Requests — Client has sent too many requests (Rate Limited)."
        500 -> "500 Internal Server Error — The server encountered an unexpected condition."
        502 -> "502 Bad Gateway — Server acting as gateway received an invalid response."
        503 -> "503 Service Unavailable — Server is temporarily unable to handle request."
        504 -> "504 Gateway Timeout — Gateway server did not receive timely response."
        else -> if (errorMessage != null) errorMessage else "Status code $statusCode"
    }
}
