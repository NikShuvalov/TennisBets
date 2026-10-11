package org.example

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

object ApiRequest {
    const val ATP_SERIES_TICKER: String = "KXATPMATCH"
    private const val BASE_URL = "https://external-api.kalshi.com/trade-api/v2"

    // One shared client instead of one per request
    private val CLIENT: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    @JvmStatic
    fun main(args: Array<String>) {
        fetchEvents(ATP_SERIES_TICKER)
    }

    /**
     * One call: open ATP events with their markets nested inline,
     * so no per-event follow-up requests are needed.
     */
    fun fetchEvents(seriesTicker: String?) {
        var cursor = ""
        var count = 0

        try {
            do {
                val url = (BASE_URL + "/events?limit=200"
                        + "&series_ticker=" + seriesTicker
                        + "&status=open"
                        + "&with_nested_markets=true"
                        + (if (cursor.isEmpty()) "" else "&cursor=" + URLEncoder.encode(
                    cursor,
                    StandardCharsets.UTF_8
                )))

                val root = JsonParser.parseString(get(url)).getAsJsonObject()

                val events = root.getAsJsonArray("events")
                for (el in events) {
                    printEvent(el.getAsJsonObject())
                    count++
                }

                cursor = if (root.has("cursor") && !root.get("cursor").isJsonNull())
                    root.get("cursor").getAsString()
                else
                    ""
            } while (!cursor.isEmpty())

            println("\nTotal open events: " + count)
        } catch (e: Exception) {
            System.err.println("Request failed: " + e.message)
        }
    }

    private fun printEvent(event: JsonObject) {
        val ticker = event.get("event_ticker").getAsString()
        val title = event.get("title").getAsString()

        if (!event.has("markets") || event.get("markets").isJsonNull()) {
            System.out.printf("%s | %s | (no markets in response)%n", ticker, title)
            return
        }

        val line = StringBuilder()
        for (m in event.getAsJsonArray("markets")) {
            val market = m.getAsJsonObject()
            val player = market.get("yes_sub_title").getAsString()
            // getAsDouble works whether Kalshi sends 0.62 or "0.6200"
            val price = if (market.has("last_price_dollars") && !market.get("last_price_dollars").isJsonNull())
                market.get("last_price_dollars").getAsDouble()
            else Double.NaN
            if (line.length > 0) line.append("  vs  ")
            line.append(String.format("%s ($%.2f)", player, price))
        }
        System.out.printf("%s | %s%n", ticker, line)
    }

    /** Returns the body on HTTP 200, throws otherwise.  */
    @Throws(Exception::class)
    fun get(url: String): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()

        val response = CLIENT.send<String?>(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw RuntimeException(
                ("HTTP " + response.statusCode() + " for " + url
                        + "\n" + response.body())
            )
        }
        return response.body()
    }
}