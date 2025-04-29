package com.misuki;


import java.util.Map;

public class App
{

    public static void main( String[] args )
    {
        System.out.println("hello world");
        String[] startUrls = {
                "https://en.wikipedia.org/wiki/List_of_pharaohs",
                "https://en.wikipedia.org/wiki/Pharaoh"
        };

        JWEC crawler = new JWEC(startUrls);
        crawler.start();


        String query = "who is the first pharaoh of egypt hello world get me a good entry please?";

        for (Map.Entry<String, Double> tfentry : crawler.search(query).entrySet()){
            System.out.println("key: "+tfentry.getKey()+" | value: "+tfentry.getValue());
        }



    }

}
