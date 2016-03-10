package org.data.retrieval;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.DoubleCodec;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

import static com.mongodb.client.model.Filters.eq;

public class Application {

    public static void main(String[] args)
    {
    	/*MongoClient mongoClient = null;
    	try{
    		mongoClient = new MongoClient( "localhost" , 27017 );
        	MongoCollection<Document> collection = null;
        	MongoDatabase database = null;
        	
    		database = mongoClient.getDatabase("local");
    		collection = database.getCollection("pages");
    		
    		Gson gson = new GsonBuilder().setPrettyPrinting().create();

            System.out.println("data dump started");
            MongoCursor<Document> cursor = collection.find().iterator();
            try {
                while (cursor.hasNext()) {
                	//String json = gson.toJson(obj);
                    System.out.println(gson.toJson(cursor.next()));
                }
            } finally {
                cursor.close();
            }
    	} finally {
    		
    		mongoClient.close();
    	}
    	*/
        reset();
        rank();


    }

    private static void reset(){
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        MongoDatabase database = mongoClient.getDatabase("local");
        MongoCollection<org.bson.Document> pages = database.getCollection("pages");

        MongoCursor<Document> cursor = pages.find().iterator();

        double rank = 1.0 / pages.count();

        try {
            while(cursor.hasNext()){
                Document obj = cursor.next();

                pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("rank", rank)));
                pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("currentRank", rank )));
            }
        }finally {

        }
    }

    private static void rank(){
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        MongoDatabase database = mongoClient.getDatabase("local");
        MongoCollection<org.bson.Document> pages = database.getCollection("pages");

        MongoCursor<Document> cursor;

        double totalDone = 0;
        long collectionSize = pages.count();
        System.out.println("Before loop: " + (totalDone / collectionSize));
        while(!((totalDone / collectionSize) > 0.80)){
            totalDone = 0;
            cursor = pages.find().iterator();
            try{
                while(cursor.hasNext()) {
                    Document obj = cursor.next();
                    //System.out.println("\n\n\n\n" + obj.get("url") + "   " + obj.get("hash"));

                    if(obj.get("incomingLinks") == null){
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(obj.get("incomingLinks").toString(), ",");

                    double total = 0;
                    while (st.hasMoreTokens()) {
                        //System.out.println("Current rank: " + total);

                        String url = st.nextToken().replace("[", "").replace("]", "").replace(" ", "");
                        int hash = url.toLowerCase().hashCode();

                        //System.out.print("url: " + url + " hash: " + url.toLowerCase().hashCode() + " ");
                        Document doc = pages.find(eq("_id", hash)).first();

                        double rank = (Double) doc.get("currentRank");
                        double links = ((Number) doc.get("outLinks")).doubleValue();
                        //System.out.print(total + " + " +  rank + " / " + links + " = ");
                        total += (rank / links);
                        //System.out.println(total);
                       // System.out.println("rank: " + (rank / links));
                    }
                    DecimalFormat df = new DecimalFormat("#.#########");
                    total = Double.parseDouble(df.format(total));
                    pages.updateOne(eq("_id", obj.get("_id")), new Document("$set", new Document("rank", total)));

                    //System.out.println("Final Total: " + total);
                    //System.out.println("Rank:" + obj.get("rank"));
                    //System.out.println("Total links" + obj.get("outLinks"));
                }
            } finally {
                cursor.close();
            }

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

