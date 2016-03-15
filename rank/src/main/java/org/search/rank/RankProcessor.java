package org.search.rank;

import static com.mongodb.client.model.Filters.eq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
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
import com.mongodb.client.model.Sorts;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
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
    private static HashMap<Integer, ArrayList<String>> linkMap;
    // create the Datastore connecting to the default port on the local host
	Datastore datastore = null;
	int t = 0;
	
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
        linkMap = new HashMap<Integer, ArrayList<String>>();
    }

    public void process() throws IOException
    {
        Files.walk(Paths.get("C:\\Users\\jwj96\\Downloads\\munged")).forEach(filePath -> {
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
                    //index2(bodyHandler.toString(), filePath);

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
		System.out.println(filePath.getFileName().toString());
		List<String> list = new ArrayList<String>();
        for (Link link : links) {
        	String uri = link.getUri();
			if(!uri.isEmpty() && !uri.startsWith("http") && !uri.startsWith("../") && uri.endsWith(".html"))
        	{
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
            obj.append("path", filePath.getFileName().toUri().toString());
            obj.append("outLinks", list.size());
            obj.append("inLinks", 0);
            obj.append("rank", 0);

            pages.insertOne(obj);

        }
    }

	
	
	public void index2(String text, Path filePath) {
		t += 1;
		System.out.println(t );
		
		System.out.println(filePath.getFileName().toString());
		
		while (tokenStopWords.hasMoreTokens())
		{
          String token = tokenStopWords.nextToken().replaceAll("\\s","");

          stopWordsList.add(token);
		}
		
		//term : doc, tfidf
		Map<String, Rank> map = new HashMap<String, Rank>();
		
		//split on non alphanumeric character to remove punctuations for now 
        String[] terms = text.split("\\W+");
        int termCount = terms.length;
        
        for(int i = 0 ; i < terms.length; i++)
        {
        	//TODO: save i as position
        	String term = terms[i].toLowerCase();
        	//skip term if it is a stop word 
        	if( term.isEmpty() || stopWordsList.contains(term) || stopWordsList.contains(term.toLowerCase()))
        	{
        		termCount--;
        		continue;
        	}
        	
        	Integer id = filePath.getFileName().toString().hashCode();
        	
        	Rank rank = map.get(term);
            if(rank == null)
            {
            	rank = new Rank();
            	rank.setDocId(id);
            	map.put(term, rank);
            }
            
            rank.setTf(rank.getTf() + 1);
        	
        }
        
        for(String word : map.keySet())
        {
        	Rank rank = map.get(word);
        	rank.setTotalTermCount(termCount);
        	//Term doc = (Term) datastore.createQuery(Term.class).field("term").equal(word).get();
        	
        	Document doc = index.find(eq("term", word)).first();
        	if(doc !=null)
        	{
        		index.updateOne(new Document("term", word),
            	        new Document("$push", new Document("docIds", new Document("docId", rank.getDocId())
            	        .append("tf", rank.getTf())
            	        .append("totalTermCount", termCount))));
        	}
        	else
        	{
        		Term term = new Term();
        		Set<Rank> ranks = new HashSet<Rank>();
        		ranks.add(rank);
        		term.setDocIds(ranks);
        		term.setTerm(word);
        		datastore.save(term);
        		
        	}
        	
        	//datastore.save(doc);
        	
        }
    }
	
	
	/**
	 * calculates the ranking of a term
	 * 
	 */
	public void rank() {
		List<Term> terms = datastore.createQuery(Term.class).asList();
        double max = 0;
		double min = 1;

        for(Term term : terms)
		{
			calculateTfIdf(term);
			datastore.save(term);
		}

		// get max and min
		for(Term term : terms)
		{
			for(Rank rank : term.getDocIds())
			{
				if(min > rank.getTfIdf())
				{
					min = rank.getTfIdf();
				}else if(max < rank.getTfIdf())
				{
					max = rank.getTfIdf();
				}
			}
		}

		System.out.println("min" + min);
		System.out.println("max" + max);

		for(Term term : terms)
		{
			for(Rank rank : term.getDocIds())
			{
				rank.setTfIdf( normalized(rank.getTfIdf(), min, max ));
			}
			datastore.save(term);
		}

        linkAnalysis();
        normalizedRank();
	}


	}
    public void linkAnalysis(){
        set();
        incomingLink();
        linkRank();
    }

    private void normalizedRank(){
        MongoCursor<Document> cursor = pages.find().iterator();
        double min=Double.MAX_VALUE;
        double max=Double.MIN_VALUE;

        //get min and max ranks
        while(cursor.hasNext()){
            double current = (Double) cursor.next().get("rank");
                if (current > max) max=current;
                if (current < min ) min=current;
        }

        cursor = pages.find().iterator();
        while(cursor.hasNext()){
            Document obj = cursor.next();
            double rank = ((Number) obj.get("rank")).doubleValue();
            double normalizedRank = normalized(rank, min, max);
            pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("normalizedRank", normalizedRank)));
        }
    }

    public void setRank(){
        set();
    }
	
	private void calculateTfIdf(Term term)
	{
		for(Rank doc : term.getDocIds())
		{
			double tf = (double)doc.getTf()/(double)doc.getTotalTermCount();
			double idf = Math.log( (double)6048 / (double) term.getDocIds().size());
			double tf_tdf = tf/idf;
			
			System.out.println("df: " + term.getDocIds().size());
			System.out.println("term: " + term.getTerm());
			
			System.out.println("tf_tdf: " + tf_tdf);
			doc.setTfIdf( tf_tdf);
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
    public void incomingLink(){
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        MongoDatabase database = mongoClient.getDatabase("local");
        MongoCollection<org.bson.Document> pages = database.getCollection("pages");

        MongoCursor<Document> cursor = pages.find().iterator();

        try {
            while(cursor.hasNext()){
                Document obj = cursor.next();
                String url = obj.get("url") + "";

                List<String> test = new ArrayList<>();
                test = (List<String>) obj.get("links");

                for(String s: test) {
                    String link = s;//.replaceAll("(\\.\\.\\/)|(articles\\/)|([a-zA-Z0-9]+\\/)|([^a-z]\\/)", "").replace("[", "").replace("]", "");
                    int urlHash = link.hashCode();
                    Document doc = pages.find(eq("_id", urlHash)).first();
                    System.out.println(urlHash + " : " +  link);
                    if(!(doc == null)){
                        int incomingLinks = ((Number) doc.get("inLinks")).intValue() + 1;

                        pages.updateOne(eq("_id", urlHash), new Document("$set", new Document("inLinks", incomingLinks)));
                        pages.updateOne(new Document("_id", urlHash), new Document("$push", new Document("incomingLinks", url)));
                    }
                }

                //StringTokenizer st = new StringTokenizer(obj.get("links").toString(), ", ");

                /*while (st.hasMoreTokens()) {
                    //System.out.println("Current rank: " + total);
                    String link = st.nextToken().replaceAll("(\\.\\.\\/)|(articles\\/)|([a-zA-Z0-9]+\\/)|([^a-z]\\/)", "").replace("[", "").replace("]", "");
                    //String link = st.nextToken();
                    int urlHash = link.hashCode();
                    Document doc = pages.find(eq("_id", urlHash)).first();
                    System.out.println(urlHash + " : " +  link);
                    if(!(doc == null)){
                        int incomingLinks = ((Number) doc.get("inLinks")).intValue() + 1;

                        pages.updateOne(eq("_id", urlHash), new Document("$set", new Document("inLinks", incomingLinks)));
                        pages.updateOne(new Document("_id", urlHash), new Document("$push", new Document("incomingLinks", url)));
                    }
                }*/
            }
        }finally {

        }
    }

    /*
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
                obj.append("rank", 0);
                obj.append("currentRank", 0);
                pages.insertOne(obj);

            }else {
                int incomingLinks = ((Number) doc.get("inLinks")).intValue() + 1;
                System.out.println("Total sites linking to " + link + ": " + incomingLinks);
                System.out.println("Total InLinks: " + incomingLinks);

                pages.updateOne(eq("_id", urlHash), new Document("$set", new Document("inLinks", incomingLinks)));
            }
        }
    }*/

    // Use to set link rank to initial value
	private static void set(){
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		MongoDatabase database = mongoClient.getDatabase("local");
		MongoCollection<org.bson.Document> pages = database.getCollection("pages");

		MongoCursor<Document> cursor = pages.find().iterator();

		double rank = 1.0 / pages.count();
        System.out.println("Setting rank to: " + rank);
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
		//System.out.println("Before loop: " + (totalDone / collectionSize));
		while(!((totalDone / collectionSize) > 0.50)){
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
            // This part can be in a separate method that returns an int
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
