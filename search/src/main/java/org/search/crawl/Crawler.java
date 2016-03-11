package org.search.crawl;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.bson.Document;
import static com.mongodb.client.model.Filters.*;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.text.DecimalFormat;


public class Crawler extends WebCrawler{
	HashMap<Integer, String> visitedSite = new HashMap<>();
	MongoClient mongoClient = null;
    MongoDatabase database = null;
    MongoCollection<Document> pages = null;
    MongoCollection<Document> index = null;
	MongoCollection<Document> stopWords = null;
    StringTokenizer tokenStopWords;

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");
	
	@Override
	public void onStart() {
		mongoClient = new MongoClient( "localhost" , 27017 );
		database = mongoClient.getDatabase("local");
		
		pages = database.getCollection("pages");
		index = database.getCollection("index");
        stopWords = database.getCollection("stopWords");

        Document words = stopWords.find().first();
        tokenStopWords = new StringTokenizer(words.get("words").toString(), ",");
	}
	
	@Override
	public WebURL handleUrlBeforeProcess(WebURL curURL) {
		//TODO: handle the :80 here

        if(curURL.getURL().endsWith("pdf")){
            saveImage(curURL.getURL(), "pdf");
            String fileName = curURL.getURL().substring(curURL.getURL().lastIndexOf("/"));
            extractPDF("pdf/" + fileName);
        }

        System.out.println(curURL.getURL());

	    return curURL;
	}
	
	@Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
		
		//if the seed url, visit
		if(url.getDepth() == 0)
			return true;
		
        Integer id = url.getURL().toLowerCase().hashCode();
        FindIterable<Document> result = pages.find(eq("_id", id));
        if(result.first() != null)
        	System.out.println("skipping "+ url.getURL());
        return result.first() == null;

    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("Visiting URL: " + url);
        Boolean isExtract = (Boolean)getMyController().getCustomData();
        if(isExtract)
        	extract(page);

        //System.out.println("\n\n\nVisited Sites:");

