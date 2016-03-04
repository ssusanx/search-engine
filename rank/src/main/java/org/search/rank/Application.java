package org.search.rank;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;

public class Application {

    public static void main(String[] args) throws IOException
    {
    	
    	RankProcessor rank = new RankProcessor();
    	rank.index();
    	
    	
    	
//    	MongoClient mongoClient = null;
//    	try{
//    		mongoClient = new MongoClient( "localhost" , 27017 );
//        	MongoCollection<Document> collection = null;
//        	MongoDatabase database = null;
//        	
//    		database = mongoClient.getDatabase("local");
//    		collection = database.getCollection("pages");
//    		
//    		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//            System.out.println("data dump started");
//            MongoCursor<Document> cursor = collection.find().iterator();
//            try {
//                while (cursor.hasNext()) {
//                	//String json = gson.toJson(obj);
//                    System.out.println(gson.toJson(cursor.next()));
//                }
//            } finally {
//                cursor.close();
//            }
//    	} finally {
//    		
//    		mongoClient.close();
//    	}
    	
    }
    
}
