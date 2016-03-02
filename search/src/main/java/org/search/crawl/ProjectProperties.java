package org.search.crawl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by susansun on 2/20/16.
 */
public class ProjectProperties {

    private static Properties prop = new Properties();


    public void load() throws IOException {
        InputStream in = getClass().getResourceAsStream("mydev.properties");
        prop.load(in);
        in.close();
    }

    public static String getPropertyAsString(String key)
    {
        return prop.getProperty(key);
    }

}