        int counter = 0;
        for (Integer name: visitedSite.keySet()){
            counter += 1;
            String key = name.toString();
            String value = visitedSite.get(name).toString();
            //System.out.println(counter + ": " + key + " " + value);
            //System.out.println("count: " + counter);
        }
    }

	private void extract(Page page) {
		
		String url = page.getWebURL().getURL();
		
		if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Map<String, String> metatags = htmlParseData.getMetaTags();
            
            String text = htmlParseData.getText();

            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            List<String> list = new ArrayList<String>();
            for (WebURL link : links) {
            	list.add(link.getURL());
            }

            String path = rawHTML(url, text);
            System.out.println("Path: " + path);

            Document doc = pages.find(eq("_id", url.toLowerCase().hashCode())).first();
            if(doc == null){
                Document obj = new Document();
                obj.append("_id", url.toLowerCase().hashCode());
                obj.append("url", url);
                obj.append("hash", url.toLowerCase().hashCode());
                obj.append("title", metatags.get("title"));
                obj.append("description", metatags.get("description"));
                obj.append("content-type", metatags.get("content-type"));
                obj.append("text", text);
                obj.append("links", list);
                obj.append("path", path);
                obj.append("outLinks", links.size());
                obj.append("inLinks", 0);
                obj.append("rank", 0.33);
                obj.append("currentRank", 0.33);
                //obj.append("inLinks", (int)(Math.random() * 101));
                pages.insertOne(obj);
            }else{
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("title", metatags.get("title"))));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("description", metatags.get("description"))));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("content-type", metatags.get("content-type"))));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("text", text)));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("links", list)));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("path", path)));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("outLinks", links.size())));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("rank", 0.33)));
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("currentRank", 0.33)));
                /*
                pages.updateOne(new Document("_id", url.toLowerCase().hashCode()),
                        new Document("$set", new Document("title", metatags.get("title"))
                                .append("$set", new Document("description", metatags.get("description")))
                                .append("$set", new Document("content-type", metatags.get("content-type")))
                                .append("$set", new Document("text", text))
                                .append("$set", new Document("links", list))
                                .append("$set", new Document("path", path))
                                .append("$set", new Document("outLinks", links.size()))));
                */
            }

            incomingLinks(url, list);

            // Save images from url
            getMediaFromUrl(url);

            tokenizing(url.toLowerCase().hashCode(), text);
        }
	}

    private void getMediaFromUrl(String url) {
        org.jsoup.nodes.Document doc = null;
        Elements media = null;
        try {
            doc = Jsoup.connect(url).get();
            media = doc.select("[src]");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Element src : media) {
            if (src.tagName().contains("img")) {
                saveImage(src.attr("abs:src"), "images");
            }
        }
    }

    private void saveImage(String imageUrl, String folder){
        URL url = null;
        InputStream in;
        OutputStream out;

        createDirectory(folder);

        String ext = imageUrl.substring(imageUrl.lastIndexOf('.'));
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/"));
        try {
            url = new URL(imageUrl);
            in = url.openStream();
            out = new FileOutputStream(folder + fileName);


            byte[] b = new byte[2048];
            int length;

            while ((length = in.read(b)) != -1) {
                out.write(b, 0, length);
            }


            in.close();
            out.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractPDF(String file){
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = null;
        try {
            inputstream = new FileInputStream(new File(file));
            ParseContext pcontext = new ParseContext();

            //parsing the document using PDF parser
            PDFParser pdfparser = new PDFParser();
            pdfparser.parse(inputstream, handler, metadata,pcontext);

            //getting the content of the document
            System.out.println("Contents of the PDF :" + handler.toString());
            PrintWriter out = new PrintWriter(file + ".txt");
            out.print(handler.toString());
            out.close();

            //getting metadata of the document
            System.out.println("Metadata of the PDF:");
            String[] metadataNames = metadata.names();

            for(String name : metadataNames) {
                System.out.println(name+ " : " + metadata.get(name));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private String rawHTML(String url, String text) {
        // Remove special characters that windows and OSX don't allow in file names
        url = url.replaceAll("[\\/:*?\"<>|.]*", "");
        //System.out.println("New URL: " + url.toLowerCase());

        createDirectory("html");

        File file = new File("html/" + url.toLowerCase()+".txt");

        if(file.exists()){
            // Maybe change it to check when the file was last updated and update file if older than n days.
            System.out.println("File exist");
        }

        try{
            PrintWriter out = new PrintWriter(file);
            out.print(text);
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error Writing to file");
            System.out.println("can write: " + file.canWrite());
            System.out.println("exist: " + file.exists());
            System.out.println("path: " + file.getAbsolutePath());
        }

        return file.getAbsolutePath();
    }

    private void createDirectory(String dirName){
        File dir = new File(dirName);
        if(!dir.exists()){
            System.out.println("Creating directory: " + dir.getAbsolutePath());
            dir.mkdir();
        }
    }

    private void tokenizing(int hashCode, String text) {
        HashSet<String> htmlTextSet = new HashSet();
        StringTokenizer tokenText = new StringTokenizer(text, " ");

        while(tokenText.hasMoreTokens()){
            htmlTextSet.add(tokenText.nextToken());
        }

        /*
        for (String s : htmlTextSet) {
            System.out.println(s);
        }*/

//        for (String s : htmlTextSet) {
//            System.out.println(s);
//        }

        while (tokenStopWords.hasMoreTokens()){
            String token = tokenStopWords.nextToken().replaceAll("\\s","");

            if(htmlTextSet.contains(token)){
                htmlTextSet.remove(token);
                //System.out.println("removing" + token);
            }
        }

        for (String s : htmlTextSet) {
            Document obj = new Document();
            Document myDoc = index.find(eq("word", s)).first();

            if(myDoc == null){
                obj.append("word", s);
                index.insertOne(obj);
            }

            index.updateOne(eq("word", s), new Document("$push", new Document("urlHash", hashCode)));
            //System.out.println("adding: " + s + " to database");

        }
    }

    private void incomingLinks(String url, List<String> links){
        System.out.println("\n\n\n\n\n\n IncomingLinks");

        for(String link : links){
            int urlHash = link.toLowerCase().hashCode();

            Document obj = new Document();
            Document doc = pages.find(eq("_id", urlHash)).first();

            if(doc == null){
                System.out.println(link + " Does not exist \n Creating entry");
                obj.append("_id", link.toLowerCase().hashCode());
                obj.append("url", link);
                obj.append("hash", link.toLowerCase().hashCode());
                //obj.append("incomingLinks", url);
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
}