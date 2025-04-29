package com.misuki;

import java.util.*;

public class TFIDFCalculator {
    private final Map<String, Map<String, Double>> docVectors = new HashMap<>();
    private final Map<String, Double> idfMap = new HashMap<>();

    public void build(Map<String, String[]> documents) {
        int totalDocs = documents.size();
        Map<String, Integer> docFreq = new HashMap<>();

        // Compute document frequency
        for (String[] doc : documents.values()) {
            Set<String> unique = new HashSet<>(Arrays.asList(doc));
            for (String term : unique) {
                docFreq.put(term, docFreq.getOrDefault(term, 0) + 1);
            }
        }

        // Compute IDF
        for (Map.Entry<String, Integer> entry : docFreq.entrySet()) {
            idfMap.put(entry.getKey(), Math.log((double) totalDocs / (1 + entry.getValue())));
        }

        // Compute TF-IDF vectors
        for (Map.Entry<String, String[]> entry : documents.entrySet()) {
            String url = entry.getKey();
            String[] words = entry.getValue();
            Map<String, Integer> tf = getTermFrequency(words);

            Map<String, Double> vector = new HashMap<>();
            for (Map.Entry<String, Integer> tfEntry : tf.entrySet()) {
                double tfVal = (double) tfEntry.getValue() / words.length;
                double idf = idfMap.getOrDefault(tfEntry.getKey(), Math.log((double) totalDocs));
                vector.put(tfEntry.getKey(), tfVal * idf);
            }

            docVectors.put(url, vector);
        }
    }

    private Map<String, Integer> getTermFrequency(String[] words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words)
            freq.put(word, freq.getOrDefault(word, 0) + 1);
        return freq;
    }

    public Map<String, Map<String, Double>> getDocVectors() {
        return docVectors;
    }

    public Map<String, Double> getIdfMap() {
        return idfMap;
    }
}
