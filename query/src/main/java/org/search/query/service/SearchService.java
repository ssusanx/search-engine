package org.search.query.service;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


/**
 * This class handles the search queries 
 * @author susansun
 *
 */
@Service
public class SearchService {
	
	
	private static MongoClient mongoClient = null;
	private static MongoDatabase database = null;
	private static MongoCollection<Document> index = null;
	private static MongoCollection<Document> pages = null;
	
	@PostConstruct
	public void setup()
	{
		mongoClient = new MongoClient( "localhost" , 27017 );
		database = mongoClient.getDatabase("local");
		index = database.getCollection("index");
		pages = database.getCollection("pages");
	}
	
	public List<String> find(String term)
	{
		
		List<String> results = new ArrayList<String>(); 
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Document doc = index.find(eq("term", term)).first();
		
		if(doc != null)
		{
			Map<String, Object> map = (Map<String, Object>) doc.get("docIds");
			
			for(String key : map.keySet())
			{
				System.out.println("key " + key);
				Document page = pages.find(eq("_id", Integer.parseInt(key))).first();
				if(page != null)
				{
					results.add(page.getString("url"));
					System.out.println("url" + page.getString("url"));
				}
			}
			
			System.out.println(gson.toJson(doc));
		}
            
            
            
//            try {
//                while (cursor.hasNext()) {
//                	//String json = gson.toJson(obj);
//                    System.out.println(gson.toJson(cursor.next()));
//                }
//            } finally {
//                cursor.close();
//            }
		
    	String result = gson.toJson(doc);
    	
		return results;
		
	}
	
	@PreDestroy
	public void destroy()
	{
		mongoClient.close();
	}

}
