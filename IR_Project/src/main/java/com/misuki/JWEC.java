package com.misuki;

import java.util.*;

public class JWEC {

    private final WikiCrawler crawler;
    private final TFIDFCalculator tfidf;
    private final QueryRanker ranker;

    public JWEC(String[] startUrls) {
        this.crawler = new WikiCrawler(startUrls);
        this.tfidf = new TFIDFCalculator();
        this.ranker = new QueryRanker(tfidf);
    }

    public void start() {
        crawler.start();
        tfidf.build(crawler.getDocuments());
    }

    public Map<String, String[]> getFullText() {
        return crawler.getDocuments();
    }

    public Map<String, Set<String>> getInvertedIndex() {
        return crawler.getInvertedIndex();
    }

    public Map<String, Double> search(String query) {
        return ranker.rank(query);
    }

}

