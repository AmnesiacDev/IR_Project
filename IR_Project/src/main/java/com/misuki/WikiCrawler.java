package com.misuki;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Crawler class to crawl Wikipedia pages from a preset list of Wikipedia URLs then saves and then cleans the URLs and extracted data
 */
public class WikiCrawler {
    private static final Set<String> visited = new HashSet<>();
    private static final Map<String, Set<String>> invertedIndex = new HashMap<>();
    private static final Map<String, String[]> urlToText = new HashMap<String, String[]>();


    private final String[] start_urls;
    private String url;


    public static int MAX_PAGES = 50;

    public WikiCrawler(String[] start_urls) {
        this.start_urls = start_urls;
    }

    /**
     * Start Crawling like a spider
     */
    public void start(){

        for (String url : start_urls) {
            crawl(url);
        }
    }

    public void set_max_pages(int max){
        MAX_PAGES = max;
    }

    public int get_max_pages(){
        return MAX_PAGES;
    }

    /**
     * Overloaded function "Crawl" in order to cleanly recursively loop
     * @param url Next URL found
     */
    private void crawl(String url){
        this.url = url;
        crawl();
    }

    /**
     * Main Crawl method which recursively crawls onto English Wikipedia.org pages
     */
    private void crawl() {
        if (!url.contains("en.wikipedia.org")) return;
        url = clean_url(url);
        if (visited.contains(url) || visited.size() >= MAX_PAGES) return;

        System.out.println("Crawling: " + url);
        visited.add(url);
        // Uses Jsoup to connect establish http connection with the given url
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            Elements links = doc.select("a[href]");
            String text = doc.body().text();


            String[] clean_text = tokenize(text);
            for (String word : clean_text)
                invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(url);

            urlToText.put(url, clean_text);

            // Loops on the list of links and picks the first appropriate URL to go to next
            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                nextUrl = clean_url(nextUrl);

                if (is_valid_doc(nextUrl) && !visited.contains(nextUrl)) {
                    crawl(nextUrl);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to crawl " + url + ": " + e.getMessage());
        }
    }

    /**
     * Checks if the given URL is Metadata link
     * @param url
     * @return Boolean value
     */
    private static boolean is_valid_doc(String url) {
        if (!url.startsWith("https://en.wikipedia.org/wiki/")) return false;

        String title = url.substring("https://en.wikipedia.org/wiki/".length());
        return !title.contains(":") && !title.startsWith("Main_Page");
    }

    /**
     * Cleans the URL from any unwanted Strings or Symbols
     * @param url
     * @return Clean URL
     */
    private String clean_url(String url) {
        try {
            URI uri = new URI(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    /**
     * Tokenizes text by removing any symbols or filler words
     * @param text
     * @return Clean text ready to store in Inverted Index
     */
    private String[] tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[\\s.,;:!?()\"'-]+"))
                .filter(word -> word.length() >= 3)
                .toArray(String[]::new);
    }

    public Map<String, String[]> getDocuments() {
        return urlToText;
    }

    public Map<String, Set<String>> getInvertedIndex() {
        return invertedIndex;
    }
}
