package org.search.crawl;

import static com.mongodb.client.model.Filters.eq;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;


public class Crawler extends WebCrawler{
	
	MongoClient mongoClient = null;
    MongoDatabase database = null;
    MongoCollection<Document> documents = null;
    MongoCollection<Document> index = null;
	MongoCollection<Document> stopWords = null;
    StringTokenizer tokenStopWords;
    PageProcessor pageProcessor = null;

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");
	
	@Override
	public void onStart() {
		mongoClient = new MongoClient( "localhost" , 27017 );
		database = mongoClient.getDatabase("local");
		
		documents = database.getCollection("document");
        stopWords = database.getCollection("stopWords");

        Document words = stopWords.find().first();
        tokenStopWords = new StringTokenizer(words.get("words").toString(), ",");
        
        pageProcessor = new PageProcessor(database);
	}
	
	@Override
	public WebURL handleUrlBeforeProcess(WebURL curURL) {
		//TODO: handle the :80 here

        if(curURL.getURL().endsWith("pdf")){
//            saveImage(curURL.getURL(), "pdf");
//            String fileName = curURL.getURL().substring(curURL.getURL().lastIndexOf("/"));
//            extractPDF("pdf/" + fileName);
        }

	    return curURL;
	}
	
	@Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
		
        Integer docId = url.getURL().toLowerCase().hashCode();
        return documents.find(eq("_id", docId)).first() == null;

    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
    	
    	String url = page.getWebURL().getURL();
    	System.out.println("Visiting URL: " + url);
    	
        pageProcessor.process(page);
    }
    
    @Override
    public void onBeforeExit() {
    	mongoClient.close();
    }

}