package org.search.rank;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Indexer {
	
	MongoClient mongoClient = null;
    MongoDatabase database = null;
    MongoCollection<Document> index = null;
    MongoCollection<Document> pages = null;
	MongoCollection<Document> stopWords = null;
    StringTokenizer tokenStopWords;
    List<String> stopWordsList = null;
	
	public Indexer()
	{
		mongoClient = new MongoClient( "localhost" , 27017 );
		database = mongoClient.getDatabase("local");
		stopWords = database.getCollection("stopWords");
		index = database.getCollection("index");
		pages = database.getCollection("pages");
		stopWordsList = new ArrayList<String>();
        Document words = stopWords.find().first();
        tokenStopWords = new StringTokenizer(words.get("words").toString(), ",");
        
	}
	
	public void index() {
		
		// "term" : [ "url1" : [12, 13, 24], "url2" : [1,2,3] ]
		
		
//		while (tokenStopWords.hasMoreTokens()){
//          String token = tokenStopWords.nextToken().replaceAll("\\s","");
//
//          stopWordsList.add(token);
//      }
//		
//		//fetch from mongo
//        Map<String, Map<String, List<Integer>>> indexMap = new HashMap<>();
//        String[] terms = text.split("\\s+");
//        System.out.println("terms: " + terms);
//
//        for(int i = 0 ; i < terms.length; i++)
//        {
//        	String term = terms[i];
//        	//skip term if it is a stop word 
//        	if(stopWordsList.contains(term))
//        	{
//        		System.out.println("skipping: " + term);
//        		continue;
//        	}
//        	
//        	Map termInfo = indexMap.get(term);
//        	if(termInfo == null)
//        	{
//        		Map map = new HashMap<String, List<Integer>>();
//        		List<Integer> positions = new ArrayList<>();
//        		positions.add(i);
//        		map.put(url.getFileName().hashCode(), positions);
//        		indexMap.put(term, map);
//        		
//        	}
//        	
//        	RankInfo info = new RankInfo();
//        	//Set<RankInfo> set = indexMap.get(term);
//        	//set.add(info);
//        	//termInfo.put(term,  );
//        	
//        }

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

}
