package bing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by qshen on 2/19/15.
 */
public class StopWordsHelper {
    private static StopWordsHelper instance = null;
    private HashSet<String> stopWords = null;

    protected StopWordsHelper(){
        stopWords = initialize();
    }

    public static StopWordsHelper getInstance(){
        if(instance == null)
            instance = new StopWordsHelper();
        return instance;
    }

    public HashSet<String> getStopWords(){
        return stopWords;
    }

    private HashSet<String> initialize(){
        HashSet<String> stopwords = new HashSet<String>();
        BufferedReader br = null;
        try {

            br = new BufferedReader(new FileReader("stopwords.txt"));
            String line = br.readLine();
            while(line != null){
                stopwords.add(line.toLowerCase().trim());
                line = br.readLine();
            }
            br.close();

        }
        catch (IOException e){
            e.printStackTrace();
        }
        return stopwords;
    }
}
