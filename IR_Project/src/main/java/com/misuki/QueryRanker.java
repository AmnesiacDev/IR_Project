package com.misuki;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

public class QueryRanker {

    private final TFIDFCalculator tfidf;

    public QueryRanker(TFIDFCalculator tfidf) {
        this.tfidf = tfidf;
    }

    public Map<String, Double> rank(String query) {
        System.out.println(query);
        String[] words = tokenize(query);
        Map<String, Integer> tf = getTermFrequency(words);
        Map<String, Double> idfMap = tfidf.getIdfMap();

        Map<String, Double> queryVec = new HashMap<>();
        for (Map.Entry<String, Integer> tfEntry : tf.entrySet()) {
            double tfVal = (double) tfEntry.getValue() / words.length;
            double idf = idfMap.getOrDefault(tfEntry.getKey(), Math.log((double) tfidf.getDocVectors().size()));
            queryVec.put(tfEntry.getKey(), tfVal * idf);
        }

        Map<String, Double> scores = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : tfidf.getDocVectors().entrySet()) {
            String url = entry.getKey();
            Map<String, Double> docVec = entry.getValue();

            double dot = 0, docMag = 0, queryMag = 0;
            for (String term : queryVec.keySet()) {
                double q = queryVec.get(term);
                double d = docVec.getOrDefault(term, 0.0);
                dot += q * d;
                queryMag += q * q;
            }

            for (double val : docVec.values()) {
                docMag += val * val;
            }

            double score = (queryMag != 0 && docMag != 0)
                    ? dot / (Math.sqrt(queryMag) * Math.sqrt(docMag))
                    : 0.0;
            scores.put(url, score);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    private String[] tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[\\s.,;:!?()\"'-]+"))
                .filter(word -> word.length() >= 3)
                .toArray(String[]::new);
    }

    private Map<String, Integer> getTermFrequency(String[] words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words)
            freq.put(word, freq.getOrDefault(word, 0) + 1);
        return freq;
    }
}
