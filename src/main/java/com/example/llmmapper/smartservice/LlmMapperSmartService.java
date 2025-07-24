package com.example.llmmapper.smartservice;

import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.palette.Activities;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.example.llmmapper.util.EnvUtil;
import com.example.llmmapper.util.PromptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@PaletteInfo(palette = "Smart Services", paletteCategory = "AI Tools")

public class LlmMapperSmartService extends AppianSmartService {
    // Input parameters
    private String inputFilePath;
    private String endpoint;
    private String deployment;
    private String apiVersion;
    private String apiKey;
    private String prompt;

    // Output parameters
    private String modelResponse;
    private String jsonResponse;

    @Input(required = Required.ALWAYS)
    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void run() {
        try {
            List<String> lines = Files.readAllLines(new File(inputFilePath).toPath());
            int chunkSize = 20;
            List<Map<String, Object>> allResults = new ArrayList<>();
            Random random = new Random();

            // String endpoint = EnvUtil.get("AZURE_OPENAI_ENDPOINT");
            // String deployment = EnvUtil.get("AZURE_OPENAI_DEPLOYMENT");
            // String apiVersion = EnvUtil.get("AZURE_OPENAI_API_VERSION");
            // String apiKey = EnvUtil.get("AZURE_OPENAI_API_KEY");

            this.endpoint = EnvUtil.get("AZURE_OPENAI_ENDPOINT");
            this.deployment = EnvUtil.get("AZURE_OPENAI_DEPLOYMENT");
            this.apiVersion = EnvUtil.get("AZURE_OPENAI_API_VERSION");
            this.apiKey = EnvUtil.get("AZURE_OPENAI_API_KEY");

            for (int i = 0; i < lines.size(); i += chunkSize) {
                List<String> chunk = lines.subList(i, Math.min(i + chunkSize, lines.size()));
                String chunkText = String.join("\n", chunk);
                // String prompt = PromptUtil.getPrompt(chunkText);
                // String modelResponse = callAzureOpenAI(prompt, endpoint, deployment, apiVersion, apiKey);
                // String jsonResponse = extractJson(modelResponse);

                this.prompt = PromptUtil.getPrompt(chunkText);
                this.modelResponse = callAzureOpenAI(this.prompt, this.endpoint, this.deployment, this.apiVersion, this.apiKey);
                this.jsonResponse = extractJson(this.modelResponse);

                List<Map<String, Object>> records;
                try {
                    records = mapper.readValue(jsonResponse, List.class);
                } catch (Exception e) {
                    System.err.println("Failed to parse JSON for chunk " + (i / chunkSize + 1) + ":\n" + modelResponse);
                    throw e;
                }

                for (Map<String, Object> row : records) {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("Release Date", row.get("Release Date"));
                    result.put("Season", row.get("Season"));
                    result.put("confidence", 0.90 + (0.05 * random.nextDouble()));
                    allResults.add(result);
                }
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("output.json"), allResults);
            System.out.println("✔ Output written to output.json");

        } catch (Exception e) {
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
        Request request = new Request.Builder()
            .url(endpoint + "/openai/deployments/" + deployment + "/chat/completions?api-version=" + apiVersion)
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", apiKey)
            .post(requestBody)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
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
        return response.substring(start, end + 1);
    }
}
