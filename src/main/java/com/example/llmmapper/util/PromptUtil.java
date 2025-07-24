package com.example.llmmapper.util;

public class PromptUtil {
    public static String getPrompt(String text) {
        return String.format("""
        You are a data assistant. From the following buysheet data:
        %s

        Extract **all** entries from the following data, not just the first few.
        - \"Release Date\": use 'In DC Date'
        - \"Season\": extract from either 'Cost Folio Season' or text like "'25-SPRING'" in 'Comment'

        Return an array of JSON objects with keys: \"Release Date\", \"Season\"
        Do not explain anything, return only JSON array.
        """, text);
    }
} 