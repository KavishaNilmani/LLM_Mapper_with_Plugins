package com.example.llmmapper.util;

public class PromptUtil {
    public static String getPrompt(String text) {
        return String.format("""
        You are a data assistant. From the following buysheet data:

        %s

        Extract all records. For each record:
        - "Release Date": use the value from the field 'In DC Date'
        - "Season": use the value from the field 'Cost Folio Season', or infer it from 'Comment' (e.g., "SPRING 2025", "FALL 2025")

        Return only a valid **JSON array** like this:
        [
          {"Release Date": "2025-09-12", "Season": "SPRING 2025"},
          ...
        ]

        Do not include explanations or markdown. Only output the JSON array.
        If no valid records exist, return: []
        """, text);
    }
} 