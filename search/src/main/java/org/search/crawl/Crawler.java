package org.search.crawl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler{
	HashMap<Integer, String> visitedSite = new HashMap<>();
	MongoClient mongoClient = null;
	MongoCollection<Document> collection = null;
	MongoDatabase database = null;
	boolean test = true
            ;
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");
	
	@Override
	public void onStart() {
		
		mongoClient = new MongoClient( "localhost" , 27017 );
		database = mongoClient.getDatabase("local");
		collection = database.getCollection("test");
		
	};
	
	@Override
	public WebURL handleUrlBeforeProcess(WebURL curURL) {
		//TODO: handle the :80 here 
	    return curURL;
	}
	
	@Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        boolean visited;
        visited = href.endsWith(":80:");
        //System.out.println("\n\nTESTING" + href + ": " + visited + "\n\n");
        return !visitedSite.containsKey(href.hashCode()) && !visited;

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
        
        Boolean isExtract = (Boolean)getMyController().getCustomData();
        if(isExtract)
        	extract(page);

        //System.out.println("\n\n\nVisited Sites:");

        int counter = 0;
        for (Integer name: visitedSite.keySet()){
            counter += 1;
            String key = name.toString();
            String value = visitedSite.get(name).toString();
            //System.out.println(counter + ": " + key + " " + value);
            System.out.println("count: " + counter);
        }

    }

	private void extract(Page page) {
		
		String url = page.getWebURL().getURL();
		
		if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Map<String, String> metatags = htmlParseData.getMetaTags();
            
            String text = htmlParseData.getText();

            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            List<String> list = new ArrayList<String>();
            for (WebURL link : links) {
            	list.add(link.getURL());
            }
            
            Document obj = new Document();
            obj.append("url", url);
            obj.append("hash", url.toLowerCase().hashCode());
            obj.append("title", metatags.get("title"));
            obj.append("description", metatags.get("description"));
            obj.append("content-type", metatags.get("content-type"));
            obj.append("text", text);
            obj.append("links", list);
            
            collection.insertOne(obj);

            rawHTML(url, text);

        }
		
	}

    private void rawHTML(String url, String text) {
        // Remove special characters that windows and OSX don't allow in file names
        url = url.replaceAll("[\\/:*?\"<>|.]*", "");
        //System.out.println("New URL: " + url.toLowerCase());

        createHtmlDirectory();

        File file = new File("html/" + url.toLowerCase()+".txt");
        
        if(file.exists()){
            // Maybe change it to check when the file was last updated and update file if older than n days.
            System.out.println("File exist");
        }

        try{
            PrintWriter out = new PrintWriter(file);
            out.print(text);
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error Writing to file");
            System.out.println("can write: " + file.canWrite());
            System.out.println("exist: " + file.exists());
            System.out.println("path: " + file.getAbsolutePath());
        }
    }

    private void createHtmlDirectory(){
        File dir = new File("html");
        if(!dir.exists()){
            System.out.println("Creating directory: " + dir.getAbsolutePath());
            dir.mkdir();
        }
    }

}