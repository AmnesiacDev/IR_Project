package com.misuki;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class WikiCrawler {
    private static final Set<String> visited = new HashSet<>();
    private static final Map<String, Set<String>> invertedIndex = new HashMap<>();
    private static final Map<String, String[]> urlToText = new HashMap<String, String[]>();


    private final String[] start_urls;
    private String url;

    public static final int MAX_PAGES = 10;

    public WikiCrawler(String[] start_urls) {
        this.start_urls = start_urls;
    }

    public void start(){

        for (String url : start_urls) {
            crawl(url);
        }
    }

    private void crawl(String url){
        this.url = url;
        crawl();
    }
    private void crawl() {
        if (!url.contains("en.wikipedia.org")) return;
        url = clean_url(url);
        if (visited.contains(url) || visited.size() >= MAX_PAGES) return;

        System.out.println("Crawling: " + url);
        visited.add(url);

        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            Elements links = doc.select("a[href]");
            String text = doc.body().text();


            String[] clean_text = tokenize(text);
            for (String word : clean_text)
                invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(url);

            urlToText.put(url, clean_text);


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

    private static boolean is_valid_doc(String url) {
        if (!url.startsWith("https://en.wikipedia.org/wiki/")) return false;

        String title = url.substring("https://en.wikipedia.org/wiki/".length());
        return !title.contains(":") && !title.startsWith("Main_Page");
    }

    private String clean_url(String url) {
        try {
            URI uri = new URI(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

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
