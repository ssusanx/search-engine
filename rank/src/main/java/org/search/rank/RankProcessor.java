package org.search.rank;

import static com.mongodb.client.model.Filters.eq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.QueryImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class RankProcessor {
	
	private static MongoClient mongoClient = null;
	private static MongoDatabase database = null;
	private static MongoCollection<Document> index = null;
	private static MongoCollection<Document> pages = null;
	private static MongoCollection<Document> stopWords = null;
	private static StringTokenizer tokenStopWords;
	private static List<String> stopWordsList = null;
    private static Morphia morphia = null;

	// create the Datastore connecting to the default port on the local host
	Datastore datastore = null;
	
	
	public RankProcessor()
	{
		morphia = new Morphia();
		morphia.mapPackage("org.search.rank");
		mongoClient = new MongoClient( "localhost" , 27017 );
		datastore = morphia.createDatastore(mongoClient, "local");
		datastore.ensureIndexes();
		
		
		database = mongoClient.getDatabase("local");
		stopWords = database.getCollection("stopWords");
		index = database.getCollection("index");
		pages = database.getCollection("pages");
		stopWordsList = new ArrayList<String>();
        Document words = stopWords.find().first();
        tokenStopWords = new StringTokenizer(words.get("words").toString(), ",");
        
	}
	
	public void process() throws IOException
    {
    	Files.walk(Paths.get("/Users/susansun/Downloads/wiki-small/articles")).forEach(filePath -> {
    	    if (Files.isRegularFile(filePath)) {
    	    	try {
    	    		ByteArrayInputStream content = new ByteArrayInputStream(Files.readAllBytes(filePath));
					
					BodyContentHandler bodyHandler = new BodyContentHandler();
					LinkContentHandler linkHandler = new LinkContentHandler();
					
					TeeContentHandler teeHandler = new TeeContentHandler(linkHandler, bodyHandler);
					
					Metadata metadata = new Metadata();
					ParseContext pcontext = new ParseContext();
					  
					//Html parser 
					HtmlParser htmlparser = new HtmlParser();
					htmlparser.parse(content, teeHandler, metadata, pcontext);
					
					//here is a list of processing 
					//saveDocument(bodyHandler.toString(), filePath, metadata, linkHandler.getLinks());
					index(bodyHandler.toString(), filePath);
					//rank()
					
					String[] metadataNames = metadata.names();
					  
					for(String name : metadataNames) {
						System.out.println(name + ":   " + metadata.get(name));  
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    	});
    }
	
	/**
	 * saves the document to mongo
	 * @param text
	 * @param filePath
	 * @param metadata
	 * @param links
	 */
	private void saveDocument(String text, Path filePath, Metadata metadata, List<Link> links) 
	{
		List<String> list = new ArrayList<String>();
        for (Link link : links) {
        	String uri = link.getUri();
        	
        	//System.out.println("link before parse: " + uri);
        	if(!uri.isEmpty() && uri.startsWith("http"))
        	{
        		//list.add(uri.substring(uri.lastIndexOf("/") + 1, uri.length()-1));
        		//only add http outgoing links for now  
        		list.add(uri);
        	}
        }
		
		Document obj = new Document();
        obj.append("_id", filePath.getFileName().toString().hashCode());
        obj.append("url", filePath.getFileName().toString());
        obj.append("title", metadata.get("title"));
        obj.append("description", metadata.get("description"));
        obj.append("content-type", metadata.get("content-type"));
        obj.append("text", text);
        obj.append("links", list);
        obj.append("path", "");
        pages.insertOne(obj);
	}

	//this method is similar to the tokenizing method in the crawler
	public void index(String text, Path filePath) {
		
		while (tokenStopWords.hasMoreTokens())
		{
          String token = tokenStopWords.nextToken().replaceAll("\\s","");

          stopWordsList.add(token);
		}
		
        String[] terms = text.split("\\s+");
        
        for(int i = 0 ; i < terms.length; i++)
        {
        	//TODO: save i as position
        	String term = terms[i];
        	//skip term if it is a stop word 
        	if(stopWordsList.contains(term))
        	{
        		System.out.println("skipping: " + term);
        		continue;
        	}
        	
        	Integer id = filePath.getFileName().toString().hashCode();
        	
        	//fetch term from mongo
        	RankInfo doc = (RankInfo) datastore.createQuery(RankInfo.class).field("term").equal(term).get();
        	
            if(doc == null)
            {
            	doc = new RankInfo();
            	doc.setTerm(term);
            }
            
            doc.addDocId(id);
        	datastore.save(doc);
        	
        }

    }
	
	public void rank() {
		List<RankInfo> rankInfos = datastore.createQuery(RankInfo.class).asList();
		
	}
    
    private double normalized(double x, double min,  double max){
		if(Double.compare(min, max) == 0)
		{
		    return 0.5;
		}
			
		return (x - min)/(max - min);
    }

}
