package com.misuki;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class JWEC {

    private static final int MAX_PAGES = 50;
    private static final Set<String> visited = new HashSet<>();
    private static final Map<String, Set<String>> invertedIndex = new HashMap<>();
    private static final Map<String, String[]> urlToText = new HashMap<String, String[]>();
    private static final Map<String, Double> query_tf_idf = new HashMap<>();
    private static final Map<String, Map<String, Double>> doc_tf_idf = new HashMap<>();
    private static final Map<String, Double> doc_scores = new HashMap<>();


    private String url;
    private String[] start_urls;

    public JWEC(String[] start_urls) {
        this.start_urls = start_urls;
    }

    public String[] start(){
        String[] visited_output = visited.toArray(new String[0]);

        for (String url : start_urls) {
            crawl(url);
        }

        return visited_output;
    }

    public void set_new_urls(String[] new_starts){
        this.start_urls = new_starts;
    }
    public Map<String, String[]> get_full_text(){
        return urlToText;
    }
    public Map<String, Set<String>> get_inverted_index(){
        return invertedIndex;
    }
    public Map<String, Double> rank_similarity(String query){
        calculate_tf_idf(query);
        cosine_similarity();

        Map<String, Double> rankedResults = new LinkedHashMap<>();
        doc_scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .forEach(entry -> {
                    rankedResults.put(entry.getKey(), entry.getValue());
                });
        System.out.println(doc_scores);
        return rankedResults;
    }


    private static boolean is_valid_doc(String url) {
        // Must be on en.wikipedia.org and under /wiki/
        if (!url.startsWith("https://en.wikipedia.org/wiki/")) return false;

        // Exclude administrative or non-article pages (those with colons, etc.)
        String title = url.substring("https://en.wikipedia.org/wiki/".length());
        return !title.contains(":") && !title.startsWith("Main_Page");
    }
    private void crawl(String url){
        this.url = url;
        crawl();
    }
    private void crawl() {
        if (!url.contains("en.wikipedia.org")) return;
        url = cleanUrl(url);
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
                nextUrl = cleanUrl(nextUrl);

                if (is_valid_doc(nextUrl) && !visited.contains(nextUrl)) {
                    crawl(nextUrl);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to crawl " + url + ": " + e.getMessage());
        }
    }

    private static String cleanUrl(String url) {
        try {
            URI uri = new URI(url);
            return new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    null,           // no query
                    null            // no fragment
            ).toString();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    private static String[] tokenize(String text){
        String[] all_words = text.toLowerCase().split("[\\s.,;:!?()\"'-]+");
        List<String> all_word_list = new ArrayList<>(Arrays.asList(all_words));

        for (String word : all_words){
            if (word.length() < 3)
                all_word_list.remove(word);
        }

        return all_word_list.toArray(new String[0]);
    }


    private static void calculate_tf_idf(String query){
        String[] query_words = tokenize(query);
        Map<String, Integer> query_tf = get_term_frequency(query_words);
        Map<String, Double> idf_map = new HashMap<>();


        int total_docs = visited.size();
        for(String term: invertedIndex.keySet()){
            int term_freq = invertedIndex.get(term).size();
            double idf = Math.log((double) total_docs / (1+ term_freq));
            idf_map.put(term, idf);
        }

        for (Map.Entry<String, String[]> entry : urlToText.entrySet()){
            Map<String, Integer> doc_tf = get_term_frequency(entry.getValue());
            int doc_len = entry.getValue().length;

            Map<String, Double> local_tf_idf = new HashMap<>();
            for(Map.Entry<String, Integer> tf_entry: doc_tf.entrySet()){
                double tf = (double) tf_entry.getValue() / doc_len;
                double idf = idf_map.getOrDefault(tf_entry.getKey(), Math.log((double) total_docs));
                local_tf_idf.put(tf_entry.getKey(), tf * idf);
            }
            doc_tf_idf.put(entry.getKey(), local_tf_idf);
        }

        for (Map.Entry<String, Integer> tf_entry : query_tf.entrySet()) {
            double tf = (double) tf_entry.getValue() / query_words.length;
            double idf = idf_map.getOrDefault(tf_entry.getKey(), Math.log((double) total_docs));
            query_tf_idf.put(tf_entry.getKey(), tf * idf);
        }
    }

    private static Map<String, Integer> get_term_frequency(String[] words){
        Map<String, Integer> term_freq = new HashMap<>();
        for (String term: words)
            term_freq.put(term, term_freq.getOrDefault(term, 0)+1);

        return term_freq;
    }

    private static void cosine_similarity(){
        for (Map.Entry<String, Map<String, Double>> tf_idf_entry : doc_tf_idf.entrySet()){
            String url = tf_idf_entry.getKey();
            Map<String, Double> doc_vec = tf_idf_entry.getValue();
            System.out.println("doc vec: "+doc_vec);

            double dotProduct = 0.0;
            double docMagnitude = 0.0;
            double queryMagnitude = 0.0;

            for (String term : query_tf_idf.keySet()) {
                double queryWeight = query_tf_idf.get(term);
                double docWeight = doc_vec.getOrDefault(term, 0.0);
                dotProduct += queryWeight * docWeight;
                queryMagnitude += queryWeight * queryWeight;
            }

            for (double docWeight : doc_vec.values()) {
                docMagnitude += docWeight * docWeight;
            }

            queryMagnitude = Math.sqrt(queryMagnitude);
            docMagnitude = Math.sqrt(docMagnitude);


            if (docMagnitude != 0 && queryMagnitude != 0) {
                double cosineSimilarity = dotProduct / (docMagnitude * queryMagnitude);
                doc_scores.put(url, cosineSimilarity);
            }

        }

    }
}

