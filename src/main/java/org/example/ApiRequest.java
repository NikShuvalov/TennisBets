package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class ApiRequest {

    public static final String ATP_SERIES_TICKER = "KXATPMATCH";

    public static void main(String[] args) {
        String tickerEvent = "KXATPMATCH-26OCT10SHEGEA";

        String url = String.format("https://external-api.kalshi.com/trade-api/v2/events/%s", tickerEvent);
        performNetworkRequest(url);

        fetchEvents(ATP_SERIES_TICKER);
    }

//    public static void fetchSeriesData(String seriesTicker) {
//        String url = String.format("https://external-api.kalshi.com/trade-api/v2/series/%s", seriesTicker);
//        performNetworkRequest(url);
//    }

    public static void fetchEvents(String seriesTicker) {
        String url = String.format("https://external-api.kalshi.com/trade-api/v2/events?limit=200&series_ticker=%s", seriesTicker);
        performNetworkRequest(url);
        // The response from the events with the series-ticker param returns a list of results that have an event-ticker
        // for each tennis match that's coming up.
        // ToDo: Compile a list of event-tickers from this response
        // ToDo: Perform a network call for each of the event-ticker values using the below code
//        String url = String.format("https://external-api.kalshi.com/trade-api/v2/events/%s", tickerEvent);
//        performNetworkRequest(url);
        // ToDo: For each result from the events response, look at the "markets" array to find both "yes_sub_title" fields
        // which indicate Player name, as well as "last_price_dollars" for the cost of each player in the event
    }

    public static void performNetworkRequest(String url){
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code: " + response.statusCode());
            System.out.println("Response body:");
            System.out.println(response.body());
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
        }
    }

}