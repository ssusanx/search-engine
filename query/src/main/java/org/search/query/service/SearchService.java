package org.search.query.service;

import static com.mongodb.client.model.Filters.eq;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
		index = database.getCollection("term");
		pages = database.getCollection("document");
	}
	
	public List<SearchResult> find(String query)
	{
		List<SearchResult> results = new ArrayList<SearchResult>(); 
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String[] terms = query.split(" ");
		
		for(String term : terms)
		{
			
			//double tfIdf = calculateTfIdf(1.0/ terms.length, calculateIdf(term));
			Document doc = index.find(eq("term", term)).first();
			
			System.out.println("find term " + term);
			
			if(doc != null)
			{
				List<Document> list = (ArrayList<Document>) doc.get("docIds");
				
				SearchResult result = null;
				for(Document rank : list)
				{
					//System.out.println("key " + rank.get("docId"));
					Document page = pages.find(eq("_id", rank.get("docId"))).first();
					if(page != null)
					{
						result = new SearchResult();
						result.setLink(page.getString("url"));
						result.setTitle(page.getString("title"));
						result.setLinkAnalysis(page.getDouble("normalizedRank"));
						result.setTfidf(rank.getDouble("tfIdf"));
						results.add(result);
						
					}
				}
				
			}
		}
        
		double[] d = new double[results.size()];
		double[] q = new double[terms.length];
		
		for(int i= 0 ; i < results.size(); i++)
		{
			d[i] = results.get(i).getTfidf();
		}
		
		for(int i= 0 ; i < q.length; i++)
		{
			q[i] = calculateIdf(terms[i]);
		}
		
		for(SearchResult r : results)
		{
			//r.setSimilarity(sim(d, q));
		}
		
		weight(results);
		Collections.sort((List<SearchResult>) results);
		
		return results;
		
	}
	
	private void weight(List<SearchResult> docs)
	{
		for(SearchResult res : docs )
		{
			double weightedTfIdf = res.getTfidf() * (double)0.1;
			double weightedLink = res.getLinkAnalysis() * (double)0.9;
			double total = weightedTfIdf + weightedLink ;
			res.setTfidf(weightedTfIdf);
			res.setLinkAnalysis(weightedLink);
			
//			System.out.println("url" + res.getLink());
//			System.out.println("tfidf" + res.getTfidf());
//			System.out.println("linkAnalysis" + res.getLinkAnalysis());
//			System.out.println("score: " + total);
			
			res.setScore(total);
		}
		
	}
	
	@PreDestroy
	public void destroy()
	{
		mongoClient.close();
	}
	
	/**
	 * 
	 * @param d
	 * @param q
	 */
	public double calculateTfIdf(double tf, double idf)
	{
		return tf/idf;
	}
	
	public double calculateIdf(String term)
	{
		
		Document doc = index.find(eq("term", term)).first();
		List<Document> docIds = (List<Document>) doc.get("docIds");
		double idf = Math.log( (double)6048 / docIds.size());
		return idf;
	}

	/*
	* Vector Space
	*/
	public static double sim(double d[],double q[]){
		//numerator
		double numerator = numerator(d, q);
		System.out.println("Numerator: " + numerator);

		// magnitude of d
		double magD = magnitude(d);
		System.out.println("mag D:" + magD);
		// magnitude of q
		double magQ = magnitude(q);
		System.out.println("mag Q:" + magQ);

		double sim = numerator / (Math.sqrt(magD * magQ));
		DecimalFormat df = new DecimalFormat("#.#########");
		sim = Double.parseDouble(df.format(sim));

		System.out.println("Sim: " + sim);
		return sim;
	}

	/*
	 * Calculate magnitude of a vector
	 */
	public static double magnitude(double[] arr){
		double[] magArr = new double[arr.length];

		for(int i = 0; i < arr.length; i++){
			magArr[i] = Math.pow(arr[i], 2);
		}

		double mag = 0;
		for(int i = 0; i < arr.length; i++){
			mag += magArr[i];
		}

		DecimalFormat df = new DecimalFormat("#.#########");
		mag = Double.parseDouble(df.format(mag));

		return mag;
	}

	/*
	 * Calculate numerator
	 */
	public static double numerator(double[] vec, double[] vec2){
		int length = vec.length;

		// Store vec x vec2
		double[] arr = new double[length];

		// multiple D and Q
		for(int i = 0; i < length; i++){
			arr[i] = (vec[i] * vec2[i]);
		}

		double num = 0;
		for(int i = 0; i < length; i++){
			num += arr[i];
		}

		return num;
	}
}
