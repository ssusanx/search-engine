package org.search.query.service;

import org.springframework.stereotype.Service;


/**
 * This class handles the search queries 
 * @author susansun
 *
 */
@Service
public class SearchService {
	
	public Object find()
	{
		
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
		
		return null;
		
	}

}
