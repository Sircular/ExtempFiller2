package org.zalgosircular.extempfiller2.research.fetching;

import org.zalgosircular.extempfiller2.research.Topic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Queue;

/**
 * Created by Walt on 7/9/2015.
 */
public class HTMLFetcher {
    protected Queue outQueue;

    public HTMLFetcher(Queue outQueue) {
        this.outQueue = outQueue;
    }

    public String getResponse(URI location, Topic topic) throws IOException {
        try {
            URL url = location.toURL();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            return sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // this shouldn't happen
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen either
            e.printStackTrace();
        }
        return null;
    }
}
