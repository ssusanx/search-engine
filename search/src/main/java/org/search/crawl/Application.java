package org.search.crawl;

import java.io.IOException;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Application {

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

        crawlSites(depth, url, extraction);

        System.out.println("Crawler started");
        
    }

    private static void crawlSites(int depth, String url, boolean isExtract){
        String crawlStorageFolder = "/Users/susansun/susan/storage";//"C:\\Users\\jwj96\\Documents\\Classes\\storage";

        int numberOfCrawlers = 10;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(depth);
        config.setMaxPagesToFetch(10000);
        // config.setResumableCrawling(true);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = null;
        try {
            controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.setCustomData(isExtract);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error creating Crawler");
        }

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed(url);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(Crawler.class, numberOfCrawlers);
        controller.shutdown();
        controller.waitUntilFinish();
    }

    private static Options setOptions() {
        Options options = new Options();

        options.addOption("d", true, "Depth");
        options.addOption("u", true, "Url");
        options.addOption("e", false, "extraction mode");

        return options;
    }

    private static CommandLine parseArg(Options options, String[] args) {
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