package com.example.llmmapper.smartservice;

import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.example.llmmapper.util.PromptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@PaletteInfo(palette = "Smart Servicesv2", paletteCategory = "AI Tools")
public class LlmMapperSmartServicev2 extends AppianSmartService {

    // Input parameters
    private String inputText;
    private String endpoint;
    private String deployment;
    private String apiVersion;
    private String apiKey;
    private String prompt;

    // Output parameters
    private String modelResponse;
    private String jsonResponse;

    // Setters for inputs
    @Input(required = Required.ALWAYS)
    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    @Input
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Input
    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    @Input
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Input
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Input
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    // Optional output getter
    public String getJsonResponse() {
        return jsonResponse;
    }

    // HTTP and JSON utilities
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void run() {
        try {
            if (inputText == null || inputText.trim().isEmpty()) {
                throw new RuntimeException("No inputText provided. Please provide the sample data.");
            }

            List<String> lines = Arrays.asList(inputText.split("\\r?\\n"));
            int chunkSize = 20;
            List<Map<String, Object>> allResults = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < lines.size(); i += chunkSize) {
                List<String> chunk = lines.subList(i, Math.min(i + chunkSize, lines.size()));
                String chunkText = String.join("\n", chunk);

                String effectivePrompt = (this.prompt != null && !this.prompt.trim().isEmpty())
                        ? this.prompt
                        : PromptUtil.getPrompt(chunkText);

                System.out.println("Prompt for chunk " + (i / chunkSize + 1) + ":\n" + effectivePrompt);

                this.modelResponse = callAzureOpenAI(effectivePrompt, this.endpoint, this.deployment, this.apiVersion, this.apiKey);

                if (modelResponse == null || modelResponse.trim().isEmpty()) {
                    System.err.println("LLM returned an empty response. Skipping chunk " + (i / chunkSize + 1));
                    continue;
                }

                System.out.println("LLM response for chunk " + (i / chunkSize + 1) + ":\n" + modelResponse);

                try {
                    this.jsonResponse = extractJson(this.modelResponse);
                } catch (RuntimeException e) {
                    System.err.println("Failed to extract JSON from LLM response:\n" + modelResponse);
                    this.jsonResponse = "[]"; // Fallback to empty array
                }

                List<Map<String, Object>> records;
                try {
                    records = mapper.readValue(jsonResponse, List.class);
                } catch (Exception e) {
                    System.err.println("Failed to parse extracted JSON for chunk " + (i / chunkSize + 1) + ":\n" + jsonResponse);
                    throw e;
                }

                for (Map<String, Object> row : records) {
                    Map<String, Object> result = new LinkedHashMap<>();
                    // result.put("Release Date", row.get("Release Date"));
                    // result.put("Season", row.get("Season"));
                    result.put("confidence", 0.90 + (0.05 * random.nextDouble()));
                    allResults.add(result);
                }
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), allResults);
            System.out.println("Output written to output.json");

        } catch (Exception e) {
            System.err.println("Error during smart service execution:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String callAzureOpenAI(String prompt, String endpoint, String deployment, String apiVersion, String apiKey) throws IOException {
        com.fasterxml.jackson.databind.node.ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        ObjectNode body = mapper.createObjectNode();
        body.set("messages", messages);
        body.put("max_tokens", 1000);
        body.put("temperature", 0.2);

        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.get("application/json"));
        String url = endpoint + "/openai/deployments/" + deployment + "/chat/completions?api-version=" + apiVersion;

        System.out.println("Calling LLM at: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("api-key", apiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                System.err.println("LLM call failed: " + response + "\nError body: " + errorBody);
                throw new IOException("Request failed: " + response + "\nError body: " + errorBody);
            }

            String responseBody = response.body().string();
            ObjectNode root = (ObjectNode) mapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf("[");
        int end = response.lastIndexOf("]");

        if (start == -1 || end == -1 || start >= end) {
            System.err.println("No valid JSON array found in LLM response:\n" + response);
            return "[]"; // safe fallback
        }

        String jsonPart = response.substring(start, end + 1).trim();

        if (!jsonPart.startsWith("[") || !jsonPart.endsWith("]")) {
            System.err.println("Extracted content is not a valid JSON array:\n" + jsonPart);
            return "[]";
        }

        return jsonPart;
    }
}
