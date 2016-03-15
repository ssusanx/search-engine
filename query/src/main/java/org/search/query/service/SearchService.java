package org.search.query.service;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public List<SearchResult> find(String term)
	{
		
		List<SearchResult> results = new ArrayList<SearchResult>(); 
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Document doc = index.find(eq("term", term)).first();
		
		if(doc != null)
		{
			List<Document> list = (ArrayList<Document>) doc.get("docIds");
			
			SearchResult result = null;
			for(Document rank : list)
			{
				System.out.println("key " + rank.get("docId"));
				Document page = pages.find(eq("_id", rank.get("docId"))).first();
				if(page != null)
				{
					result = new SearchResult();
					result.setLink(page.getString("path"));
					result.setTitle(page.getString("title"));
					results.add(result);
					System.out.println("url" + page.getString("path"));
				}
			}
			
		}
            
		return results;
		
	}
	
	@PreDestroy
	public void destroy()
	{
		mongoClient.close();
	}

}
