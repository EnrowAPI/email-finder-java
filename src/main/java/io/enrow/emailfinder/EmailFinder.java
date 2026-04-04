package io.enrow.emailfinder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Lightweight wrapper around the Enrow email finder API.
 *
 * <p>All methods are static — no instantiation needed. Every call returns a
 * {@code Map<String, Object>} parsed from the JSON response.</p>
 */
public final class EmailFinder {

    private static final String BASE_URL = "https://api.enrow.io";
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private EmailFinder() {}

    /**
     * Start a single email-finder search.
     *
     * <p>Required key in {@code params}: {@code "fullname"}.
     * Optional keys: {@code "company_domain"}, {@code "company_name"}, {@code "custom"}, {@code "settings"}.
     * Settings may include {@code "country_code"}, {@code "retrieve_gender"}, {@code "webhook"}.</p>
     *
     * @param apiKey Enrow API key
     * @param params request body fields
     * @return parsed JSON response containing at least {@code "id"}
     */
    public static Map<String, Object> find(String apiKey, Map<String, Object> params) {
        return post(apiKey, "/email/find/single", params);
    }

    /**
     * Retrieve the result of a single email-finder search.
     *
     * @param apiKey Enrow API key
     * @param id     search ID returned by {@link #find}
     * @return parsed JSON response
     */
    public static Map<String, Object> get(String apiKey, String id) {
        return get(apiKey, "/email/find/single?id=" + id);
    }

    /**
     * Start a bulk email-finder search (up to 5,000 per batch).
     *
     * <p>Required key in {@code params}: {@code "searches"} (a list of maps, each
     * containing at least {@code "fullname"}).
     * Optional keys: {@code "settings"}.</p>
     *
     * @param apiKey Enrow API key
     * @param params request body fields
     * @return parsed JSON response containing at least {@code "batchId"}
     */
    public static Map<String, Object> findBulk(String apiKey, Map<String, Object> params) {
        return post(apiKey, "/email/find/bulk", params);
    }

    /**
     * Retrieve the results of a bulk email-finder search.
     *
     * @param apiKey Enrow API key
     * @param id     batch ID returned by {@link #findBulk}
     * @return parsed JSON response
     */
    public static Map<String, Object> getBulk(String apiKey, String id) {
        return get(apiKey, "/email/find/bulk?id=" + id);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private static Map<String, Object> post(String apiKey, String path, Map<String, Object> body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("x-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();
        return send(request);
    }

    private static Map<String, Object> get(String apiKey, String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("x-api-key", apiKey)
                .GET()
                .build();
        return send(request);
    }

    private static Map<String, Object> send(HttpRequest request) {
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> data = GSON.fromJson(response.body(), MAP_TYPE);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String message = data != null && data.containsKey("message")
                        ? String.valueOf(data.get("message"))
                        : "API error " + response.statusCode();
                throw new RuntimeException(message);
            }
            return data;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Request failed: " + e.getMessage(), e);
        }
    }
}
