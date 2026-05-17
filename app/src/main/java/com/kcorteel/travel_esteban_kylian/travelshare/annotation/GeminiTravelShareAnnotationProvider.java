package com.kcorteel.travel_esteban_kylian.travelshare.annotation;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.kcorteel.travel_esteban_kylian.BuildConfig;
import com.kcorteel.travel_esteban_kylian.travelshare.model.PlaceType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeminiTravelShareAnnotationProvider implements TravelShareAnnotationProvider {

    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final int IMAGE_LIMIT_BYTES = 900_000;
    private static final String IMAGE_MIME_TYPE = "image/jpeg";

    private final Context appContext;

    public GeminiTravelShareAnnotationProvider(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public AnnotationSuggestion generateSuggestion(
            Uri imageUri,
            String title,
            String description,
            String placeName,
            String city,
            String country,
            PlaceType placeType
    ) throws Exception {
        String model = TextUtils.isEmpty(BuildConfig.GEMINI_MODEL)
                ? "gemini-2.5-flash"
                : BuildConfig.GEMINI_MODEL;
        String apiUrl = API_BASE_URL
                + URLEncoder.encode(model, StandardCharsets.UTF_8.name())
                + ":generateContent?key="
                + URLEncoder.encode(BuildConfig.GEMINI_API_KEY, StandardCharsets.UTF_8.name());

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            JSONObject payload = buildPayload(
                    imageUri,
                    title,
                    description,
                    placeName,
                    city,
                    country,
                    placeType
            );

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = readAll(inputStream);

            if (responseCode < 200 || responseCode >= 300) {
                String apiMessage = extractApiErrorMessage(responseBody);
                String message = TextUtils.isEmpty(apiMessage)
                        ? "HTTP " + responseCode
                        : "HTTP " + responseCode + " - " + apiMessage;
                throw new IllegalStateException("Gemini API error: " + message);
            }

            return parseSuggestion(responseBody);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public boolean isConfigured() {
        return !TextUtils.isEmpty(BuildConfig.GEMINI_API_KEY);
    }

    private JSONObject buildPayload(
            Uri imageUri,
            String title,
            String description,
            String placeName,
            String city,
            String country,
            PlaceType placeType
    ) throws Exception {
        JSONObject payload = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");

        JSONArray parts = new JSONArray();
        parts.put(new JSONObject().put("text", buildPrompt(
                title,
                description,
                placeName,
                city,
                country,
                placeType
        )));

        if (imageUri != null) {
            byte[] imageBytes = readImageBytes(imageUri);
            if (imageBytes.length > 0) {
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", IMAGE_MIME_TYPE);
                inlineData.put("data", Base64.encodeToString(imageBytes, Base64.NO_WRAP));
                parts.put(new JSONObject().put("inline_data", inlineData));
            }
        }

        userContent.put("parts", parts);
        contents.put(userContent);
        payload.put("contents", contents);

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.5);
        generationConfig.put("response_mime_type", "application/json");
        payload.put("generationConfig", generationConfig);

        return payload;
    }

    private String buildPrompt(
            String title,
            String description,
            String placeName,
            String city,
            String country,
            PlaceType placeType
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("Tu es un assistant éditorial pour une application Android de voyage. ");
        builder.append("Analyse l'image si elle est fournie, ainsi que les informations textuelles. ");
        builder.append("Retourne uniquement un objet JSON valide sans markdown ni texte additionnel. ");
        builder.append("Le JSON doit avoir exactement deux clés: summary et tags. ");
        builder.append("summary doit contenir un résumé naturel, inspirant, concis, en français, sans hashtags, en deux phrases maximum et moins de 180 caractères. ");
        builder.append("tags doit contenir entre 3 et 6 tags utiles pour la découverte d'une photo de voyage. ");
        builder.append("Les tags doivent être de simples mots ou courtes expressions, sans #.\n\n");
        builder.append("Titre: ").append(safeValue(title)).append('\n');
        builder.append("Description: ").append(safeValue(description)).append('\n');
        builder.append("Lieu: ").append(safeValue(placeName)).append('\n');
        builder.append("Ville: ").append(safeValue(city)).append('\n');
        builder.append("Pays: ").append(safeValue(country)).append('\n');
        builder.append("Type de lieu: ").append(placeType == null ? "OTHER" : placeType.name()).append('\n');
        builder.append("\nExemple de format attendu:\n");
        builder.append("{\"summary\":\"Balade lumineuse entre ruelles et façades colorées.\",\"tags\":[\"ville\",\"architecture\",\"week-end\"]}");
        return builder.toString();
    }

    private AnnotationSuggestion parseSuggestion(String responseBody) throws Exception {
        JSONObject root = new JSONObject(responseBody);
        JSONArray candidates = root.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            throw new IllegalStateException("Réponse Gemini inattendue.");
        }

        String jsonText = "";
        for (int i = 0; i < candidates.length() && jsonText.isEmpty(); i++) {
            JSONObject candidate = candidates.optJSONObject(i);
            if (candidate == null) {
                continue;
            }

            JSONObject content = candidate.optJSONObject("content");
            if (content == null) {
                continue;
            }

            JSONArray parts = content.optJSONArray("parts");
            if (parts == null) {
                continue;
            }

            for (int j = 0; j < parts.length() && jsonText.isEmpty(); j++) {
                JSONObject part = parts.optJSONObject(j);
                if (part == null) {
                    continue;
                }
                jsonText = part.optString("text", "").trim();
            }
        }

        if (jsonText.isEmpty()) {
            throw new IllegalStateException("Aucune annotation générée par Gemini.");
        }

        JSONObject suggestionJson = new JSONObject(stripMarkdownFences(jsonText));
        String summary = suggestionJson.optString("summary", "").trim();
        JSONArray tagsArray = suggestionJson.optJSONArray("tags");
        List<String> tags = new ArrayList<>();
        if (tagsArray != null) {
            for (int i = 0; i < tagsArray.length(); i++) {
                String tag = tagsArray.optString(i, "").trim();
                if (!tag.isEmpty()) {
                    tags.add(tag);
                }
            }
        }

        return new AnnotationSuggestion(summary, tags);
    }

    private String extractApiErrorMessage(String responseBody) {
        if (TextUtils.isEmpty(responseBody)) {
            return "";
        }

        try {
            JSONObject root = new JSONObject(responseBody);
            JSONObject error = root.optJSONObject("error");
            if (error == null) {
                return "";
            }
            return error.optString("message", "").trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private byte[] readImageBytes(Uri imageUri) throws Exception {
        ContentResolver resolver = appContext.getContentResolver();
        try (InputStream inputStream = resolver.openInputStream(imageUri)) {
            if (inputStream == null) {
                return new byte[0];
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                return new byte[0];
            }

            int quality = 88;
            byte[] bytes;
            do {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                bytes = outputStream.toByteArray();
                quality -= 12;
            } while (bytes.length > IMAGE_LIMIT_BYTES && quality >= 40);

            return bytes;
        }
    }

    private String readAll(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        try (InputStream stream = inputStream;
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }

    private String stripMarkdownFences(String value) {
        String sanitized = value.trim();
        if (sanitized.startsWith("```")) {
            sanitized = sanitized.replaceFirst("^```(?:json)?\\s*", "");
            sanitized = sanitized.replaceFirst("\\s*```$", "");
        }
        return sanitized.trim();
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}
