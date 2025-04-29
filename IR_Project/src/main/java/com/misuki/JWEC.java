package com.misuki;

import java.util.*;


/**
 * @Author AmnesiacDev
 * @Email Ahmed.gamal.ali120@gmail.com
 */


/**
 * This class is the main project class JWEC -> Java Wikipedia Web Crawler (using cosine similarity and inverted index)
 */
public class JWEC {

    private final WikiCrawler crawler;
    private final TFIDFCalculator tfidf;
    private final QueryRanker ranker;

    public JWEC(String[] startUrls) {
        this.crawler = new WikiCrawler(startUrls);
        this.tfidf = new TFIDFCalculator();
        this.ranker = new QueryRanker(tfidf);
    }

    /**
     * Start crawling and built the TF-IDF
     * Use @set_max_pages(int) to change number of pages to crawl
     */
    public void start() {
        crawler.start();
        tfidf.build(crawler.getDocuments());
    }

    /**
     * To return a Map of the extracted text
     * @return Map<String Page URL, String[] Full text extracted from page>
     */
    public Map<String, String[]> getFullText() {
        return crawler.getDocuments();
    }

    /**
     * To return the tokenized text in an InvertedIndex
     * @return Map<String Page URL, Set<String> InvertedIndex representation>
     */
    public Map<String, Set<String>> getInvertedIndex() {
        return crawler.getInvertedIndex();
    }

    /**
     * To search for the query items in the extracted data
     * @param query
     * @return Map<String URL, Double Cosine Similarity Score>
     */
    public Map<String, Double> search(String query) {
        return ranker.rank(query);
    }

    public int get_max_pages(){
        return crawler.get_max_pages();
    }
    public void set_max_pages(int max){
        crawler.set_max_pages(max);
    }

}

