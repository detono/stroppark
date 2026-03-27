#!/usr/bin/env kotlin

import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.time.LocalDate

val accessToken = System.getenv("PLAY_API_TOKEN")
    ?: throw IllegalArgumentException("Missing PLAY_API_TOKEN")
val packageName = System.getenv("PACKAGE_NAME")
    ?: throw IllegalArgumentException("Missing PACKAGE_NAME")

// Dynamic dates: today and 7 days ago
val endDate = LocalDate.now()
val startDate = endDate.minusDays(7)

fun LocalDate.toJsonFragment() =
    """{ "year": $year, "month": ${monthValue}, "day": $dayOfMonth }"""

val payload = """
    {
      "timelineSpec": {
        "aggregationPeriod": "DAILY",
        "startTime": ${startDate.toJsonFragment()},
        "endTime": ${endDate.toJsonFragment()}
      }
    }
""".trimIndent()

val endpoint = URL(
    "https://playdeveloperreporting.googleapis.com/v1beta1/apps/$packageName/vitals/crashrate:query"
)

with(endpoint.openConnection() as HttpURLConnection) {
    requestMethod = "POST"
    setRequestProperty("Authorization", "Bearer $accessToken")
    setRequestProperty("Content-Type", "application/json")
    doOutput = true
    outputStream.write(payload.toByteArray())

    if (responseCode == HttpURLConnection.HTTP_OK) {
        val response = inputStream.bufferedReader().readText()

        // Parse crash-free rate from the response.
        // The API returns a "rows" array; each row has a "startTime" and "metrics".
        // We grab the most recent row's "crashRate" value and invert it.
        val metricRegex = """"decimalValue"\s*:\s*"?([\d.]+)"?""".toRegex()
        val rawRate = metricRegex.find(response)?.groupValues?.get(1)?.toDoubleOrNull()

        if (rawRate != null) {
            // The API returns crash rate (e.g. 0.001 = 0.1% crash rate)
            // so crash-FREE rate = (1 - crashRate) * 100
            val crashFreeRate = (1.0 - rawRate) * 100.0
            val formatted = "%.2f".format(crashFreeRate)
            println("Crash-free rate: $formatted%")

            val githubEnvPath = System.getenv("GITHUB_ENV")
            if (githubEnvPath != null) {
                File(githubEnvPath).appendText("CRASH_FREE_RATE=$formatted\n")
                println("Written to GITHUB_ENV")
            }
        } else {
            println("Could not parse crash rate from response:")
            println(response)
            // Don't fail the build — just skip the Slack step
            val githubEnvPath = System.getenv("GITHUB_ENV")
            if (githubEnvPath != null) {
                File(githubEnvPath).appendText("CRASH_FREE_RATE=unknown\n")
            }
        }
    } else {
        val error = errorStream.bufferedReader().readText()
        println("Error $responseCode: $error")
        throw RuntimeException("API call failed")
    }
}
