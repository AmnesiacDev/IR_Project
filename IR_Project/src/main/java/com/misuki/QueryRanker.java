package com.misuki;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

/**
 * This class is where the Query is processed by being Tokenized, Calculate TF and IDF and then calculate Cosine Similarity
 */
public class QueryRanker {

    private final TFIDFCalculator tfidf;

    public QueryRanker(TFIDFCalculator tfidf) {
        this.tfidf = tfidf;
    }

    /**
     * Method that calculates TF-IDF of given query then gets term Cosine Similarity between query and the extracted documents
     * then ranks the most relevant documents to the query in order
     * @param query
     * @return Map<String URL, Double Cosine score>
     */
    public Map<String, Double> rank(String query) {
        System.out.println(query);
        String[] words = tokenize(query);
        Map<String, Integer> tf = getTermFrequency(words);
        Map<String, Double> idfMap = tfidf.getIdfMap();

        // Calculate TF-IDF of the query
        Map<String, Double> query_vec = new HashMap<>();
        for (Map.Entry<String, Integer> tfEntry : tf.entrySet()) {
            double tfVal = (double) tfEntry.getValue() / words.length;
            double idf = idfMap.getOrDefault(tfEntry.getKey(), Math.log((double) tfidf.getDocVectors().size()));
            query_vec.put(tfEntry.getKey(), tfVal * idf);
        }

        // Cosine Similarity Cs(x, y) = x . y / ||x|| ×× ||y||
        Map<String, Double> scores = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : tfidf.getDocVectors().entrySet()) {
            String url = entry.getKey();
            Map<String, Double> docVec = entry.getValue();

            double dot = 0, docMag = 0, queryMag = 0;
            for (String term : query_vec.keySet()) {
                double q = query_vec.get(term);
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

        // Order the "scores" Vector in descending order, limit of 10
        return scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
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
