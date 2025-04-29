package com.misuki;


import java.util.Map;

public class App
{

    public static void main( String[] args )
    {
        System.out.println("hello");
        String[] startUrls = {
                "https://en.wikipedia.org/wiki/List_of_pharaohs",
                "https://en.wikipedia.org/wiki/Pharaoh"
        };

        JWEC crawler = new JWEC(startUrls);
        String[] visited_output = crawler.start();

        for (String s : visited_output) {
            System.out.println(s);
        }

        String query = "who is the first pharaoh of egypt hello world get me a good entry please?";
        for (Map.Entry<String, Double> tfentry : crawler.rank_similarity(query).entrySet()){
            System.out.println("key: "+tfentry.getKey()+" | value: "+tfentry.getValue());
        }



        /*
        System.out.println("\nInverted Index Statistics:");
        System.out.println("Total unique words: " + invertedIndex.size());
        System.out.println("Total documents indexed: " + visited.size());

        // Print sample of the inverted index (first 20 entries)
        System.out.println("\nSample of Inverted Index (first 20 entries):");
        int count = 0;
        for (Map.Entry<String, Set<String>> entry : invertedIndex.entrySet()) {
            if (count++ >= 20) break;
            System.out.println(entry.getKey() + ": " + entry.getValue().size() + " documents");
        }

         */


    }










/*
    public static void crawl(String url) {
        if (!url.contains("wikipedia.org")) return;
        url = cleanUrl(url);
        if (visited.contains(url) || visited.size() >= MAX_PAGES) return;

        System.out.println("Crawling: " + url);
        visited.add(url);

        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String nextUrl = link.absUrl("href");
                nextUrl = cleanUrl(nextUrl);

                if (nextUrl.contains("wikipedia.org") && !visited.contains(nextUrl)) {
                    crawl(nextUrl);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to crawl " + url + ": " + e.getMessage());
        }
    }


    public static String cleanUrl(String url) {
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


    private static void indexDocument(String url, String text) {
        // Simple tokenization - split on whitespace and punctuation
        String[] words = text.toLowerCase().split("[\\s.,;:!?()\"'-]+");

        for (String word : words) {
            if (word.length() < 3) continue; // skip stop_words

            // Update the inverted index
            invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(url);
        }
    }
*/

}
