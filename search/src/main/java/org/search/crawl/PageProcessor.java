package org.search.crawl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageProcessor {
	
	private MongoDatabase database = null;
	MongoCollection<Document> pages = null;
    MongoCollection<Document> index = null;
	MongoCollection<Document> stopWords = null;
	
	public PageProcessor() {
	}
	
	public PageProcessor(MongoDatabase db) {
		this.database = db;
		this.pages = database.getCollection("document");
		this.index = database.getCollection("term");
        this.stopWords = database.getCollection("stopWords");
        
	}
	
	public enum DocumentType
	{
		TEXT, IMAGE, PDF;
	}
	
	/**
	 * 
	 * @param page
	 */
	public void process(Page page)
	{
		
		//saveDocumentToMongo
		//saveFileTofileSystem	 
		//if jpg page
		 //extract some info
		
		saveDocument(page);
		//add extract
		
	}
	
	/**
	 * saves the document to mongo and file system
	 * @param text
	 * @param filePath
	 * @param metadata
	 * @param links
	 */
	private void saveDocument(Page page) 
	{
		System.out.println("saving document");
		String url = page.getWebURL().getURL();
		Document doc = new Document();
		
		if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Map<String, String> metatags = htmlParseData.getMetaTags();
            
            String text = htmlParseData.getText();

            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            List<String> list = new ArrayList<String>();
            for (WebURL link : links) {
            	list.add(link.getURL());
            }

            String path = download(page);
            System.out.println("Path: " + path);
            
            doc.append("_id", url.toLowerCase().hashCode());
            doc.append("url", url);
            doc.append("hash", url.toLowerCase().hashCode());
            doc.append("title", metatags.get("title"));
            doc.append("description", metatags.get("description"));
            doc.append("content-type", metatags.get("content-type"));
            doc.append("text", text);
            doc.append("links", list);
            doc.append("type", DocumentType.TEXT.toString());
            doc.append("path", path);
            doc.append("outLinks", links.size());
            doc.append("inLinks", 0);
            doc.append("rank", 0);
            
            // Save images from url
            //getMediaFromUrl(url);

        }
		
		pages.insertOne(doc);
    }
	
	/**
	 * for extract info from image files
	 * @param page
	 */
	private void extract(Page page) 
	{
		
        
	}
	
	/**
	 * download a page to the file system
	 * @param page
	 * @return the path to the file
	 */
	public String download(Page page)
	{
		return null;
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
	
	
}
