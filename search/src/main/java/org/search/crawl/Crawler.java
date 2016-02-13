package org.search.crawl;

import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler{
	HashMap<Integer, String> visitedSite = new HashMap<>();

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");
	
	@Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        boolean visited;
        visited = href.endsWith(":80:");
        //System.out.println("\n\nTESTING" + href + ": " + visited + "\n\n");
        return !visitedSite.containsKey(href.hashCode());

    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);
        visitedSite.put(url.hashCode(), url.toLowerCase());

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();


            //System.out.println("Links on current page:");
            //for (WebURL l: links) {
            //    System.out.println(l.toString());
            //}
            /*
            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());
            */
        }


        System.out.println("\n\n\nVisited Sites:");


        int counter = 0;
        for (Integer name: visitedSite.keySet()){
            counter += 1;
            String key = name.toString();
            String value = visitedSite.get(name).toString();
            System.out.println(counter + ": " + key + " " + value);
        }

        }
    }