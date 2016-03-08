package org.search.rank;

import static com.mongodb.client.model.Filters.eq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
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
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class RankProcessor {
	
	MongoClient mongoClient = null;
    MongoDatabase database = null;
    MongoCollection<Document> index = null;
	MongoCollection<Document> stopWords = null;
    StringTokenizer tokenStopWords;
	
	public RankProcessor()
	{
		mongoClient = new MongoClient( "localhost" , 27017 );
		database = mongoClient.getDatabase("local");
		stopWords = database.getCollection("stopWords");
		index = database.getCollection("index");

        Document words = stopWords.find().first();
        tokenStopWords = new StringTokenizer(words.get("words").toString(), ",");
	}
	
	
	
	public void index() throws IOException
    {
    	Files.walk(Paths.get("/Users/susansun/Downloads/wiki-small/articles")).forEach(filePath -> {
    	    if (Files.isRegularFile(filePath)) {
    	    	try {
    	    		ByteArrayInputStream content = new ByteArrayInputStream(Files.readAllBytes(filePath));
					
					BodyContentHandler handler = new BodyContentHandler();
					Metadata metadata = new Metadata();
					ParseContext pcontext = new ParseContext();
					  
					//Html parser 
					HtmlParser htmlparser = new HtmlParser();
					htmlparser.parse(content, handler, metadata,pcontext);
					System.out.println("Contents of the document:" + handler.toString());
					System.out.println("Metadata of the document:");
					
					index(handler.toString(), filePath.getFileName().toString());
					
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
	
	//this method is similar to the tokenizing method in the crawler
	private void index(String text, String url) {
		
        Map<String, Set<RankInfo>> indexMap = new HashMap<String, Set<RankInfo>>();
        String[] terms = text.split("\\s+");

        for(int i = 0 ; i < terms.length; i++)
        {
        	String term = terms[i];
        	Set<RankInfo> termInfo = indexMap.get(term);
        	if(termInfo == null)
        	{
        		indexMap.put(term, new HashSet<RankInfo>());
        		
        	}
        	
        	RankInfo info = new RankInfo();
        	Set<RankInfo> set = indexMap.get(term);
        	set.add(info);
        	//termInfo.put(term,  );
        	
        }

//        for (String s : htmlTextSet) {
//            System.out.println(s);
//        }

//        while (tokenStopWords.hasMoreTokens()){
//            String token = tokenStopWords.nextToken().replaceAll("\\s","");
//
//            if(htmlTextSet.contains(token)){
//                htmlTextSet.remove(token);
//                //System.out.println("removing" + token);
//            }
//        }
//
//        for (String s : htmlTextSet) {
//            Document obj = new Document();
//            Document myDoc = index.find(eq("word", s)).first();
//
//            if(myDoc == null){
//                obj.append("word", s);
//                index.insertOne(obj);
//            }
//
//            index.updateOne(eq("word", s), new Document("$push", new Document("urlHash", url.hashCode())));
//            //System.out.println("adding: " + s + " to database");
//
//        }
    }
    
    private double normalized(double x, double min,  double max){
	if(Double.compare(min, max) == 0){
	    return 0.5;
	}
		
	return (x - min)/(max - min);
    }
}
