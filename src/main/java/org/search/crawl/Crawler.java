package org.search.crawl;

import java.io.IOException;

import org.apache.commons.cli.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Crawler {

    public static void main(String[] args) throws IOException
    {
        int depth;
        String url;
        boolean extraction;

        // Get Arguments
        Options options = setOptions();
        // Parse Arguments
        CommandLine cmd = parseArg(options, args);

        // Check if Depth and Url were passed as arguments
        // Exit if arguments are not passed
        if (!cmd.hasOption("d") || !cmd.hasOption("u")) {
            System.out.println("Arguments needed Url and Depth");
            return;
        }

        // Set Values from the parameters
        depth = Integer.parseInt(cmd.getOptionValue("d"));
        url = cmd.getOptionValue("u");
        extraction = cmd.hasOption("e");

        System.out.printf("Depth:%d, Url:%s, extraction:%b \n", depth, url, extraction);
        /*----------------------------------------------------------------------------------------------------------*/

        System.out.println("Crawler started");
    	//String url = "http://en.wikipedia.org/";
    	downloadPage(url);
    }

    private static void downloadPage(String url) throws IOException
    {
    	Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
    	Elements newsHeadlines = doc.select("#mp-itn b a");
    	
    	System.out.println(newsHeadlines.toString());
    }

    public static Options setOptions() {
        Options options = new Options();

        options.addOption("d", true, "Depth");
        options.addOption("u", true, "Url");
        options.addOption("e", false, "extraction mode");

        return options;
    }

    public static CommandLine parseArg(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return cmd;
    }
}
