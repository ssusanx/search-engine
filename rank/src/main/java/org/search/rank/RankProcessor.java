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
import com.mongodb.client.MongoCursor;

import java.text.DecimalFormat;

import org.bson.codecs.DoubleCodec;

import java.text.DecimalFormat;
import java.util.StringTokenizer;
import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.mongodb.client.model.Filters.eq;

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
    	Files.walk(Paths.get("C:\\Users\\jwj96\\Downloads\\en\\articles")).forEach(filePath -> {
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
					saveDocument(bodyHandler.toString(), filePath, metadata, linkHandler.getLinks());
					index(bodyHandler.toString(), filePath);
					
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

        Document doc = pages.find(eq("_id", filePath.getFileName().toString().hashCode())).first();
        if(doc == null) {
            Document obj = new Document();
            obj.append("_id", filePath.getFileName().toString().hashCode());
            obj.append("url", filePath.getFileName().toString());
            obj.append("title", metadata.get("title"));
            obj.append("description", metadata.get("description"));
            obj.append("content-type", metadata.get("content-type"));
            obj.append("text", text);
            obj.append("links", list);
            obj.append("path", "");
            obj.append("outLinks", list.size());
            obj.append("inLinks", 0);
            obj.append("rank", 0.33);
            obj.append("currentRank", 0.33);

            pages.insertOne(obj);
        } else {
                pages.updateOne(new Document("_id", filePath.getFileName().toString().hashCode()),
                        new Document("$set", new Document("title", metadata.get("title"))));
                pages.updateOne(new Document("_id", filePath.getFileName().toString().hashCode()),
                        new Document("$set", new Document("description", metadata.get("description"))));
                pages.updateOne(new Document("_id", filePath.getFileName().toString().hashCode()),
                        new Document("$set", new Document("content-type", metadata.get("content-type"))));
                pages.updateOne(new Document("_id", filePath.getFileName().toString().hashCode()),
                        new Document("$set", new Document("text", text)));
                pages.updateOne(new Document("_id", filePath.getFileName().toString().hashCode()),
                        new Document("$set", new Document("links", list)));
                pages.updateOne(new Document("_id", filePath.getFileName().toString().hashCode()),
                        new Document("$set", new Document("outLinks", links.size())));
            }

        incomingLinks(filePath.getFileName().toString(), list);

    }

	//this method is similar to the tokenizing method in the crawler
	public void index(String text, Path filePath) {
		
		while (tokenStopWords.hasMoreTokens())
		{
          String token = tokenStopWords.nextToken().replaceAll("\\s","");

          stopWordsList.add(token);
		}
		
		//split on non alphanumeric character to remove punctuations for now 
        String[] terms = text.split("\\W+");
        
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
        	Term doc = (Term) datastore.createQuery(Term.class).field("term").equal(term).get();
        	
            if(doc == null)
            {
            	doc = new Term();
            	doc.setTerm(term);
            }
            
            doc.addDocId(id);
        	datastore.save(doc);
        	
        }

    }
	
	/**
	 * calculates the ranking of a term
	 * 
	 */
	public void rank() {
		List<Term> terms = datastore.createQuery(Term.class).asList();
		for(Term term : terms)
		{
			calculateTfIdf(term);
			//link analysis
		}
	}

    public void linkAnalysis(){
        linkRank();
    }

    public void resetRank(){
        reset();
    }
	
	private void calculateTfIdf(Term term)
	{
		for(Integer docId : term.getDocIds().keySet())
		{
			Rank doc = term.getDocIds().get(docId);
			double tf = (double)doc.getTf();
			double tf_tdf = tf/term.getDocIds().size();
			
			System.out.println("tf_tdf: " + tf_tdf);
			doc.setTfIdf( normalized(tf_tdf, 0, 1));
		}
	}
    
    private double normalized(double x, double min,  double max){
		if(Double.compare(min, max) == 0)
		{
		    return 0.5;
		}
			
		return (x - min)/(max - min);
    }

	/**
	 * calculates the link analysis
	 *
	 */

    private void incomingLinks(String url, List<String> links){
        //System.out.println("\n\n\n\n\n\n IncomingLinks");

        for(String link : links){
            int urlHash = link.hashCode();

            Document obj = new Document();
            Document doc = pages.find(eq("_id", urlHash)).first();

            if(doc == null){
                System.out.println(link + " Does not exist \n Creating entry");
                obj.append("_id", link.hashCode());
                obj.append("url", link);
                obj.append("inLinks", 1);
                obj.append("rank", 0.33);
                obj.append("currentRank", 0.33);
                pages.insertOne(obj);

            }else {
                int incomingLinks = ((Number) doc.get("inLinks")).intValue() + 1;
                System.out.println("Total sites linking to " + link + ": " + incomingLinks);
                System.out.println("Total InLinks: " + incomingLinks);

                pages.updateOne(eq("hash", urlHash), new Document("$set", new Document("inLinks", incomingLinks)));
            }
            pages.updateOne(new Document("_id", urlHash),
                    new Document("$push", new Document("incomingLinks", url)));
        }
    }

    // Use to reset link rank
	private static void reset(){
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		MongoDatabase database = mongoClient.getDatabase("local");
		MongoCollection<org.bson.Document> pages = database.getCollection("pages");

		MongoCursor<Document> cursor = pages.find().iterator();

		double rank = 1.0 / pages.count();
        System.out.println(rank);
        try {
			while(cursor.hasNext()){
				Document obj = cursor.next();

				pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("rank", rank)));
				pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("currentRank", rank )));
			}
		}finally {

		}
	}

	private static void linkRank(){
		MongoCursor<Document> cursor;
        int run = 0;
		double totalDone = 0;
		long collectionSize = pages.count();
		System.out.println("Before loop: " + (totalDone / collectionSize));
		while(!((totalDone / collectionSize) > 0.75)){
		//for(int i = 0; i < 3; i++){
            totalDone = 0;
			cursor = pages.find().iterator();
			try{
				while(cursor.hasNext()) {
					Document obj = cursor.next();
					//System.out.println("\n\n\n\n" + obj.get("url") + "   " + obj.get("hash"));

					if(obj.get("incomingLinks") == null){
						continue;
					}

                    //System.out.println(obj.get("url") + ": " + obj.get("url").hashCode());
                    //System.out.println();
                    StringTokenizer st = new StringTokenizer(obj.get("incomingLinks").toString(), ",");
                    double total = 0;
					while (st.hasMoreTokens()) {
                        //System.out.println("Current rank: " + total);

						String url = st.nextToken().replace("[", "").replace("]", "").replace(" ", "");
                        //System.out.println(url.toLowerCase());
                        int hash = url.hashCode();
                        //System.out.println(hash);
                        //System.out.print("url: " + url + " hash: " + url.hashCode() + " ");
						Document doc = pages.find(eq("_id", hash)).first();
                        //System.out.println(hash + ": " + doc);

                        if(!(doc == null)){
                            double rank = (Double) doc.get("currentRank");

                            double links = ((Number) doc.get("outLinks")).doubleValue();
                            //System.out.print(total + " + " +  rank + " / " + links + " = ");
                            total += (rank / links);
                            //System.out.println(total);
                            // System.out.println("rank: " + (rank / links));
                        }
					}

					DecimalFormat df = new DecimalFormat("#.#########");
					total = Double.parseDouble(df.format(total));
					pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("rank", total)));

                    //System.out.println("Final Total: " + total);
					//System.out.println("Rank:" + obj.get("rank"));
					//System.out.println("Total links" + obj.get("outLinks"));
                }
                run += 1;
            } finally {
				cursor.close();
			}

            System.out.println("Total times ran: " + run);

				cursor = pages.find().iterator();
			try {
				while (cursor.hasNext()) {
					Document obj = cursor.next();
					if(obj.get("incomingLinks") == null){
						continue;
					}

					double oldRank = ((Number) obj.get("currentRank")).doubleValue();
					double newRank = ((Number) obj.get("rank")).doubleValue();
					//check if rank converge
					int compare = Double.compare(oldRank, newRank);

					if(compare == 0){
						totalDone++;
					}else{
						if(oldRank - newRank < 0.000001){
							//done = true;
							totalDone++;
							//System.out.println("\n\n\n\n\n\n total Done: " + totalDone);
						}
					}

					double currentRank = ((Number) obj.get("rank")).doubleValue();
					//double currentRank = ((Number) obj.get("rank")).doubleValue();
					pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("currentRank", currentRank)));
				}
			} finally {
				cursor.close();
			}

			System.out.println("percentage done: " +  (totalDone / collectionSize));
		}
	}
}
