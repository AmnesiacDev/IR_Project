package org.example;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class App
{
    private static final int MAX_PAGES = 10;
    private static final Set<String> visited = new HashSet<>();

    public static void main( String[] args )
    {

        String[] startUrls = {
                "https://en.wikipedia.org/wiki/List_of_pharaohs",
                "https://en.wikipedia.org/wiki/Pharaoh"
        };

        for (String url : startUrls) {
            crawl(url);
        }
        String[] visited_output = visited.toArray(new String[0]);

        for (String s : visited_output) {
            System.out.println(s);
        }

    }




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
}
