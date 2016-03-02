package org.search.crawl;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageProcessor {
	
	private MongoDatabase database = null;
	MongoCollection<Document> pages = null;
    MongoCollection<Document> index = null;
	MongoCollection<Document> stopWords = null;
	
	public PageProcessor() {
	}
	
	public PageProcessor(MongoDatabase db) {
		this.database = db;
		this.pages = database.getCollection("pages");
		index = database.getCollection("index");
        stopWords = database.getCollection("stopWords");
        
	}
	
	public void process(Page page){
		
		
		Document obj = extract(page);
		download(page);
		index(obj);
		
	}
	
	private Document extract(Page page) {
		
		String url = page.getWebURL().getURL();
		
		if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Map<String, String> metatags = htmlParseData.getMetaTags();
            
            String text = htmlParseData.getText();

            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            List<String> list = new ArrayList<String>();
            for (WebURL link : links) {
            	list.add(link.getURL());
            }

            String path = rawHTML(url, text);
            System.out.println("Path: " + path);

            Document obj = new Document();
            obj.append("_id", url.toLowerCase().hashCode());
            obj.append("url", url);
            obj.append("hash", url.toLowerCase().hashCode());
            obj.append("title", metatags.get("title"));
            obj.append("description", metatags.get("description"));
            obj.append("content-type", metatags.get("content-type"));
            obj.append("text", text);
            obj.append("links", list);
            obj.append("path", path);
            pages.insertOne(obj);

            // Save images from url
            getMediaFromUrl(url);

            return obj;
        }
	}
	
	public void download(Page page)
	{
		
	}
	
	private void index(Document doc) {
		
		String text = doc.getString("text");
		String url = doc.getString("url");
        HashSet<String> htmlTextSet = new HashSet();
        StringTokenizer tokenText = new StringTokenizer(text, " ");

        while(tokenText.hasMoreTokens()){
            htmlTextSet.add(tokenText.nextToken());
        }

//        for (String s : htmlTextSet) {
//            System.out.println(s);
//        }

        while (tokenStopWords.hasMoreTokens()){
            String token = tokenStopWords.nextToken().replaceAll("\\s","");

            if(htmlTextSet.contains(token)){
                htmlTextSet.remove(token);
                //System.out.println("removing" + token);
            }
        }

        for (String s : htmlTextSet) {
            Document obj = new Document();
            Document myDoc = index.find(eq("word", s)).first();

            if(myDoc == null){
                obj.append("word", s);
                index.insertOne(obj);
            }

            index.updateOne(eq("word", s), new Document("$push", new Document("urlHash", url.hashCode())));
            //System.out.println("adding: " + s + " to database");

        }
    }

}
