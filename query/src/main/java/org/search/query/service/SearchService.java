package org.search.query.service;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Collections;
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
		
		//Document doc = index.find(eq("term", term)).sort("doc");
		System.out.println("find term " + term);
		
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
					result.setLink(page.getString("url"));
					result.setTitle(page.getString("title"));
					result.setLinkAnalysis(page.getDouble("currentRank"));
					result.setTfidf(rank.getDouble("tfIdf"));
					results.add(result);
					
				}
			}
			
		}
        
		weight(results);
		Collections.sort((List<SearchResult>) results);
		
		return results;
		
	}
	
	private void weight(List<SearchResult> docs)
	{
		for(SearchResult res : docs )
		{
			double total = res.getTfidf() * (double)0.5 + res.getLinkAnalysis() * (double)0.5;
			res.setTfidf(res.getTfidf() * (double)0.5);
			res.setLinkAnalysis(res.getLinkAnalysis() * (double)0.5);
			System.out.println("url" + res.getLink());
			System.out.println("tfidf" + res.getTfidf());
			System.out.println("linkAnalysis" + res.getLinkAnalysis());
			System.out.println("score: " + total);
			res.setScore(total);
		}
		
	}
	
	@PreDestroy
	public void destroy()
	{
		mongoClient.close();
	}
	
}
