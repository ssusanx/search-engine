package org.data.retrieval;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class Application {

    public static void main(String[] args)
    {
    	MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
    	MongoCollection<Document> collection = null;
    	MongoDatabase database = null;
    	
		database = mongoClient.getDatabase("local");
		collection = database.getCollection("test");
		
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
        
        mongoClient.close();
    }
    
}
