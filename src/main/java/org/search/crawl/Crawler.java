package org.search.crawl;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Crawler {

    public static void main(String[] args) throws IOException
    {
    	System.out.println("Crawler started");
    	
    	
    	String url = "http://en.wikipedia.org/";
    	downloadPage(url);
    }

    private static void downloadPage(String url) throws IOException
    {
    	Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
    	Elements newsHeadlines = doc.select("#mp-itn b a");
    	
    	System.out.println(newsHeadlines.toString());
    }
}
