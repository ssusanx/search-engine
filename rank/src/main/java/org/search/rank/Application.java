package org.search.rank;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Application {

    public static void main(String[] args) throws IOException
    {
    	
    	Options options = setOptions();
        // Parse Arguments
        CommandLine cmd = parseArg(options, args);
        //TODO: organize this part
        //this indexes wiki small, only to be called once
        if(cmd.hasOption("i"))
        {
        	//Indexer indexer = new Indexer();
        	//indexer.index();
            RankProcessor rankProcessor = new RankProcessor();
            rankProcessor.process();
        }
        else if(cmd.hasOption("r") )
        {
        	// calculates the tf idf and link analysis
            RankProcessor rankProcessor = new RankProcessor();
            rankProcessor.rank();
        }else if(cmd.hasOption("l")){
            RankProcessor rankProcessor = new RankProcessor();
            rankProcessor.linkAnalysis();
        }else if(cmd.hasOption("u")){
            RankProcessor rankProcessor = new RankProcessor();
            rankProcessor.resetRank();
        }
    }
    
    private static Options setOptions() {
        Options options = new Options();

        options.addOption("p", true, "path");// set the path to document collection
        options.addOption("i", false, "index");
        options.addOption("r", false, "rank");
        options.addOption("l", false, "link");
        options.addOption("u", false, "reset");

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
